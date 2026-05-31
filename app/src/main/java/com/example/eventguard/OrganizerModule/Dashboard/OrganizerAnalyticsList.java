package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Scanner.OrganizerScanner;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.example.eventguard.R;

public class OrganizerAnalyticsList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_analytics_list);

        setupRecyclerView();
        setupNavigation();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvAnalyticsEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Adapter for listing events with "View Analysis" buttons
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navScanner).setOnClickListener(v -> startActivity(new Intent(this, OrganizerScanner.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, profile_setting.class)));
    }
}
