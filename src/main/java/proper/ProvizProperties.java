package proper;

import java.io.*;
import java.util.Properties;

/**
 * Created by bigbywolf on 1/19/17.
 *
 * Singleton class to read or write to properties file. No matter the propose first the configFilePath must be set,
 * this is the location that the file will be read from or written too.
 *
 * To read file first set configFilePath and use the loadProperties() method to load values, then use the individual
 * get methods for each parameter.
 *
 * To write file first set the configFilePath then set the individual parameters using set methods. Lastly
 * call the writeProperties() to write parameters to file.
 *
 */
public class ProvizProperties {

    private static ProvizProperties ourInstance = new ProvizProperties();

    public static ProvizProperties getInstance() {
        return getOurInstance();
    }



    private  String configFilePath = "";

    // general parameters
    private String provizVer = "";
    private String deviceName = "";
    private String deviceId = "";
    private String appName = "";
    private String appVer = "";
    private String connectionType = "";
    private String staticSettings = "";
    private String localProvizDir = "";
    private String remoteProvizDir = "";

    // hardware settings
    private String hwModel = "";
    private String hwVer = "";
    private String osVer = "";

    // wifi/ssh settings
    private String wifiSsid = "";
    private String wifiPsk = "";
    private String hostUserName = "";
    private String hostIpAddr = "";
    private String hostPortNumber = "";
    private String hostPasskey = "";
    private String knowHostFile = "";
    private String identityFile = "";
    private String wifiEncryption = "WPA";

    // http server settings
    private String serverIp = "";
    private String serverPort = "";
    private String tabletIp = "";
    private String tabletPort = "";

    //  ip settings
    private String staticIp = "";
    private String staticNetmask = "";
    private String staticGateWay = "";

    // bluetooth settings
    private String btName = "";
    private String btAddr = "";
    private String btSppUuid = "";
    private String btObexUuid = "";

    private ProvizProperties() {}

    public static ProvizProperties getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(ProvizProperties ourInstance) {
        ProvizProperties.ourInstance = ourInstance;
    }

    public void loadProperties(String configFilePath){

        this.configFilePath = configFilePath;
        setConfigFilePath(configFilePath);
        loadProperties();
    }

