package com.example.eventguard.OrganizerModule.Scanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.R;
import com.example.eventguard.models.Attendance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentScanAdapter extends RecyclerView.Adapter<RecentScanAdapter.ViewHolder> {

    private List<Attendance> attendanceList;

    public RecentScanAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        holder.tvUserName.setText(attendance.userName);
        holder.tvUserEmail.setText(attendance.userEmail);
        holder.tvEventName.setText(attendance.eventName);
        holder.tvStatus.setText(attendance.status);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(attendance.timestamp)));
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvEventName, tvStatus, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvScanUserName);
            tvUserEmail = itemView.findViewById(R.id.tvScanUserEmail);
            tvEventName = itemView.findViewById(R.id.tvScanEventName);
            tvStatus = itemView.findViewById(R.id.tvScanStatus);
            tvTime = itemView.findViewById(R.id.tvScanTime);
        }
    }
}
