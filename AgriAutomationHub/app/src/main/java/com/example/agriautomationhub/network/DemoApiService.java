package com.example.agriautomationhub.network;

import com.example.agriautomationhub.network.model.DemoResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DemoApiService {
    @POST("api/v1/identification")
    Call<DemoResponse> predictJson(@Header("Api-Key") String apiKey, @Body JsonObject body);

}
