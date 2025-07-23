package com.example.agriautomationhub.network;

import com.example.agriautomationhub.network.model.DemoResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface DemoDiseaseService {
    @Multipart
    @POST("predict")
    Call<DemoResponse> predict(
            @Header("x-api-key") String apiKey,
            @Part MultipartBody.Part image
    );
}
