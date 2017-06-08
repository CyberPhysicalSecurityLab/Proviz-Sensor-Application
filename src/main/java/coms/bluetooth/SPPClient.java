package coms.bluetooth;

import coms.pipe.BufferedReaderInputThread;
import coms.pipe.ComCallBack;
import handler.MessageListener;
import org.apache.log4j.Logger;
import proper.ProvizProperties;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.Vector;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * A simple SPP client that connects with an SPP server
 */
public class SPPClient implements DiscoveryListener{


    private String uuidValue = "8848";
    private String btServerAddress = "8019344CB50C";

    private ProvizProperties properties;

    //object used for waiting
    private static Object lock=new Object();
    //vector containing the devices discovered
    private static Vector<RemoteDevice> vecDevices=new Vector<>();
    private static String connectionURL=null;
    private MessageListener messageListener;

    //connect to the server and send a line of text
    private StreamConnection streamConnection=null;

    private OutputStream outStream=null;
    private PrintWriter pWriter=null;
    private InputStream inStream=null;

    private BufferedReaderInputThread inputThread = null;
    private int timeOutCounter = 0; // variable to keep track of how many messages timed out
    private int numOfTimeouts = 3;  // number of timeouts before connection is dropped

    private Logger logger = Logger.getLogger(SPPClient.class.getName());

    private void resetTimeOutCounter(){
        timeOutCounter = 0;
    }

    private void incrementTimeOutCounter(){
        timeOutCounter++;
    }

    public enum State {
        CONNECTED, NOT_CONNECTED
    }
    // variable to keep track of connection
    private State state = State.NOT_CONNECTED;

    public SPPClient(){
        state = State.NOT_CONNECTED;

        properties = ProvizProperties.getInstance();
        properties.loadProperties();
    }

    public SPPClient(int numOfTimeouts){
        state = State.NOT_CONNECTED;
        this.numOfTimeouts = numOfTimeouts;

        properties = ProvizProperties.getInstance();
        properties.loadProperties();
    }

    public boolean searchForProvizSpp() throws IOException {

        // start state is not connected
        state = State.NOT_CONNECTED;

        properties.loadProperties();
        uuidValue = properties.getBtSppUuid();
        btServerAddress = properties.getBtAddr();

        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        logger.info("Local address: "+localDevice.getBluetoothAddress());
        logger.info("Local name: "+localDevice.getFriendlyName());
        logger.info("UUID: "+uuidValue);
        logger.info("Searching for : "+btServerAddress);

        //find devices
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        logger.debug("Starting device inquiry…");
        agent.startInquiry(DiscoveryAgent.GIAC, this);

        // wait till scan is complete
        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            logger.error("SPP client search error: ", e);
        }

        logger.debug("Device Inquiry Completed. ");

        //print all devices in vecDevices
        int deviceCount=vecDevices.size();
        int index = -1;

        if(deviceCount <= 0){
            logger.debug("No Devices Found .");
            return false;
        }
        else{
            //print bluetooth device addresses and names in the format [ No. address (name) ]
            logger.debug("Bluetooth Devices: ");

            for (int i = 0; i <deviceCount; i++) {
                RemoteDevice remoteDevice= vecDevices.elementAt(i);
                logger.debug((i+1)+". "+remoteDevice.getBluetoothAddress() );

                if (remoteDevice.getBluetoothAddress().equals(btServerAddress)){
                    logger.debug("Registered device found");
                    index = i;
                }
            }
        }

        if (index == -1){
            logger.debug("Registered device not found");
            return false;
        }

        //check for spp service
        RemoteDevice remoteDevice= vecDevices.elementAt(index);
        UUID[] uuidSet = new UUID[1];

        uuidSet[0]=new UUID(uuidValue,true);

        logger.debug("\nSearching for service…");
        agent.searchServices(null,uuidSet,remoteDevice,this);

