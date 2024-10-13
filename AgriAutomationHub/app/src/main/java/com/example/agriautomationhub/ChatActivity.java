//package com.example.agriautomationhub;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ChatActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerViewMessages;
//    private EditText inputMessage;
//    private MessageAdapter messageAdapter;
//    private List<ChatMessage> messageList;
//    private OpenAIApi openAIApi;  // Declare the API interface
//    private AppDatabase db;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//
//        // Initialize components
//        db = AppDatabase.getDatabase(getApplicationContext());
//        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
//        inputMessage = findViewById(R.id.inputMessage);
//        ImageButton btnSend = findViewById(R.id.btnSend);
//        Button btnNewChat = findViewById(R.id.btnNewChat);
//
//        // Initialize Retrofit and OpenAIApi
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.openai.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        openAIApi = retrofit.create(OpenAIApi.class);  // Initialize here
//
//        messageList = new ArrayList<>();
//        messageAdapter = new MessageAdapter(messageList);
//        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
//        recyclerViewMessages.setAdapter(messageAdapter);
//
//        loadChatHistory();
//
//        btnSend.setOnClickListener(v -> sendMessage());
//        btnNewChat.setOnClickListener(v -> startNewChat());
//    }
//
//    private void loadChatHistory() {
//        // Load chat messages from the database
//        new Thread(() -> {
//            List<MessageEntity> messages = db.messageDao().getAllMessages();
//            runOnUiThread(() -> {
//                messageList.clear();
//                for (MessageEntity message : messages) {
//                    messageList.add(new ChatMessage(message.getMessage(), message.isSentByUser()));
//                }
//                messageAdapter.notifyDataSetChanged();
//                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
//            });
//        }).start();
//    }
//
//    private void sendMessage() {
//        String userMessage = inputMessage.getText().toString().trim();
//        if (!userMessage.isEmpty()) {
//            messageList.add(new ChatMessage(userMessage, true));
//            messageAdapter.notifyItemInserted(messageList.size() - 1);
//            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
//            inputMessage.setText("");
//
//            saveUserMessage(userMessage);
//            sendMessageToGPT(userMessage);
//        }
//    }
//
//    private void startNewChat() {
//        // Clear the chat history and reload new chat
//        messageList.clear();
//        messageAdapter.notifyDataSetChanged();
//        // Optionally clear messages from the database or keep them for reference
//    }
//
//    private void sendMessageToGPT(String userMessage) {
//        // Create a GPT request message
//        GPTRequest.Message message = new GPTRequest.Message("user", userMessage);
//        GPTRequest request = new GPTRequest("gpt-3.5-turbo", Collections.singletonList(message));
//
//        // Make the API call
//        openAIApi.getGPTResponse(request).enqueue(new Callback<GPTResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<GPTResponse> call, @NonNull Response<GPTResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String botResponse = response.body().getChoices().get(0).getMessage().getContent();
//                    Log.d("ChatActivity", "Bot response: " + botResponse);
//                    messageList.add(new ChatMessage(botResponse, false));
//                    messageAdapter.notifyItemInserted(messageList.size() - 1);
//                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
//                    saveBotResponse(botResponse); // Save bot response
//                } else {
//                    String errorMessage = "Unknown error";
//                    try {
//                        if (response.errorBody() != null) {
//                            errorMessage = response.errorBody().string();
//                        }
//                    } catch (IOException e) {
//                        Log.e("ChatActivity", "Error reading error body: " + e.getMessage());
//                    }
//                    Log.e("ChatActivity", "Response not successful: " + response.code() + " - " + errorMessage);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<GPTResponse> call, @NonNull Throwable t) {
//                Log.e("ChatActivity", "API call failed: " + t.getMessage());
//            }
//        });
//    }
//
//    private void saveUserMessage(String userMessage) {
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        new Thread(() -> db.messageDao().insert(new MessageEntity(userMessage, true, timestamp))).start();
//    }
//
//    private void saveBotResponse(String botResponse) {
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        new Thread(() -> db.messageDao().insert(new MessageEntity(botResponse, false, timestamp))).start();
//    }
//}
