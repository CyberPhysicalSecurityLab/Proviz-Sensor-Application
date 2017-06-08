package coms.http.client;

import coms.http.client.models.requests.SendSensorValueRequest;
import coms.http.client.models.responses.SendSensorValueResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by bigbywolf on 1/13/17.
 */
public interface WebApis {
    @POST("/sendvalue")
    Call<SendSensorValueResponse> sendSensorData(@Body SendSensorValueRequest request);
}
