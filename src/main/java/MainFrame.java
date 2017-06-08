import coms.bluetooth.SPPClient;
import coms.http.client.WebClientManager;
import coms.http.client.models.requests.SendSensorValueRequest;
import coms.http.client.models.requests.SensorData;
import coms.http.client.models.responses.SendSensorValueResponse;
import coms.pipe.ComCallBack;
import coms.pipe.ReadPipeInputThread;
import handler.AppComHandler;
import handler.MessageListener;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import proper.ProvizProperties;
import retrofit2.Call;
import retrofit2.Response;
import ui.monitor.Monitor;
import ui.systray.SysTray;
import ui.systray.SysTrayListener;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by bigbywolf on 1/6/17.
 */
public class MainFrame extends JFrame {

    private SysTray sysTray;
    private AppComHandler appComHandler = null;
    private Monitor monitor = null;
    private ReadPipeInputThread controlCommands = null;
    private SPPClient sppClient = null;

    private Logger logger = Logger.getLogger(MainFrame.class.getName());
    private ProvizProperties properties = ProvizProperties.getInstance();

    private boolean monitorStatus = false;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private volatile JSONParser parser = new JSONParser();

    private WebClientManager webClientManager4Tablet;
    private WebClientManager webClientManager4Desktop;

    public MainFrame(){
        super("Proviz Client");

        sysTray = new SysTray();
        appComHandler = new AppComHandler();
        monitor = new Monitor();

        monitor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                monitorStatus = false;
                logger.debug("Monitor closed");
            }
        });

        // Handle messages from the control pipe
        controlCommands = new ReadPipeInputThread(properties.getRemoteProvizDir() + "pipes/control_pipe");
        controlCommands.messageReceivedCallBack(controlCallBack);
        controlCommands.start();



        webClientManager4Desktop = new WebClientManager("http://" + properties.getServerIp() + ":" + properties.getServerPort() + "/");
        webClientManager4Tablet = new WebClientManager("http://" + properties.getTabletIp() + ":" + properties.getTabletPort() + "/");
        // Handle messages from systray
        setUpSysTray();
        // handle messages from AppComHandler
        addAppMessageListener();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                appComHandler.closeCom();
                logger.info("Main frame closing");
                if (sppClient != null){
                    try { sppClient.disconnectFromServer(); }
                    catch (IOException e) { logger.error(e);}
                }
            }
        });

        logger.info("Main frame started");

