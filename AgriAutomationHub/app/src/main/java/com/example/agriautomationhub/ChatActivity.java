package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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

    // Define the welcome message
    private static final String WELCOME_MESSAGE = "Welcome to KrishiMitra! How can I assist you with your agricultural queries today?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize components
        db = AppDatabase.getDatabase(getApplicationContext());
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        inputMessage = findViewById(R.id.inputMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);
        ImageView back = findViewById(R.id.back_btn_chat);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize Retrofit and OpenAIApi
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openai-service-agri.openai.azure.com/")  // Azure endpoint
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openAIApi = retrofit.create(OpenAIApi.class);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        loadChatHistory();

        btnSend.setOnClickListener(v -> sendMessage());
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

                // If the chat history is empty, show the welcome message
                if (messageList.isEmpty()) {
                    messageList.add(new ChatMessage(WELCOME_MESSAGE, false)); // Add the welcome message
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
            });
        }).start();
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new_chat) {
            startNewChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewChat() {
        // Clear the chat history from the database
        new Thread(() -> {
            db.messageDao().deleteAllMessages(); // Call to delete all messages from the database
            runOnUiThread(() -> {
                // Clear the message list in the UI
                messageList.clear();
                messageAdapter.notifyDataSetChanged();

                // Add the welcome message for the new chat
                messageList.add(new ChatMessage(WELCOME_MESSAGE, false));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            });
        }).start();
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
