package com.example.eventguard.UserModule.Events;

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
import com.example.eventguard.UserModule.Events.events_details;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private List<String> userRegisteredEventIds;
    private List<String> userMarkedEventIds;

    public EventAdapter(Context context, List<Event> eventList, List<String> userRegisteredEventIds, List<String> userMarkedEventIds) {
        this.context = context;
        this.eventList = eventList;
        this.userRegisteredEventIds = userRegisteredEventIds;
        this.userMarkedEventIds = userMarkedEventIds;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.title);
        holder.tvDate.setText(event.date);
        holder.tvCategory.setText(event.category);
        holder.tvLocation.setText(event.location);
        holder.tvSeats.setText(event.currentParticipants + "/" + event.maxParticipants);

        if (event.imageUrl != null && !event.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.event_detail_banner)
                    .into(holder.ivBanner);
        }

        // Status Logic: Registered (Green) > Closed (Blue) > Full (Red) > Join (Blue)
        boolean isRegistered = userRegisteredEventIds != null && userRegisteredEventIds.contains(event.id);
        boolean isMarked = userMarkedEventIds != null && userMarkedEventIds.contains(event.id);
        boolean isFull = event.currentParticipants >= event.maxParticipants;
        
        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000;
        boolean isClosed;
        
        if ("Available".equalsIgnoreCase(event.status)) {
            isClosed = false;
        } else if ("Closed".equalsIgnoreCase(event.status)) {
            isClosed = true;
        } else if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
            isClosed = true;
        } else {
            isClosed = false;
        }

        String statusText;
        if (isMarked) {
            statusText = "Verified";
        } else if (isRegistered) {
            statusText = "Registered";
        } else if (isClosed) {
            statusText = "Closed";
        } else if (isFull) {
            statusText = "Full";
        } else {
            statusText = "Join";
        }

        holder.tvStatus.setText(statusText);

        if (statusText.equalsIgnoreCase("Verified")) {
            holder.tvStatus.setBackgroundResource(R.drawable.green_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_green_text));
        } else if (statusText.equalsIgnoreCase("Registered")) {
            holder.tvStatus.setBackgroundResource(R.drawable.green_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_green_text));
        } else if (statusText.equalsIgnoreCase("Closed")) {
            holder.tvStatus.setBackgroundResource(R.drawable.blue_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_blue_text));
        } else if (statusText.equalsIgnoreCase("Full")) {
            holder.tvStatus.setBackgroundResource(R.drawable.red_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_red_text));
        } else {
            // "Join" status
            holder.tvStatus.setBackgroundResource(R.drawable.blue_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_blue_text));
        }

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, events_details.class);
            // Passing event details to the details activity
            intent.putExtra("eventId", event.id);
            intent.putExtra("eventTitle", event.title);
            intent.putExtra("eventDate", event.date);
            intent.putExtra("eventDesc", event.description);
            intent.putExtra("eventLoc", event.location);
            intent.putExtra("eventTime", event.time != null ? event.time : "09:00 AM");
            intent.putExtra("eventTimestamp", event.eventTimestamp);
            intent.putExtra("currentParticipants", event.currentParticipants);
            intent.putExtra("maxParticipants", event.maxParticipants);
            intent.putExtra("isRegistered", isRegistered);
            intent.putExtra("isMarked", isMarked);
            intent.putExtra("imageUrl", event.imageUrl);
            intent.putExtra("status", event.status);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateList(List<Event> newList, List<String> newUserRegisteredEventIds, List<String> newUserMarkedEventIds) {
        this.eventList = newList;
        this.userRegisteredEventIds = newUserRegisteredEventIds;
        this.userMarkedEventIds = newUserMarkedEventIds;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCategory, tvStatus, tvLocation, tvSeats;
        Button btnDetails;
        ImageView ivBanner;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvSeats = itemView.findViewById(R.id.tvEventSeats);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            ivBanner = itemView.findViewById(R.id.ivEventBanner);
        }
    }
}
