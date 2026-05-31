package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEventAdapter;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.OrganizerModule.Scanner.ScannerRegisteredEvents;
import com.example.eventguard.OrganizerModule.OrganizerProfile.organizer_profile_setting;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrganizerAnalyticsList extends AppCompatActivity {

    private EditText etSearch;
    private CardView filterSection;
    private CheckBox cbOpen, cbClosed, cbFull;
    private CheckBox cbWorkshop, cbSeminar, cbExhibition, cbHackathon;
    private android.widget.CalendarView calendarFilter;

    private RecyclerView rvAnalyticsEvents;
    private TextView tvNoEvents;
    private OrganizerEventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private DatabaseReference eventsRef;
    private String currentUserId;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_analytics_list);

        currentUserId = FirebaseAuth.getInstance().getUid();
        String databaseUrl = "https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/";
        eventsRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Events");

        initViews();
        setupRecyclerView();
        setupFilters();
        setupNavigation();
        loadEvents();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchEvents);
        filterSection = findViewById(R.id.filterSection);
        cbOpen = findViewById(R.id.cbOpen);
        cbClosed = findViewById(R.id.cbClosed);
        cbFull = findViewById(R.id.cbFull);
        cbWorkshop = findViewById(R.id.cbWorkshop);
        cbSeminar = findViewById(R.id.cbSeminar);
        cbExhibition = findViewById(R.id.cbExhibition);
        cbHackathon = findViewById(R.id.cbHackathon);
        calendarFilter = findViewById(R.id.calendarFilter);
        tvNoEvents = findViewById(R.id.tvNoEvents);
    }

    private void setupFilters() {
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);

        btnFilter.setOnClickListener(v -> {
            filterSection.setVisibility(filterSection.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
            filterSection.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        calendarFilter.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            selectedDate = months[month] + " " + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + ", " + year;
        });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();

        boolean fFull = cbFull.isChecked();
        boolean fClosed = cbClosed.isChecked();
        boolean fOpen = cbOpen.isChecked();
        boolean noStatusFilter = !fFull && !fClosed && !fOpen;

        boolean fWorkshop = cbWorkshop.isChecked();
        boolean fSeminar = cbSeminar.isChecked();
        boolean fExhibition = cbExhibition.isChecked();
        boolean fHackathon = cbHackathon.isChecked();
        boolean noCategoryFilter = !fWorkshop && !fSeminar && !fExhibition && !fHackathon;

        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000;

        filteredEvents.clear();
        for (Event event : allEvents) {
            boolean matchesSearch = event.title.toLowerCase().contains(query) ||
                    (event.location != null && event.location.toLowerCase().contains(query));

            String effectiveStatus = event.status;
            if ("Closed".equalsIgnoreCase(event.status)) {
                effectiveStatus = "Closed";
            } else if (event.currentParticipants >= event.maxParticipants) {
                effectiveStatus = "Full";
            } else if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
                effectiveStatus = "Closed";
            } else if ("Available".equalsIgnoreCase(event.status) || "Registration Open".equalsIgnoreCase(event.status)) {
                effectiveStatus = "Open";
            }

            boolean matchesStatus = noStatusFilter;
            if (!noStatusFilter) {
                if (fFull && "Full".equalsIgnoreCase(effectiveStatus)) matchesStatus = true;
                if (fClosed && "Closed".equalsIgnoreCase(effectiveStatus)) matchesStatus = true;
                if (fOpen && "Open".equalsIgnoreCase(effectiveStatus)) matchesStatus = true;
            }

            boolean matchesCategory = noCategoryFilter ||
                    (fWorkshop && "Workshop".equalsIgnoreCase(event.category)) ||
                    (fSeminar && "Seminar".equalsIgnoreCase(event.category)) ||
                    (fExhibition && "Exhibition".equalsIgnoreCase(event.category)) ||
                    (fHackathon && "Hackathon".equalsIgnoreCase(event.category));

            boolean matchesDate = selectedDate.isEmpty() || (event.date != null && event.date.contains(selectedDate));

            if (matchesSearch && matchesStatus && matchesCategory && matchesDate) {
                filteredEvents.add(event);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvAnalyticsEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvAnalyticsEvents.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        rvAnalyticsEvents = findViewById(R.id.rvAnalyticsEvents);
        rvAnalyticsEvents.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new OrganizerEventAdapter(this, filteredEvents) {
            @Override
            public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Event event = filteredEvents.get(position);
                holder.btnManage.setText("View Analytics");
                holder.btnManage.setOnClickListener(v -> {
                    Intent intent = new Intent(OrganizerAnalyticsList.this, OrganizerAnalytics.class);
                    intent.putExtra("eventId", event.id);
                    intent.putExtra("eventTitle", event.title);
                    startActivity(intent);
                });
            }
        };
        rvAnalyticsEvents.setAdapter(adapter);
    }

    private void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    if (event != null) {
                        allEvents.add(event);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navScanner).setOnClickListener(v -> startActivity(new Intent(this, ScannerRegisteredEvents.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, organizer_profile_setting.class)));
        findViewById(R.id.navDashboard).setOnClickListener(v -> startActivity(new Intent(this, OrganizerDashboard.class)));
    }
}
