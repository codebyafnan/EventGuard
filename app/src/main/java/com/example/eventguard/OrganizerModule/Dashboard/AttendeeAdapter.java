package com.example.eventguard.OrganizerModule.Dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.R;
import com.example.eventguard.models.Registration;
import com.example.eventguard.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {

    private List<Registration> registrations;
    private Map<String, String> userNames = new HashMap<>();

    public AttendeeAdapter(List<Registration> registrations) {
        this.registrations = registrations;
    }

    public void setUserNames(Map<String, String> userNames) {
        this.userNames = userNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organizer_attendee, parent, false);
        return new AttendeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        Registration reg = registrations.get(position);
        
        // Fetch user name from map or Firebase
        String name = userNames.get(reg.userId);
        if (name != null) {
            holder.tvName.setText(name);
        } else {
            holder.tvName.setText("Loading...");
            FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users").child(reg.userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                userNames.put(reg.userId, user.name);
                                holder.tvName.setText(user.name);
                            } else {
                                holder.tvName.setText("Unknown User");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        String ticketId = reg.registrationId != null ? 
                (reg.registrationId.length() > 6 ? reg.registrationId.substring(0, 6).toUpperCase() : reg.registrationId.toUpperCase()) 
                : "N/A";
        holder.tvTicketId.setText("#" + ticketId);
        
        if (reg.isAttendanceMarked) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(reg.registrationTime))); 
            holder.tvStatus.setText("Present");
            holder.tvStatus.setBackgroundResource(R.drawable.green_badge);
        } else {
            holder.tvTime.setText("-");
            holder.tvStatus.setText("Absent");
            holder.tvStatus.setBackgroundResource(R.drawable.red_badge);
        }
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    public static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTicketId, tvTime, tvStatus;

        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAttendeeName);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvTime = itemView.findViewById(R.id.tvCheckInTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}