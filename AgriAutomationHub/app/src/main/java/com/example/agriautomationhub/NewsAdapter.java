package com.example.agriautomationhub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriautomationhub.GNewsArticle;
import com.example.agriautomationhub.R;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<GNewsArticle> articles;
    private final Map<String, Bitmap> imageCache = new HashMap<>();

    public NewsAdapter(Context context, List<GNewsArticle> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        GNewsArticle article = articles.get(position);
        holder.newsTitle.setText(article.getTitle());
        holder.newsDescription.setText(article.getDescription());

        String imageUrl = article.getImage();
        holder.newsImage.setTag(imageUrl);

        // Check cache first
        if (imageCache.containsKey(imageUrl)) {
            holder.newsImage.setImageBitmap(imageCache.get(imageUrl));
            holder.newsImage.setVisibility(View.VISIBLE);
        } else {
            holder.newsImage.setVisibility(View.GONE);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                new ImageLoadTask(holder.newsImage, imageUrl).execute(imageUrl);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        ImageView newsImage;
        TextView newsTitle;
        TextView newsDescription;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsDescription = itemView.findViewById(R.id.newsDescription);
        }
    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private final String imageUrl;

        public ImageLoadTask(ImageView imageView, String imageUrl) {
            this.imageView = imageView;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream input = new URL(urlDisplay).openStream();
                bitmap = BitmapFactory.decodeStream(input);
                // Cache the loaded bitmap
                if (bitmap != null) {
                    imageCache.put(imageUrl, bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Only set the image if the tag matches the expected URL
            if (imageView.getTag() != null && imageView.getTag().equals(imageUrl)) {
                if (result != null) {
                    imageView.setImageBitmap(result);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        }
    }
}
