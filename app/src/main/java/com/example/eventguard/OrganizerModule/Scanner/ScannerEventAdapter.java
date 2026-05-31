package com.example.eventguard.OrganizerModule.Scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ScannerEventAdapter extends RecyclerView.Adapter<ScannerEventAdapter.ViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public ScannerEventAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_registered_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvEventTitle.setText(event.title);
        holder.tvEventDate.setText(event.date);
        
        // Reuse item_registered_event but change button text
        holder.btnAction.setText("Scan Pass");

        // Set status visibility and text
        holder.tvStatus.setVisibility(View.VISIBLE);
        String status = getEffectiveStatus(event);
        holder.tvStatus.setText(status);
        
        // Dynamic status colors
        if ("Full".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_red);
        } else if ("Closed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_grey);
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_green);
        }

        if (event.imageUrl != null && !event.imageUrl.isEmpty()) {
            Glide.with(context).load(event.imageUrl).placeholder(R.drawable.img).into(holder.ivEventImage);
        } else {
            holder.ivEventImage.setImageResource(R.drawable.img);
        }

        holder.btnAction.setOnClickListener(v -> listener.onEventClick(event));
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    private String getEffectiveStatus(Event event) {
        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000;

        if ("Closed".equalsIgnoreCase(event.status)) {
            return "Closed";
        } else if (event.currentParticipants >= event.maxParticipants) {
            return "Full";
        } else if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
            return "Closed";
        } else if ("Available".equalsIgnoreCase(event.status) || "Registration Open".equalsIgnoreCase(event.status)) {
            return "Open";
        }
        return event.status != null ? event.status : "Open";
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvStatus;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnEntryPass);
        }
    }
}
