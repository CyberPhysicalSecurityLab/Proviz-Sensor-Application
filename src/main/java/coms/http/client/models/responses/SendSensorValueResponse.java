package coms.http.client.models.responses;

/**
 * Created by bigbywolf on 1/13/17.
 */
public class SendSensorValueResponse {

    private OPERATION_RESULT result;

    public OPERATION_RESULT getResult() {
        return result;
    }

    public void setResult(OPERATION_RESULT result) {
        this.result = result;
    }

    public enum OPERATION_RESULT{
        SUCCESS,
        FAIL
    }
}
