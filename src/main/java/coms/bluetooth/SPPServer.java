package coms.bluetooth;


import coms.pipe.BufferedReaderInputThread;
import coms.pipe.ComCallBack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class SPPServer extends Thread {

    private UUID uuid = null;

    //Create the servicve url
    private String connectionString = null;
    private StreamConnectionNotifier streamConnNotifier = null;
    private StreamConnection connection = null;
    private PrintWriter pWriter = null;
    private BufferedReaderInputThread inputThread = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;

    private volatile JSONParser parser = new JSONParser();

    private volatile boolean running = true;
    //object used for waiting
    private static Object lock=new Object();
    private int timeOutCounter = 0; // variable to keep track of how many messages timed out
    private int numOfTimeouts = 3;  // number of timeouts before connection is dropped


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

    //start server
    private void startServer() throws IOException {

        //Create a UUID for SPP
        uuid = new UUID("8848", true);
        connectionString = "btspp://localhost:" + uuid +";name=SPP Server";
        //open server url
        streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
        state = State.NOT_CONNECTED;

    }

    private ComCallBack callBack = new ComCallBack() {
        public void callBackMethod(String message) {

            System.out.println("SSP Client: " + message);
            resetTimeOutCounter();

            // todo: check for valid packet

            if (message.contains("[SPP-OK]") || message.contains("[SPP-ERROR]")){
                System.out.println("client response received");
                synchronized(lock){
                    lock.notify();
                }
            }
            else if (message.contains("[SPP-DISCONNECT]") ){
                try {
                    System.out.println("Request to disconnect");
                    respondToClient();
                    shutdown();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            else{
                respondToClient();

                // check for valid json object
                JSONObject readJson = null;
                boolean isJson;

                try {
                    readJson = (JSONObject) parser.parse(message);
                    isJson = true;
                    System.out.println("is json");
                } catch (ParseException e) {
                    isJson = false;
                    System.out.println("is not json");
                }
            }
        }
    };

    public boolean writeToClient(String message) {

        if (state == State.NOT_CONNECTED){
            System.out.println("Error not connected to a client");
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
                System.out.println("SPP Message sent to client");

                // wait for response from server
                try {
                    synchronized(lock){
                        lock.wait();
                    }
                }
                catch (InterruptedException e) {
                    System.out.println("write to SPP client error: "+ e);
                }

                //do operations you want
                return "OK";
            }
        });
        try {
            System.out.println(future.get(2, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            System.out.println("write to SPP client timeout error: "+ e);

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
                    System.out.println(ex);
                }
            }
            return false;
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (ExecutionException e) {
            System.out.println(e);
        }
        executor.shutdownNow();
        return true;
    }

    public boolean requestStartApplication(){
        return writeToClient("[SPP-START]");
    }

    public boolean requestStopApplication(){
        return writeToClient("[SPP-STOP]");
    }

    public void respondToClient(){
        pWriter.write("[SPP-OK]\r\n");
        pWriter.flush();
    }

    public void disconnectFromClient() throws IOException {
        writeToClient("[SPP-DISCONNECT]");
        shutdown();
    }
    public void run(){

        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect......");

        try {
            connection = streamConnNotifier.acceptAndOpen();

            RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);

            System.out.println("Remote device address: "+dev.getBluetoothAddress());
            System.out.println("Remote device name: "+dev.getFriendlyName(true));
            state = State.CONNECTED;

            //read string from spp client
            inStream = connection.openInputStream();
            outStream = connection.openOutputStream();
            pWriter=new PrintWriter(new OutputStreamWriter(outStream));

            inputThread = new BufferedReaderInputThread(inStream);
            inputThread.messageReceivedCallBack(callBack);
            inputThread.start();


            System.out.println(outStream);
            System.out.println(inStream);

            while (running){
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Interrupt Exception");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendFile() throws IOException {

        // todo: complete
    }


    public void shutdown() throws IOException {
        running = false;
        streamConnNotifier.close();
        pWriter.close();
        connection.close();
        inputThread.shutdown();
        state = State.NOT_CONNECTED;

    }


    public static void main(String[] args) throws IOException {

        LocalDevice localDevice = LocalDevice.getLocalDevice();

        System.out.println("new ");
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());
        Scanner input = new Scanner(System.in);

        SPPServer SPPServer = new SPPServer();

        try {
            SPPServer.startServer();
            SPPServer.start();

            while(true){
                System.out.println("Enter command ");
                String command = input.nextLine();
                System.out.println("Command " + command);
                if (command.equals("1")){
                    SPPServer.requestStartApplication();
                }
                if (command.equals("2")){
                    SPPServer.requestStopApplication();
                }
                if (command.equals("3")){
                    SPPServer.sendFile();
                }

            }
        } catch (Exception e){
            System.out.println("Server exception " + e);
            SPPServer.shutdown();
        }
    }
}