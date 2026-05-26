package com.example.eventguard.events;

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

import com.example.eventguard.R;
import com.example.eventguard.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private List<String> userRegisteredEventIds;

    public EventAdapter(Context context, List<Event> eventList, List<String> userRegisteredEventIds) {
        this.context = context;
        this.eventList = eventList;
        this.userRegisteredEventIds = userRegisteredEventIds;
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

        // Status Logic: Registered (Green) > Full (Red) > Join (Blue)
        boolean isRegistered = userRegisteredEventIds != null && userRegisteredEventIds.contains(event.id);
        boolean isFull = event.currentParticipants >= event.maxParticipants;

        String statusText;
        if (isRegistered) {
            statusText = "Registered";
        } else if (isFull) {
            statusText = "Full";
        } else {
            statusText = "Join";
        }

        holder.tvStatus.setText(statusText);

        if (statusText.equalsIgnoreCase("Registered")) {
            holder.tvStatus.setBackgroundResource(R.drawable.green_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        } else if (statusText.equalsIgnoreCase("Full")) {
            holder.tvStatus.setBackgroundResource(R.drawable.red_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            // "Join" status
            holder.tvStatus.setBackgroundResource(R.drawable.blue_badge);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
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
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateList(List<Event> newList, List<String> newUserRegisteredEventIds) {
        this.eventList = newList;
        this.userRegisteredEventIds = newUserRegisteredEventIds;
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
