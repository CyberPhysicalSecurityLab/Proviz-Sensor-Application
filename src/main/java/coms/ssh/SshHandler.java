package coms.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import proper.ProvizProperties;

/**
 * Created by bigbywolf on 1/26/17.
 */
public class SshHandler {

    private String user = null;
    private String host = null;
    private String pass = null;
    private int port = 22;
    private String knowHostFile = null;
    private String identityfile = null;

    SftpTransfer sftp = null;
    SshExec exec = null;

    ProvizProperties properties = ProvizProperties.getInstance();

    // contructor will use parameters from properties file
    public SshHandler() throws JSchException {
        ProvizProperties properties = ProvizProperties.getInstance();
        properties.loadProperties();

        user = properties.getHostUserName();
        host = properties.getHostIpAddr();
        pass = properties.getHostPasskey();
        port = Integer.parseInt(properties.getHostPortNumber());
        knowHostFile = properties.getKnowHostFile();
        identityfile = properties.getIdentityFile();

        sftp = new SftpTransfer(user, host, port, pass, knowHostFile);
        exec = new SshExec(user, host, port, pass, knowHostFile);
    }

    public void connect() throws JSchException {

        sftp.connect();
        exec.connect();
    }

    public void disconnect(){
        sftp.disconnect();
        exec.disconnect();
    }

    // todo: create better messages
    public String sendStartAppCommand(){
        String command="echo 'start' > " + properties.getRemoteProvizDir() + "pipes/control_pipe";
        return  exec.sendCommand(command);
    }

    public String sendStopAppCommand(){
        String command="echo 'stop' > " + properties.getRemoteProvizDir() + "pipes/control_pipe";
        return  exec.sendCommand(command);
    }

    public void transferApplication() throws SftpException {

        String commandResponse = "";

        commandResponse = sendStopAppCommand();
        System.out.println("Command response: " + commandResponse);

        String src = properties.getLocalProvizDir() + "app/*";
        String dst = properties.getRemoteProvizDir() + "app/";
        System.out.println("src: " + src + " dst: " + dst);

        // delete previous content
        sftp.getSftp().rm(dst+"/*");

        // send content of the local app dir to remote app dir
        sftp.put(src, dst);

        // send new properties file
        src = properties.getConfigFilePath();
        dst = properties.getRemoteProvizDir() + ".config.properties";
        System.out.println("src: " + src + " dst: " + dst);
        sftp.put(src, dst);

        commandResponse = sendStartAppCommand();
        System.out.println("Command response: " + commandResponse);
    }

    public void transferConfigFile() throws SftpException {

        String commandResponse = "";

        commandResponse = sendStopAppCommand();
        System.out.println("Command response: " + commandResponse);

        // send new properties file
        String src = properties.getConfigFilePath();
        String dst = properties.getRemoteProvizDir() + ".config.properties";
        System.out.println("src: " + src + " dst: " + dst);
        sftp.put(src, dst);

        commandResponse = sendStartAppCommand();
        System.out.println("Command response: " + commandResponse);
    }

    // todo: finish commands
    // Doesn't work yet
    private String sendStartComAppCommand(){
        String command = "bash " + properties.getRemoteProvizDir() + "run.sh";
//        String command = "java -Dlog4j.configuration=file:/home/pi/.proviz/src/log4j.properties -jar /home/pi/.proviz/src/provizclient.com.manager.jar";
        System.out.println("sending commands: " + command);
        return  exec.sendCommand(command);
    }

    public String sendStopComAppCommand(){
//        String command="echo 'fullStop' > ~/.proviz/pipes/control_pipe";
        String command="echo 'fullStop' > " + properties.getRemoteProvizDir() + "pipes/control_pipe";
        System.out.println("sending commands: " + command);
        return  exec.sendCommand(command);
    }

    // Haven't found a way to restart com application
    public void transferComApplication(){

    }


    public static void main(String[] args) {
        writeProperties();

        try {
            SshHandler sshHandler = new SshHandler();
            sshHandler.connect();
            sshHandler.transferConfigFile();

            sshHandler.disconnect();

        } catch (JSchException e) {
            System.out.println("jsch exception");
            e.printStackTrace();
        }
        catch (SftpException e){
            System.out.println("ftp transfer error");
            e.printStackTrace();
        }

    }

    public static void writeProperties(){

        String user = System.getProperty("user.name");
        String configFilePath = "/home/" + user + "/.proviz/pi_config.properties";

        ProvizProperties properties = ProvizProperties.getInstance();
        properties.setConfigFilePath(configFilePath);

        // general settings
        properties.setProvizVer("0.0.1"); // proviz version
        properties.setDeviceName("proviz-pi"); // device name sent to server
        properties.setDeviceId("proviz117"); // device id sent to server
        properties.setAppName("app-1.0-SNAPSHOT-standalone.jar"); // name of app that will be started on iot device
        properties.setAppVer("0.0.1"); // com manager app version
        properties.setConnectionType("http"); // http, bluetooth
        properties.setStaticSettings("false"); // decide if static up settings are going to be used
        properties.setLocalProvizDir("/home/" + user + "/.proviz/"); // local pc proviz dir path
        properties.setRemoteProvizDir("/home/pi/.proviz/"); // remote iot device proviz dir path

        // harware settings *optional*
        properties.setHwModel("raspberry pi");
        properties.setHwVer("3");
        properties.setOsVer("debian jessie");

        // wifi settings
        // since device is already connected to wifi
        properties.setWifiSsid("FIU_WiFi"); // wifi ssid
        properties.setWifiPsk(""); // wifi password
        properties.setHostUserName("pi"); // username of iot device
        properties.setHostIpAddr("10.42.0.209"); // ip address of the iot device, used for sending application
        properties.setHostPortNumber("22"); // ssh port
        properties.setHostPasskey("raspberry"); // iot device ssh password/hostPasskey
        properties.setKnowHostFile("/home/"+user+"/.ssh/known_hosts"); // local pc ssh know host file
        properties.setIdentityFile("/home/"+user+"/.ssh/id_rsa"); // local pc ssh identify public key
        properties.setWifiEncryption("Open"); // WPA, WEP, Open

        // http settings
        properties.setServerIp("10.109.63.249"); // ip address that http post will be sent to
        properties.setServerPort("5867"); // port number of server that http post will be sent to

        // static ip settings
        properties.setStaticIp("");
        properties.setStaticNetmask("");
        properties.setStaticGateWay("");

        // bluetooth settings
        properties.setBtName("workstation"); // name of bl device, not used by to find device
        properties.setBtAddr("5CF37071096D"); // bl device mac address, used to find device
        properties.setBtSppUuid("8848"); // bl spp uuid, used to find service
        properties.setBtObexUuid("");

        // send settings to file
        properties.writeProperties();
        // reload new properties
        properties.loadProperties();
    }
}
