package com.example.eventguard.OrganizerModule.OrganizerEvents;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;

import java.util.List;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.AdminEventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public OrganizerEventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_organizer_item_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.title);
        holder.tvDate.setText(event.date);
        holder.tvCategory.setText(event.category);
        holder.tvLocation.setText(event.location);
        holder.tvSeats.setText(event.currentParticipants + "/" + event.maxParticipants);

        // Determine effective status based on time and capacity
        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000;
        String displayStatus = event.status;

        if ("Closed".equalsIgnoreCase(event.status)) {
            displayStatus = "Closed";
        } else if (event.currentParticipants >= event.maxParticipants) {
            displayStatus = "Full";
        } else if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
            displayStatus = "Closed";
        } else if ("Registration Open".equalsIgnoreCase(event.status) || "Available".equalsIgnoreCase(event.status)) {
            displayStatus = "Open";
        }

        holder.tvStatus.setText(displayStatus);

        if (event.imageUrl != null && !event.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.event_detail_banner)
                    .into(holder.ivBanner);
        }

        // Styling status based on text
        if ("Full".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.red_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_red_text));
        } else if ("Closed".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.blue_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_blue_text));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.green_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_green_text));
        }

        holder.btnManage.setOnClickListener(v -> {
            Intent intent = new Intent(context, organizer_events_details.class);
            intent.putExtra("eventId", event.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCategory, tvStatus, tvLocation, tvSeats;
        Button btnManage;
        ImageView ivBanner;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvSeats = itemView.findViewById(R.id.tvEventSeats);
            btnManage = itemView.findViewById(R.id.btnDetails);
            ivBanner = itemView.findViewById(R.id.ivEventBanner);
        }
    }
}
