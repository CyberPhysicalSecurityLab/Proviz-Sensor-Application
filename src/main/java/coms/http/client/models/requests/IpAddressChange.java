package coms.http.client.models.requests;

/**
 * Created by bigbywolf on 3/15/17.
 */
public class IpAddressChange {


    private String deviceName;
    private String deviceId;
    private String dateTime;
    private String oldAddress;
    private String newAddress;

    public IpAddressChange(String deviceName, String deviceId, String dateTime, String oldAddress, String newAddress){

        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.dateTime = dateTime;
        this.oldAddress = oldAddress;
        this.newAddress = newAddress;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getOldAddress() {
        return oldAddress;
    }

    public void setOldAddress(String oldAddress) {
        this.oldAddress = oldAddress;
    }

    public String getNewAddress() {
        return newAddress;
    }

    public void setNewAddress(String newAddress) {
        this.newAddress = newAddress;
    }
}
