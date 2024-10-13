package com.example.agriautomationhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private List<ChatMessage> messageList;

    public MessageAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the view type based on whether the message is sent by the user
        ChatMessage message = messageList.get(position);
        if (message.isSentByUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            // Inflate user message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            // Inflate bot message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).textViewMessage.setText(message.getMessage());
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).textViewMessage.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for user messages
    public static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMessage;

        public UserMessageViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewUserMessage);  // Use user-specific TextView ID
        }
    }

    // ViewHolder for bot messages
    public static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMessage;

        public BotMessageViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewBotMessage);  // Use bot-specific TextView ID
        }
    }
}

