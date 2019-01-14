package com.myapartment1.api;

import com.myapartment1.model.paytm.Checksum;
import com.myapartment1.model.paytm.Paytm;
import retrofit2.Call;
import retrofit2.http.*;

public interface Api {


    //this is the URL of the paytm folder that we added in the server
    //make sure you are using your ip else it will not work
    //String BASE_URL = "http://192.168.43.221:8080";
    String BASE_URL = "http://10.92.242.249:8080";

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("/generateChecksum")
    Call<Checksum> getChecksum(@Body Paytm paytm);

}