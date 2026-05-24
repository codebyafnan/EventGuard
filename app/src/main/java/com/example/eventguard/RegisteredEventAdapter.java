package com.example.eventguard;

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

import java.util.List;

public class RegisteredEventAdapter extends RecyclerView.Adapter<RegisteredEventAdapter.ViewHolder> {

    private Context context;
    private List<Registration> registrationList;

    public RegisteredEventAdapter(Context context, List<Registration> registrationList) {
        this.context = context;
        this.registrationList = registrationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_registered_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registration reg = registrationList.get(position);
        holder.tvTitle.setText(reg.eventTitle);
        holder.tvDate.setText(reg.eventDate);

        holder.btnEntryPass.setOnClickListener(v -> {
            Intent intent = new Intent(context, qr_pass.class);
            intent.putExtra("registrationId", reg.registrationId);
            intent.putExtra("eventTitle", reg.eventTitle);
            intent.putExtra("eventDate", reg.eventDate);
            intent.putExtra("eventTimestamp", reg.eventTimestamp);
            intent.putExtra("isMarked", reg.isAttendanceMarked);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return registrationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        Button btnEntryPass;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            btnEntryPass = itemView.findViewById(R.id.btnEntryPass);
            ivImage = itemView.findViewById(R.id.ivEventImage);
        }
    }
}
