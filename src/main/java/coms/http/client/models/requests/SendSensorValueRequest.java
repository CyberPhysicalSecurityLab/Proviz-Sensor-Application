package coms.http.client.models.requests;

import java.util.ArrayList;

/**
 * Created by bigbywolf on 1/13/17.
 */
public class SendSensorValueRequest {

    private String deviceName;
    private String deviceId;
    private ArrayList<SensorData> sensors;
    private String dateTime;

    public SendSensorValueRequest(){

    }

    public SendSensorValueRequest(String deviceName, String deviceId, ArrayList<SensorData> sensor, String dateTime){
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.sensors = sensor;
        this.dateTime = dateTime;
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

    public ArrayList<SensorData> getSensors() {
        return sensors;
    }

    public void setSensors(ArrayList<SensorData> sensors) {
        this.sensors = sensors;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String toString(){
        String str = "";
        str += deviceName + ", " + deviceId + " ";
        for (SensorData sen : sensors) {
            str += sen + ", ";
        }

        str += dateTime;
        return str;
    }
}

