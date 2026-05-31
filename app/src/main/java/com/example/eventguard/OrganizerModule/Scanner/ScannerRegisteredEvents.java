package com.example.eventguard.OrganizerModule.Scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Dashboard.OrganizerDashboard;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.R;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.example.eventguard.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ScannerRegisteredEvents extends AppCompatActivity {

    private RecyclerView rvEventsToScan;
    private ScannerEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private DatabaseReference eventsRef;
    private FirebaseAuth auth;
    private TextView tvNoEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_registered_events);

        auth = FirebaseAuth.getInstance();
        String databaseUrl = "https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/";
        eventsRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Events");

        rvEventsToScan = findViewById(R.id.rvEventsToScan);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        rvEventsToScan.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ScannerEventAdapter(this, eventList, event -> {
            Intent intent = new Intent(ScannerRegisteredEvents.this, OrganizerScanner.class);
            intent.putExtra("eventId", event.id);
            intent.putExtra("eventName", event.title);
            startActivity(intent);
        });
        rvEventsToScan.setAdapter(adapter);

        setupNavigation();
        fetchOrganizerEvents();
    }

    private void fetchOrganizerEvents() {
        if (auth.getCurrentUser() == null) return;
        String organizerId = auth.getCurrentUser().getUid();

        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Event event = postSnapshot.getValue(Event.class);
                    // Filter events by organizer if needed, assuming any organizer can see all for now or if event has organizerId
                    // For this project, let's assume we show all events for simplicity unless Event model has organizerId
                    if (event != null) {
                        eventList.add(event);
                    }
                }
                
                if (eventList.isEmpty()) {
                    tvNoEvents.setVisibility(View.VISIBLE);
                } else {
                    tvNoEvents.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScannerRegisteredEvents.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navDashboard).setOnClickListener(v -> startActivity(new Intent(this, OrganizerDashboard.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, profile_setting.class)));
        // navScanner is current
    }
}
