package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApi {

    // Define the endpoint for GPT-4 (completion endpoint)
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })

    @POST("v1/chat/completions")
    Call<GPTResponse> getGPTResponse(@Body GPTRequest request);
}

