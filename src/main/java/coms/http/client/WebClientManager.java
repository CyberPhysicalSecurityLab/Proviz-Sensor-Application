package coms.http.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by bigbywolf on 1/13/17.
 */
public class WebClientManager {
    private Retrofit retrofit;
    private WebApis webApis;
    private  WebClientManager webClientManager;



    private String destinationAddress;

    public WebClientManager(String destinationAddress)
    {
        Gson gson = new GsonBuilder().create();
//        retrofit = new Retrofit.Builder().baseUrl("http://192.234.32.32:5858/").addConverterFactory(GsonConverterFactory.create(gson)).build();
        retrofit = new Retrofit.Builder().baseUrl(destinationAddress).addConverterFactory(GsonConverterFactory.create(gson)).build();
        setWebApis(retrofit.create(WebApis.class));
    }




    public WebApis getWebApis() {
        return webApis;
    }

    public void setWebApis(WebApis webApis) {
        this.webApis = webApis;
    }


}
