package com.example.eventguard.OrganizerModule.OrganizerEvents;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Scanner.OrganizerScanner;
import com.example.eventguard.OrganizerModule.Scanner.ScannerRegisteredEvents;
import com.example.eventguard.OrganizerModule.Dashboard.CreateEventActivity;
import com.example.eventguard.OrganizerModule.Dashboard.OrganizerDashboard;
import com.example.eventguard.OrganizerModule.OrganizerProfile.organizer_profile_setting;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEvents extends AppCompatActivity {

    private EditText etSearch;
    private CardView filterSection;
    private CheckBox cbOpen, cbClosed, cbFull;
    private CheckBox cbWorkshop, cbSeminar, cbExhibition, cbHackathon;
    private android.widget.CalendarView calendarFilter;

    private RecyclerView rvEvents;
    private TextView tvNoEvents;
    private FloatingActionButton fabAddEvent;
    private OrganizerEventAdapter eventAdapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    private DatabaseReference eventsRef;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events");

        etSearch = findViewById(R.id.etSearch);
        filterSection = findViewById(R.id.filterSection);
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);

        cbOpen = findViewById(R.id.cbOpen);
        cbClosed = findViewById(R.id.cbClosed);
        cbFull = findViewById(R.id.cbFull);

        cbWorkshop = findViewById(R.id.cbWorkshop);
        cbSeminar = findViewById(R.id.cbSeminar);
        cbExhibition = findViewById(R.id.cbExhibition);
        cbHackathon = findViewById(R.id.cbHackathon);

        calendarFilter = findViewById(R.id.calendarFilter);
        rvEvents = findViewById(R.id.rvEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new OrganizerEventAdapter(this, filteredEvents);
        rvEvents.setAdapter(eventAdapter);

        btnFilter.setOnClickListener(v -> {
            if (filterSection.getVisibility() == View.VISIBLE) {
                filterSection.setVisibility(View.GONE);
            } else {
                filterSection.setVisibility(View.VISIBLE);
            }
        });

        btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
            filterSection.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEvents.this, CreateEventActivity.class);
            intent.putExtra("isUpdate", false);
            startActivity(intent);
        });

        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(OrganizerEvents.this, OrganizerEvents.class)));
        findViewById(R.id.navDashboard).setOnClickListener(v -> startActivity(new Intent(OrganizerEvents.this, OrganizerDashboard.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(OrganizerEvents.this, organizer_profile_setting.class)));
        findViewById(R.id.navScanner).setOnClickListener(v -> {
            startActivity(new Intent(OrganizerEvents.this, ScannerRegisteredEvents.class));
        });

        calendarFilter.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            selectedDate = months[month] + " " + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + ", " + year;
        });

        fetchEvents();
    }

    private void fetchEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Event event = postSnapshot.getValue(Event.class);
                    if (event != null) {
                        allEvents.add(event);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrganizerEvents.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
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

            // Determine effective status based on time and capacity
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
        eventAdapter.updateList(filteredEvents);

        if (filteredEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }
}
