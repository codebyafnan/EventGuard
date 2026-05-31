package com.example.eventguard.OrganizerModule.OrganizerEvents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventguard.OrganizerModule.Dashboard.CreateEventActivity;
import com.example.eventguard.OrganizerModule.Dashboard.OrganizerDashboard;
import com.example.eventguard.OrganizerModule.Scanner.OrganizerScanner;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class organizer_events_details extends AppCompatActivity {

    private String eventId;
    private DatabaseReference eventRef;
    private TextView tvTitle, tvDesc, tvLocation, tvTime, tvDate, tvCategory, tvCapacity, tvStatus;
    private ImageView ivBanner;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_events_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events").child(eventId);

        initViews();
        fetchEventDetails();

        findViewById(R.id.btnUpdateEvent).setOnClickListener(v -> {
            Intent intent = new Intent(organizer_events_details.this, CreateEventActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("isUpdate", true);
            startActivity(intent);
        });

        findViewById(R.id.btnDeleteEvent).setOnClickListener(v -> showDeleteConfirmation());

        // Navigation
        findViewById(R.id.navEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEvents.class));
            finish();
        });
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerDashboard.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, profile_setting.class));
            finish();
        });
        findViewById(R.id.navScanner).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerScanner.class));
            finish();
        });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvTime = findViewById(R.id.tvDetailTime);
        tvDate = findViewById(R.id.tvDetailDate);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvCapacity = findViewById(R.id.tvDetailCapacity);
        tvStatus = findViewById(R.id.tvDetailStatus);
        ivBanner = findViewById(R.id.ivDetailBanner);
    }

    private void fetchEventDetails() {
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentEvent = snapshot.getValue(Event.class);
                if (currentEvent != null) {
                    tvTitle.setText(currentEvent.title);
                    tvDesc.setText(currentEvent.description);
                    tvLocation.setText(currentEvent.location);
                    tvTime.setText(currentEvent.time);
                    tvDate.setText(currentEvent.date);
                    tvCategory.setText(currentEvent.category);
                    tvCapacity.setText(currentEvent.currentParticipants + " / " + currentEvent.maxParticipants);
                    tvStatus.setText(currentEvent.status);

                    if (currentEvent.imageUrl != null && !currentEvent.imageUrl.isEmpty()) {
                        Glide.with(organizer_events_details.this)
                                .load(currentEvent.imageUrl)
                                .placeholder(R.drawable.event_detail_banner)
                                .into(ivBanner);
                    }

                    if ("Full".equalsIgnoreCase(currentEvent.status)) {
                        tvStatus.setBackgroundResource(R.drawable.red_badge);
                    } else if ("Tentative".equalsIgnoreCase(currentEvent.status)) {
                        tvStatus.setBackgroundResource(R.drawable.blue_badge);
                    } else {
                        tvStatus.setBackgroundResource(R.drawable.green_badge);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(organizer_events_details.this, "Failed to load event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        eventRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(organizer_events_details.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(organizer_events_details.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
