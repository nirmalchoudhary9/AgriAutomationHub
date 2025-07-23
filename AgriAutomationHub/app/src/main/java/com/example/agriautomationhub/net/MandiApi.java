package com.example.agriautomationhub.net;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class MandiApi {

    private static final String END_POINT = "https://eanugya.mp.gov.in/Anugya_e/frontData.asmx/getAllData";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 2_000;

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .callTimeout(70, TimeUnit.SECONDS)
                    .build();

    /** dd/MM/yyyy  →  dd-MM-yyyy */
    public static String convertDate(String src) {
        return src.replace('/', '-');
    }

    /**
     * Fire the request, retrying up to 3× on any network exception.
     *
     * @param reportDate  dd/MM/yyyy (from your TextView)
     * @param distCode    e.g. "2320"
     * @param mandiCode   e.g. "20088"
     * @param commGroup   e.g. "3"
     * @param commCode    e.g. "13"
     * @param callback    OkHttp callback (runs on background thread)
     */
    public void fetchMandiData(
            @NonNull String reportDate,
            @NonNull String distCode,
            @NonNull String mandiCode,
            @NonNull String commGroup,
            @NonNull String commCode,
            @NonNull Callback callback) {

        try {
            JSONObject payload = new JSONObject()
                    .put("date", reportDate)
                    .put("distCode",   distCode)
                    .put("mandiCode",  mandiCode)
                    .put("commGroupCode", commGroup)
                    .put("commCode",   commCode);

            RequestBody body = RequestBody.create(payload.toString(), JSON);
            Request request  = new Request.Builder()
                    .url(END_POINT)
                    .post(body)
                    .build();

            executeWithRetry(request, MAX_RETRY, callback);

        } catch (Exception e) {
            // Marshal JSON errors straight to onFailure for consistency
            callback.onFailure(null, new IOException("JSON build failed", e));
        }
    }

    /** Recursive retry helper */
    private void executeWithRetry(
            Request request, int remaining,
            Callback userCallback) {

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                if (remaining > 1) {
                    new Handler(Looper.getMainLooper())
                            .postDelayed(
                                    () -> executeWithRetry(request,
                                            remaining - 1,
                                            userCallback),
                                    RETRY_DELAY_MS);
                } else {
                    userCallback.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                userCallback.onResponse(call, response);
            }
        });
    }
}

