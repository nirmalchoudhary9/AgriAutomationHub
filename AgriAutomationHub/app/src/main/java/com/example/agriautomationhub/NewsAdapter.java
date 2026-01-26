package com.example.agriautomationhub;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import android.content.Intent;
import android.net.Uri;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsThread> newsList;

    public NewsAdapter(List<NewsThread> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsThread news = newsList.get(position);
        holder.titleTextView.setText(news.getTitle());
        holder.sourceTextView.setText(news.getSiteFull());
        
        // Simple date formatting or truncation if needed
        String pub = news.getPublished();
        if (pub != null && pub.length() > 10) pub = pub.substring(0, 10);
        holder.publishedTextView.setText("• " + pub);

        // Handle the image if available
        if (news.getMainImage() != null && !news.getMainImage().isEmpty()) {
            holder.newsImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(news.getMainImage())
                    .placeholder(R.drawable.rounded_item_selected) // Use a placeholder
                    .error(R.drawable.rounded_item_selected) // Fallback
                    .into(holder.newsImageView);
        } else {
            holder.newsImageView.setVisibility(View.GONE);
        }

        // Set OnClickListener on the entire CardView
        holder.itemView.setOnClickListener(view -> {
            String url = news.getUrl();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, sourceTextView, publishedTextView;
        ImageView newsImageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.newsTitle);
            sourceTextView = itemView.findViewById(R.id.newsSource);
            publishedTextView = itemView.findViewById(R.id.newsPublished);
            newsImageView = itemView.findViewById(R.id.newsImage);
        }
    }
}
