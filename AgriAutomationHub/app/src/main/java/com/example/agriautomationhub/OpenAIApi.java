package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApi {

    @Headers({
            "Content-Type: application/json",
            "api-key: 660e9a7d457142afb65dc7cb0ae049c4"  // Replace with Azure OpenAI API Key
    })
    @POST("/openai/deployments/gpt-35-turbo-16k/chat/completions?api-version=2024-08-01-preview")
    Call<GPTResponse> getGPTResponse(@Body GPTRequest request);
}


