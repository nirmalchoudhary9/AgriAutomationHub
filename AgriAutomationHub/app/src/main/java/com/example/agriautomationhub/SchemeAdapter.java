package com.example.agriautomationhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder> {

    private List<Scheme> schemeList;
    private List<Scheme> schemeListFull; // Full list for filtering

    public SchemeAdapter(List<Scheme> schemeList) {
        this.schemeList = schemeList;
        this.schemeListFull = new ArrayList<>(schemeList);
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scheme_item, parent, false);
        return new SchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        Scheme scheme = schemeList.get(position);
        holder.schemeTitle.setText(scheme.getTitle());
        holder.schemeDescription.setText(scheme.getDescription());
    }

    @Override
    public int getItemCount() {
        return schemeList.size();
    }

    public void filter(String text) {
        schemeList.clear();
        if (text.isEmpty()) {
            schemeList.addAll(schemeListFull);
        } else {
            for (Scheme scheme : schemeListFull) {
                if (scheme.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    schemeList.add(scheme);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class SchemeViewHolder extends RecyclerView.ViewHolder {
        TextView schemeTitle, schemeDescription;

        public SchemeViewHolder(@NonNull View itemView) {
            super(itemView);
            schemeTitle = itemView.findViewById(R.id.schemeTitle);
            schemeDescription = itemView.findViewById(R.id.schemeDescription);
        }
    }
}
