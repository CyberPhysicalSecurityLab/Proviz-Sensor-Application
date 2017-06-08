package coms.http.client.models.requests;

/**
 * Created by bigbywolf on 1/13/17.
 */
public class SensorData {

    private String sensorName;
    private String sensorId;
    private String sensorUnit;
    private double sensorValue;
    private String variableName;

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String toString(){
        return sensorName + ", " + sensorId + ", " + variableName + ": " + sensorValue + " " + sensorUnit;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