//        // If using bt coms, create spp client
        if (properties.getConnectionType().contains("bluetooth")){
            sppClient = new SPPClient();
            // spp message listener created
            addSppMessageListener();
            try {
                if (sppClient.searchForProvizSpp()){
                    JOptionPane.showMessageDialog(null,
                            "BT Connected to Proviz Server" );
                }
                else {
                    JOptionPane.showMessageDialog(null,
                            "BT Not Connected, check BT address" );
                }
            } catch (IOException e) {
                logger.error("Search For Proviz SPP ", e);
            }
        }
    }

    private void addSppMessageListener(){

        sppClient.setMessageListener(new MessageListener() {
            @Override
            public void messageEmitted(String text) {
                // todo: remove
                logger.info("spp message listener received " + text);

                if (text.equals("[SPP-START]")){
                    logger.debug("SPP Server request to start application");
                    startApplication();
                }

                if (text.equals("[SPP-STOP]")){
                    logger.debug("SPP Server request to stop application");
                    stopApplication();
                }

                if (text.equals("[SPP-NEWFIRM]")){
                    logger.debug("SPP Server request to receive new firmware");
                }

            }
        });
    }

    private void addAppMessageListener(){
        appComHandler.setMessageListener(new MessageListener() {
            @Override
            public void messageEmitted(String text) {
                JSONObject readJson = null;
                boolean isJson;

                try {
                    readJson = (JSONObject) parser.parse(text);
                    isJson = true;
                    logger.debug("is json");
                } catch (ParseException e) {
                    isJson = false;
                    logger.debug("is not json");
                }

                // Check if monitor is active
                if (monitorStatus){
                    if (!isJson) {
                        monitor.appendText("In stream: " + text + "\n");
                    }
                    // If in json format convert to nice string
                    if (isJson) {
                        monitor.appendText("<---- Sensor Message Start ---->\n" + jsonToNiceString(readJson));
                    }
                }

                // http post
                if (properties.getConnectionType().contains("http") && isJson){
                    try {

                        // Check for server ip
                        if (!Objects.equals(properties.getServerIp(), "") && !Objects.equals(properties.getServerPort(), "")) {
                            SendSensorValueRequest request = postRequest(readJson);
                            Call<SendSensorValueResponse> response = webClientManager4Desktop.getWebApis().sendSensorData(request);

                            logger.debug("Sending http packet: " + request + " to server");
                            Response<SendSensorValueResponse> respons = response.execute();
                            logger.debug(respons.body().getResult());
                        }

                        // Check for tablet ip
                        if (!Objects.equals(properties.getTabletIp(), "") && !Objects.equals(properties.getTabletPort(), "")) {
                            SendSensorValueRequest request = postRequest(readJson);
                            Call<SendSensorValueResponse> response = webClientManager4Tablet.getWebApis().sendSensorData(request);

                            logger.debug("Sending http packet: " + request + " to tablet");
                            Response<SendSensorValueResponse> respons = response.execute();
                            logger.debug(respons.body().getResult());
                        }
                    } catch (IOException e) {
                        logger.error("Unable to send post", e);
                    } catch (Exception e){
                        logger.error("Error ", e);
                    }
                }

                // bt message
                if (properties.getConnectionType().contains("bluetooth") && isJson){

                    // Check if spp client was created
                    if (sppClient == null){
                        sppClient = new SPPClient();
                        addSppMessageListener();
                        try {
                            if (sppClient.searchForProvizSpp()){
                                JOptionPane.showMessageDialog(null,
                                        "BT Connected to Proviz Server" );
                            }
                            else {
                                JOptionPane.showMessageDialog(null,
                                        "BT Not Connected, check BT address" );
                            }
                        } catch (IOException e) {
                            logger.error("Search For Proviz SPP ", e);
                        }
                    }
//
                    sppClient.writeToServer(jsonAppendParameters(readJson).toString());
                }
            }
        });
    }


    private void startApplication(){
        logger.debug("Application: Start");
        if (appComHandler == null){
            appComHandler = new AppComHandler();
            addAppMessageListener();
        }
        else {
            logger.debug("Application already running");
        }
    }

    private void stopApplication(){

        logger.debug("Application: Stop");
        if (appComHandler != null){
            appComHandler.closeCom();
            appComHandler = null;
        }
        else {
            logger.debug("No application running");
        }
    }

    private ComCallBack controlCallBack = new ComCallBack() {
        @Override
        public void callBackMethod(String message) {

            logger.debug("Control pipe request " + message);
            if(message.contains("start")){
                startApplication();
            }
            if(message.contains("stop")){
                stopApplication();
            }

            if(message.contains("fullStop")){
                logger.debug("Control pipe request Com.Manager shutdown");
                System.exit(0);
            }
        }
    };

    private void setUpSysTray(){

        sysTray.setSysTrayListner(new SysTrayListener() {
            public void selectedMenuItem(String menuItem) {

                if (Objects.equals(menuItem, "Monitor")){
                    monitorStatus = true;

                    logger.info("Monitor opened");
                    monitor.clearMonitor();

                    monitor.setMonitorVisible(true);
                }

                if(Objects.equals(menuItem, "Start")){
                    logger.debug("System tray start");
                    if (appComHandler == null){
                        appComHandler = new AppComHandler();
                        addAppMessageListener();

//                        logger.debug("in start monitor status is: "  + monitorStatus);
                        logger.info("Systray request start application");

                        JOptionPane.showMessageDialog(null,
                                "Application has been Started");
                    }
                    else{
                        JOptionPane.showMessageDialog(null,
                                "Application already running");
                    }
                }

                if (Objects.equals(menuItem, "Restart")){
                    logger.debug("System tray restart");
                    if (appComHandler != null){
                        appComHandler.closeCom();
                        appComHandler = null;
                    }

                    logger.info("Systray request restart application");
                    if (appComHandler == null){
                        appComHandler = new AppComHandler();
                        addAppMessageListener();
                    }
                    else
                        JOptionPane.showMessageDialog(null,
                            "Application has been restarted");
                }
                if (Objects.equals(menuItem, "Stop")){
                    logger.debug("System tray stop");
                    if (appComHandler != null){
                        appComHandler.closeCom();
                        appComHandler = null;
                        logger.info("Systray request stop application");

                        JOptionPane.showMessageDialog(null,
                                "Application has been stopped");
                    }
                    else{
                        JOptionPane.showMessageDialog(null,
                                "No application running");
                    }
                }

                if (Objects.equals(menuItem, "btStatus")){

                    logger.debug("System tray bt status");
                    JOptionPane.showMessageDialog(null,
                            "BT Status " + sppClient.getState());
                }

                if (Objects.equals(menuItem, "btConnect")){

                    logger.debug("System tray bt connect");
                    try {
                        if (sppClient.searchForProvizSpp()){
                            JOptionPane.showMessageDialog(null,
                                    "BT Connected to Proviz Server" );
                        }
                        else {
                            JOptionPane.showMessageDialog(null,
                                    "BT Not Connected, check BT address" );
                        }
                    } catch (IOException e) {
                        logger.error("Search For Proviz SPP ", e);
                    }
                }

                if (Objects.equals(menuItem, "btDisconnect")){

                    logger.debug("System tray bt disconnect");
                    try {
                        sppClient.disconnectFromServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null,
                            "BT disconnected" );
                }
                // TODO: view logs
            }
        });

    }

    // todo: thread to periodic check ip addr, bt connection
    // todo: check for ip change
    // todo: need to add peripheral name

    private String jsonToNiceString(JSONObject jsonObject) {

        String niceString = "";
        JSONArray msg = (JSONArray) jsonObject.get("sensors");
        JSONObject sensor;
        Iterator<JSONObject> iterator = msg.iterator();
        while (iterator.hasNext()) {
            sensor = iterator.next();
            niceString += "Sensor: " + sensor.get("sensorName") + ", id: " + sensor.get("sensorId") + "\n";
            niceString += "Variable: " + sensor.get("variableName") + " = " + sensor.get("sensorValue") + " " + sensor.get("sensorUnit") + "\n";
            niceString += "-> \n";
        }
        niceString += "\n";
        return niceString;
    }


    private JSONObject jsonAppendParameters(JSONObject jsonObject){

        JSONObject message = new JSONObject();
        message.put("deviceName", properties.getDeviceName());
        message.put("deviceId", properties.getDeviceId());

        Date date = new Date();
        message.put("dateTime", dateFormat.format(date));

        JSONArray sensorMsg = (JSONArray) jsonObject.get("sensors");

        message.put("sensors", sensorMsg);

        return message;
    }

    private SendSensorValueRequest postRequest(JSONObject jsonObject){
        SendSensorValueRequest request = new SendSensorValueRequest();

        request.setDeviceId(properties.getDeviceId());
        request.setDeviceName(properties.getDeviceName());

        Date date = new Date();
        request.setDateTime(dateFormat.format(date));

        ArrayList<SensorData> sensor = new ArrayList<>();

        JSONArray msg = (JSONArray) jsonObject.get("sensors");
        JSONObject sensorObj;
        Iterator<JSONObject> iterator = msg.iterator();

        while (iterator.hasNext()) {
            sensorObj = iterator.next();
            SensorData sensorData = new SensorData();
            sensorData.setSensorId((String)sensorObj.get("sensorId"));
            sensorData.setSensorName((String)sensorObj.get("sensorName"));
            sensorData.setSensorUnit((String)sensorObj.get("sensorUnit"));
            sensorData.setSensorValue((double)sensorObj.get("sensorValue"));
            sensorData.setVariableName((String)sensorObj.get("variableName"));
            sensor.add(sensorData);
        }

        request.setSensors(sensor);

        return request;
    }
}
