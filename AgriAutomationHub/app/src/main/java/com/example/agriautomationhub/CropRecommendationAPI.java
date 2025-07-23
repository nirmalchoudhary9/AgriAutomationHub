package com.example.agriautomationhub;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CropRecommendationAPI {
    @POST("/score")
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer "Your-api"

    })
    Call<ResponseBody> getRecommendation(@Body RequestBody requestBody);
}