    public void loadProperties(){
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(getConfigFilePath());
            // load a properties file
            prop.load(input);

            // get the property
            setProvizVer(prop.getProperty("proviz-ver"));
            setDeviceName(prop.getProperty("device-name"));
            setDeviceId(prop.getProperty("device-id"));
            setAppName(prop.getProperty("app-name"));
            setAppVer(prop.getProperty("app-ver"));
            setConnectionType(prop.getProperty("connection-type"));
            setStaticSettings(prop.getProperty("static-settings"));
            setLocalProvizDir(prop.getProperty("local-proviz-dir"));
            setRemoteProvizDir(prop.getProperty("remote-proviz-dir"));

            // hardware settings
            setHwModel(prop.getProperty("hw-model"));
            setHwVer(prop.getProperty("hw-ver"));
            setOsVer(prop.getProperty("os-version"));

            // wifi settings
            setWifiSsid(prop.getProperty("wifi-ssid"));
            setWifiPsk(prop.getProperty("wifi-psk"));
            setHostUserName(prop.getProperty("user-name"));
            setHostIpAddr(prop.getProperty("host-name"));
            setHostPortNumber(prop.getProperty("port-number"));
            setHostPasskey(prop.getProperty("pass-key"));
            setKnowHostFile(prop.getProperty("know-host-file"));
            setIdentityFile(prop.getProperty("identity-file"));
            setWifiEncryption(prop.getProperty("wifi-encryption"));

            // http server settings
            setServerIp(prop.getProperty("server-ip"));
            setServerPort(prop.getProperty("server-port"));
            setTabletIp(prop.getProperty("tablet-ip"));
            setTabletPort(prop.getProperty("tablet-port"));

            // static ip settings
            setStaticIp(prop.getProperty("static-ip"));
            setStaticNetmask(prop.getProperty("static-netmask"));
            setStaticGateWay(prop.getProperty("static-gateway"));

            // bluetooth settings
            setBtName(prop.getProperty("bt-name"));
            setBtAddr(prop.getProperty("bt-addr"));
            setBtSppUuid(prop.getProperty("bt-ssp-uuid"));
            setBtObexUuid(prop.getProperty("bt-obex-uuid"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void writeProperties(String configFilePath){

        this.configFilePath = configFilePath;
        setConfigFilePath(configFilePath);
        writeProperties();
    }

    public void writeProperties(){
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream(getConfigFilePath());

            // set general parameters
            prop.setProperty("proviz-ver", getProvizVer());
            prop.setProperty("device-name", getDeviceName());
            prop.setProperty("device-id", getDeviceId());
            prop.setProperty("app-name", getAppName());
            prop.setProperty("app-ver", getAppVer());
            prop.setProperty("connection-type", getConnectionType());
            prop.setProperty("static-settings", getStaticSettings());
            prop.setProperty("local-proviz-dir", getLocalProvizDir());
            prop.setProperty("remote-proviz-dir", getRemoteProvizDir());

            // hardware settings
            prop.setProperty("hw-model", getHwModel());
            prop.setProperty("hw-ver", getHwVer());
            prop.setProperty("os-version", getOsVer());

            // set wifi settings
            prop.setProperty("wifi-ssid", getWifiSsid());
            prop.setProperty("wifi-psk", getWifiPsk());
            prop.setProperty("user-name", getHostUserName());
            prop.setProperty("host-name", getHostIpAddr());
            prop.setProperty("port-number", getHostPortNumber());
            prop.setProperty("pass-key", getHostPasskey());
            prop.setProperty("know-host-file", getKnowHostFile());
            prop.setProperty("identity-file", getIdentityFile());
            prop.setProperty("wifi-encryption", getWifiEncryption());


            // if using wifi or eth http server
            prop.setProperty("server-ip", getServerIp());
            prop.setProperty("server-port", getServerPort());
            prop.setProperty("tablet-ip", getTabletIp());
            prop.setProperty("tablet-port", getTabletPort());

            // static ip settings
            prop.setProperty("static-ip", getStaticIp());
            prop.setProperty("static-netmask", getStaticNetmask());
            prop.setProperty("static-gateway", getStaticGateWay());

            // bluetooth settings
            prop.setProperty("bt-name", getBtName());
            prop.setProperty("bt-addr", getBtAddr());
            prop.setProperty("bt-ssp-uuid", getBtSppUuid());
            prop.setProperty("bt-obex-uuid", getBtObexUuid());

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getProvizVer() {
        return provizVer;
    }

    public void setProvizVer(String provizVer) {
        this.provizVer = provizVer;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getStaticSettings() {
        return staticSettings;
    }

    public void setStaticSettings(String staticSettings) {
        this.staticSettings = staticSettings;
    }

    public String getHwModel() {
        return hwModel;
    }

    public void setHwModel(String hwModel) {
        this.hwModel = hwModel;
    }

    public String getHwVer() {
        return hwVer;
    }

    public void setHwVer(String hwVer) {
        this.hwVer = hwVer;
    }

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getWifiPsk() {
        return wifiPsk;
    }

    public void setWifiPsk(String wifiPsk) {
        this.wifiPsk = wifiPsk;
    }

    public String getHostUserName() {
        return hostUserName;
    }

    public void setHostUserName(String hostUserName) {
        this.hostUserName = hostUserName;
    }

    public String getHostIpAddr() {
        return hostIpAddr;
    }

    public void setHostIpAddr(String hostIpAddr) {
        this.hostIpAddr = hostIpAddr;
    }

    public String getHostPortNumber() {
        return hostPortNumber;
    }

    public void setHostPortNumber(String hostPortNumber) {
        this.hostPortNumber = hostPortNumber;
    }

    public String getHostPasskey() {
        return hostPasskey;
    }

    public void setHostPasskey(String hostPasskey) {
        this.hostPasskey = hostPasskey;
    }

    public String getKnowHostFile() {
        return knowHostFile;
    }

    public void setKnowHostFile(String knowHostFile) {
        this.knowHostFile = knowHostFile;
    }

    public String getIdentityFile() {
        return identityFile;
    }

    public void setIdentityFile(String identityFile) {
        this.identityFile = identityFile;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getStaticIp() {
        return staticIp;
    }

    public void setStaticIp(String staticIp) {
        this.staticIp = staticIp;
    }

    public String getStaticNetmask() {
        return staticNetmask;
    }

    public void setStaticNetmask(String staticNetmask) {
        this.staticNetmask = staticNetmask;
    }

    public String getStaticGateWay() {
        return staticGateWay;
    }

    public void setStaticGateWay(String staticGateWay) {
        this.staticGateWay = staticGateWay;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getBtAddr() {
        return btAddr;
    }

    public void setBtAddr(String btAddr) {
        this.btAddr = btAddr;
    }

    public String getBtSppUuid() {
        return btSppUuid;
    }

    public void setBtSppUuid(String btSppUuid) {
        this.btSppUuid = btSppUuid;
    }

    public String getBtObexUuid() {
        return btObexUuid;
    }

    public void setBtObexUuid(String blObexUuid) {
        this.btObexUuid = blObexUuid;
    }

    public String getLocalProvizDir() {
        return localProvizDir;
    }

    public void setLocalProvizDir(String localProvizDir) {
        this.localProvizDir = localProvizDir;
    }

    public String getRemoteProvizDir() {
        return remoteProvizDir;
    }

    public void setRemoteProvizDir(String remoteProvizDir) {
        this.remoteProvizDir = remoteProvizDir;
    }

    public String getWifiEncryption() {
        return wifiEncryption;
    }

    public void setWifiEncryption(String wifiEncryption) {
        this.wifiEncryption = wifiEncryption;
    }

    public static void main(String[] args) {

        // general example of how to create a properties file
        // multiple config files can be created for different iot devices

        String user = System.getProperty("user.name");
        String configFilePath = "/home/" + user + "/.proviz/.config.properties";

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
        properties.setTabletIp(""); // IP address of tablet that http post will send to
        properties.setTabletPort(""); // Port number of tablet http post will send to

        // static ip settings
        properties.setStaticIp("");
        properties.setStaticNetmask("");
        properties.setStaticGateWay("");

        // bluetooth settings
        properties.setBtName(""); // name of bt device, not used by to find device
        properties.setBtAddr(""); // bt device mac address, used to find device
        properties.setBtSppUuid(""); // bt spp uuid, used to find service
        properties.setBtObexUuid("");

        // send settings to file
        properties.writeProperties();
        // reload new properties
        properties.loadProperties();
    }

    public String getTabletIp() {
        return tabletIp;
    }

    public void setTabletIp(String tabletIp) {
        this.tabletIp = tabletIp;
    }

    public String getTabletPort() {
        return tabletPort;
    }

    public void setTabletPort(String tabletPort) {
        this.tabletPort = tabletPort;
    }
}