        // wait till service to be found
        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            logger.error("SPP client search error: ", e);
        }

        if(connectionURL==null){
            logger.debug("Device does not support Proviz SPP Service.");
            return false;
        }

        logger.debug("Connection url: " + connectionURL);

        //connect to the server and send a line of text
        streamConnection=(StreamConnection)Connector.open(connectionURL);

        outStream=streamConnection.openOutputStream();
        pWriter=new PrintWriter(new OutputStreamWriter(outStream));

        inStream=streamConnection.openInputStream();

        inputThread = new BufferedReaderInputThread(inStream);
        inputThread.messageReceivedCallBack(callBack);
        inputThread.start();

        System.out.println(outStream);
        System.out.println(inStream);

        state = state.CONNECTED;
        return true;
    }

    public void shutdown() throws IOException {
        inputThread.shutdown();
        streamConnection.close();
        pWriter.close();
        state = State.NOT_CONNECTED;
    }

    private ComCallBack callBack = new ComCallBack() {
        public void callBackMethod(String message) {
            logger.debug("Server message: " + message);
            resetTimeOutCounter();

            if (message.contains("[SPP-OK]") || message.contains("[SPP-ERROR]")){
                logger.debug("server response received");
                synchronized(lock){
                    lock.notify();
                }
            }
            else if (message.contains("[SPP-DISCONNECT]") ){
                try {
                    respondToServer();
                    shutdown();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
            else if (message.contains("SPP-NEWFIRM")){
                // todo: finish implementation
                respondToServer();
            }
            else{
                respondToServer();
                if (messageListener != null){
                    messageListener.messageEmitted(message);
                }
            }
        }
    };

    public boolean writeToServer(String message) {

        if (state == State.NOT_CONNECTED){
            logger.debug("BT writeToServer error, no device connected");
            return false;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(new Callable() {
            public String call() throws Exception {
                //send string
                // the new line and return are need
                // for buffered reader
                pWriter.write(message + "\r\n");
                pWriter.flush();
                logger.debug("SPP Message sent to server");

                // wait for response from server
                try {
                    synchronized(lock){
                        lock.wait();
                    }
                }
                catch (InterruptedException e) {
                    logger.error("write to SPP server error: ", e);
                }

                //do operations you want
                return "OK";
            }
        });
        try {
            logger.debug(future.get(2, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            logger.error("write to SPP server timeout error: ", e);

            synchronized(lock){
                lock.notify();
            }

            incrementTimeOutCounter();
            // if more than numOfTimeouts have occurred change state
            if (timeOutCounter > numOfTimeouts){
                state = State.NOT_CONNECTED;
                try {
                    shutdown();
                } catch (IOException ex) {
                    logger.error(ex);
                }
            }
            return false;
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (ExecutionException e) {
            logger.error(e);
        }
        executor.shutdownNow();
        return true;
    }

    public void respondToServer(){
        pWriter.write("[SPP-OK]\r\n");
        pWriter.flush();
        logger.debug("Response sent to SPP server");
    }

    public void disconnectFromServer() throws IOException {
        writeToServer("[SPP-DISCONNECT]");
        shutdown();
    }

    public State getState() {
        return state;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public static void main(String[] args) throws IOException {

        // start and load proviz properties file
        ProvizProperties properties = ProvizProperties.getInstance();

        String user = System.getenv("SUDO_USER");
        if (user == null)
            user = System.getProperty("user.name");

        String configFilePath = "/home/" + user + "/.proviz/.config.properties";

        properties.setConfigFilePath(configFilePath);
        properties.loadProperties();

        SPPClient client=new SPPClient();
        boolean connection = client.searchForProvizSpp();

        if (!connection)
            System.exit(0);

        while(true){

            try {
                //send string
                client.writeToServer("New String from SPP Client\r\n");
                sleep(3000);
            } catch (Exception e) {
                System.out.println("Encountered Error " + e);
            }
        }
    }


    //methods of DiscoveryListener
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

        //add the device to the vector
        if(!vecDevices.contains(btDevice)){
            vecDevices.addElement(btDevice);
        }
    }

    //implement this method since services are not being discovered
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if(servRecord!=null && servRecord.length>0){
            connectionURL=servRecord[0].getConnectionURL(0,false);
        }
        synchronized(lock){
            lock.notify();
        }
    }
    //implement this method since services are not being discovered
    public void serviceSearchCompleted(int transID, int respCode) {
        synchronized(lock){
            lock.notify();
        }
    }
    public void inquiryCompleted(int discType) {
        synchronized(lock){
            lock.notify();
        }
    }//end method
}
