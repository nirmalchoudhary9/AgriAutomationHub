package com.example.agriautomationhub;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText inputMessage;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private OpenAIApi openAIApi;  // Declare the API interface
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize components
        db = AppDatabase.getDatabase(getApplicationContext());
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        inputMessage = findViewById(R.id.inputMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);
        Button btnNewChat = findViewById(R.id.btnNewChat);

        // Initialize Retrofit and OpenAIApi
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://chatbot-agriautomationhub.openai.azure.com/")  // Azure endpoint
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openAIApi = retrofit.create(OpenAIApi.class);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        loadChatHistory();

        btnSend.setOnClickListener(v -> sendMessage());
        btnNewChat.setOnClickListener(v -> startNewChat());
    }

    private void loadChatHistory() {
        // Load chat messages from the database
        new Thread(() -> {
            List<MessageEntity> messages = db.messageDao().getAllMessages();
            runOnUiThread(() -> {
                messageList.clear();
                for (MessageEntity message : messages) {
                    messageList.add(new ChatMessage(message.getMessage(), message.isSentByUser()));
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            });
        }).start();
    }

    private void startNewChat() {
        // Clear the chat history and reload new chat
        messageList.clear();
        messageAdapter.notifyDataSetChanged();
        // Optionally clear messages from the database or keep them for reference
    }

    private void sendMessage() {
        String userMessage = inputMessage.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            ChatMessage userChatMessage = new ChatMessage(userMessage, true);
            messageList.add(userChatMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            inputMessage.setText("");

            saveUserMessage(userMessage);
            sendMessageToGPT(userMessage);
        }
    }

    private void sendMessageToGPT(String userMessage) {
        // Create a list to hold both the system and user messages
        List<GPTRequest.Message> messages = new ArrayList<>();

        // Add a system message to define the chatbot's role and name
        messages.add(new GPTRequest.Message("system", "Your name is KrishiMitra. You are an expert agricultural assistant helping farmers with crop and farming related issues. Provide clear and concise advice."));

        // Add the user message
        GPTRequest.Message userChatMessage = new GPTRequest.Message("user", userMessage);
        messages.add(userChatMessage);

        // Create the GPT request with the defined model and messages
        GPTRequest request = new GPTRequest("gpt-35-turbo-16k", messages);

        // Call the OpenAI API for a response
        openAIApi.getGPTResponse(request).enqueue(new Callback<GPTResponse>() {
            @Override
            public void onResponse(@NonNull Call<GPTResponse> call, @NonNull Response<GPTResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extract the bot's response from the API response
                    String botResponse = response.body().getChoices().get(0).getMessage().getContent();
                    Log.d("ChatActivity", "Bot response: " + botResponse);

                    // Add the bot response to the chat message list
                    ChatMessage botChatMessage = new ChatMessage(botResponse, false);
                    messageList.add(botChatMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);

                    // Save the bot response
                    saveBotResponse(botResponse);
                } else {
                    Log.e("ChatActivity", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GPTResponse> call, @NonNull Throwable t) {
                Log.e("ChatActivity", "API call failed: " + t.getMessage());
            }
        });
    }


    private void saveUserMessage(String userMessage) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        new Thread(() -> db.messageDao().insert(new MessageEntity(userMessage, true, timestamp))).start();
    }

    private void saveBotResponse(String botResponse) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        new Thread(() -> db.messageDao().insert(new MessageEntity(botResponse, false, timestamp))).start();
    }
}
