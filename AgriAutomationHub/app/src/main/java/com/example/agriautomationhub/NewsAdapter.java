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
        holder.urlTextView.setText(news.getUrl());
        holder.publishedTextView.setText(news.getPublished());

        // Handle the image if available
        if (news.getMainImage() != null && !news.getMainImage().isEmpty()) {
            holder.newsImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())  // Use the context from the itemView
                    .load(news.getMainImage())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // Hide the ImageView if image loading fails
                            holder.newsImageView.setVisibility(View.GONE);
                            return true; // Indicate that we handled the error
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // Image loaded successfully, make sure ImageView is visible
                            holder.newsImageView.setVisibility(View.VISIBLE);
                            return false; // Glide will handle the display of the image
                        }
                    })
                    .into(holder.newsImageView);

        } else {
            holder.newsImageView.setVisibility(View.GONE); // Hide the image if not available
        }

        // Set an OnClickListener on the URL TextView to open it in a browser
        holder.urlTextView.setOnClickListener(view -> {
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
        TextView titleTextView, urlTextView, publishedTextView;
        ImageView newsImageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.newsTitle);
            urlTextView = itemView.findViewById(R.id.newsUrl);
            publishedTextView = itemView.findViewById(R.id.newsPublished);
            newsImageView = itemView.findViewById(R.id.newsImage);
        }
    }
}
