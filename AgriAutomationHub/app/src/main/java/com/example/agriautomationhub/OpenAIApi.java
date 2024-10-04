package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApi {

    // Define the endpoint for GPT-4 (completion endpoint)
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer <--OpenAi-API-->"
    })

    @POST("v1/chat/completions")
    Call<GPTResponse> getGPTResponse(@Body GPTRequest request);
}

