package com.example.agriautomationhub;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MandiAdapter extends RecyclerView.Adapter<MandiAdapter.MandiViewHolder> {
    List<MandiData> mandiList;

    public MandiAdapter(List<MandiData> mandiList) {
        this.mandiList = mandiList;
    }

    @NonNull
    @Override
    public MandiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mandi_item_card, parent, false);
        return new MandiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MandiViewHolder holder, int position) {
        MandiData data = mandiList.get(position);
        holder.tvMarket.setText(data.market);
        holder.tvCommodity.setText(data.commodity);
        holder.tvMinPrice.setText("₹" + data.minPrice);
        holder.tvMaxPrice.setText("₹" + data.maxPrice);
        holder.tvDate.setText(data.priceDate);
    }

    @Override
    public int getItemCount() {
        return mandiList.size();
    }

    static class MandiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMarket, tvCommodity, tvMinPrice, tvMaxPrice, tvDate;

        public MandiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMarket = itemView.findViewById(R.id.tvMarket);
            tvCommodity = itemView.findViewById(R.id.tvCommodity);
            tvMinPrice = itemView.findViewById(R.id.tvMinPrice);
            tvMaxPrice = itemView.findViewById(R.id.tvMaxPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
