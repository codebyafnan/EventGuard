package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Scanner.ScannerRegisteredEvents;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.OrganizerModule.OrganizerProfile.organizer_profile_setting;
import com.example.eventguard.R;
import com.example.eventguard.models.Registration;
import com.example.eventguard.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerAnalytics extends AppCompatActivity {

    private TextView tvEventSubtitle, tvPercentage, tvCheckedIn, tvTotal, tvShowingCount;
    private ProgressBar progressCircle, progressHorizontal;
    private RecyclerView rvAttendees;
    private AttendeeAdapter adapter;
    private List<Registration> allRegistrations = new ArrayList<>();
    private List<Registration> filteredRegistrations = new ArrayList<>();
    private Map<String, String> userNamesMap = new HashMap<>();
    private String eventId;
    private DatabaseReference registrationsRef;
    private EditText etSearch;
    private ImageButton btnToggleList;
    private boolean showOnlyCheckedIn = false; // Default: show all for better overview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_analytics);

        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        tvEventSubtitle = findViewById(R.id.tvEventSubtitle);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvCheckedIn = findViewById(R.id.tvCheckedIn);
        tvTotal = findViewById(R.id.tvTotal);
        tvShowingCount = findViewById(R.id.tvShowingCount);
        progressCircle = findViewById(R.id.progressCircle);
        progressHorizontal = findViewById(R.id.progressHorizontal);
        etSearch = findViewById(R.id.etSearchAttendees);
        btnToggleList = findViewById(R.id.btnToggleList);

        if (eventTitle != null) {
            tvEventSubtitle.setText("Real-time oversight for " + eventTitle);
        }

        String databaseUrl = "https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/";
        registrationsRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Registrations");

        setupAttendeeList();
        setupFilters();
        setupNavigation();
        loadInitialData();
    }

    private void setupAttendeeList() {
        rvAttendees = findViewById(R.id.rvAttendees);
        rvAttendees.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendeeAdapter(filteredRegistrations);
        rvAttendees.setAdapter(adapter);
    }

    private void setupFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyAttendeeFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnToggleList.setOnClickListener(v -> {
            showOnlyCheckedIn = !showOnlyCheckedIn;
            if (showOnlyCheckedIn) {
                btnToggleList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_blue_light)));
            } else {
                btnToggleList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.LTGRAY));
            }
            applyAttendeeFilters();
        });
    }

    private void applyAttendeeFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredRegistrations.clear();

        for (Registration reg : allRegistrations) {
            boolean matchesToggle = !showOnlyCheckedIn || reg.isAttendanceMarked;
            String userName = userNamesMap.get(reg.userId);
            
            boolean matchesSearch = query.isEmpty() || 
                    (reg.registrationId != null && reg.registrationId.toLowerCase().contains(query)) ||
                    (reg.userId != null && reg.userId.toLowerCase().contains(query)) ||
                    (userName != null && userName.toLowerCase().contains(query));

            if (matchesToggle && matchesSearch) {
                filteredRegistrations.add(reg);
            }
        }
        adapter.notifyDataSetChanged();
        tvShowingCount.setText("Showing " + filteredRegistrations.size() + " of " + allRegistrations.size() + " entries");
    }

    private void loadInitialData() {
        // First load all user names to enable searching by name
        FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userNamesMap.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User user = ds.getValue(User.class);
                            if (user != null) {
                                userNamesMap.put(ds.getKey(), user.name);
                            }
                        }
                        adapter.setUserNames(userNamesMap);
                        loadRegistrations();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadRegistrations() {
        if (eventId == null) return;

        registrationsRef.orderByChild("eventId").equalTo(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allRegistrations.clear();
                int checkedInCount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Registration reg = ds.getValue(Registration.class);
                    if (reg != null) {
                        allRegistrations.add(reg);
                        if (reg.isAttendanceMarked) checkedInCount++;
                    }
                }
                applyAttendeeFilters();
                updateUI(checkedInCount, allRegistrations.size());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUI(int checkedIn, int total) {
        tvCheckedIn.setText(checkedIn + " Checked In");
        tvTotal.setText(total + " Total");

        if (total > 0) {
            int percent = (checkedIn * 100) / total;
            tvPercentage.setText(percent + "%");
            progressCircle.setProgress(percent);
            progressHorizontal.setProgress(percent);
        } else {
            tvPercentage.setText("0%");
            progressCircle.setProgress(0);
            progressHorizontal.setProgress(0);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEvents.class));
            finish();
        });
        findViewById(R.id.navScanner).setOnClickListener(v -> {
            startActivity(new Intent(this, ScannerRegisteredEvents.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, organizer_profile_setting.class));
            finish();
        });
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerDashboard.class));
            finish();
        });
    }
}
