package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsActivity extends AppCompatActivity {

    ImageView back;
    private static final String BASE_URL = "https://newsapi.org/";
    private static final String API_KEY = "6b1c157d0242456a88e119d64eecc4ec"; // Replace with your actual API key
    private static final String TAG = "NewsActivity";

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getNewsData();

        back = findViewById(R.id.back_btn_news);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getNewsData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GNewsApiService apiService = retrofit.create(GNewsApiService.class);

        String query = "agriculture OR technology OR science"; // Add more topics as needed
        String language = "hi"; // Language code for Hindi
        Log.d(TAG, "Requesting news with query: " + query + ", language: " + language + ", API Key: " + API_KEY);

        Call<GNewsResponse> call = apiService.getNewsArticles(query, language, API_KEY);

        call.enqueue(new Callback<GNewsResponse>() {
            @Override
            public void onResponse(Call<GNewsResponse> call, Response<GNewsResponse> response) {
                if (response.isSuccessful()) {
                    GNewsResponse newsResponse = response.body();
                    if (newsResponse != null) {
                        List<GNewsArticle> articles = newsResponse.getArticles();
                        if (articles != null && !articles.isEmpty()) {
                            newsAdapter = new NewsAdapter(NewsActivity.this, articles);
                            newsRecyclerView.setAdapter(newsAdapter);
                        } else {
                            Log.e(TAG, "No articles found");
                        }
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code() + " " + response.message());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<GNewsResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: ", t);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            return logoutUser();
        }
        if (id == R.id.action_settings) {
            return settings();
        }
        if (id == R.id.action_help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settings() {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
        return true;
    }
}
