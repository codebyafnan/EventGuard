package com.example.eventguard.events;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;

import com.example.eventguard.Dashboards.UserDashboard;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.example.eventguard.models.Registration;
import com.example.eventguard.Dashboards.profile_setting;
import com.example.eventguard.tickets.registered_events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class events extends AppCompatActivity {

    private EditText etSearch;
    private CardView filterSection;
    private CheckBox cbRegistered, cbJoin, cbFull;
    private CheckBox cbWorkshop, cbSeminar, cbExhibition, cbHackathon;
    private android.widget.CalendarView calendarFilter;
    
    private RecyclerView rvEvents;
    private TextView tvNoEvents;
    private EventAdapter eventAdapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    
    private DatabaseReference eventsRef, registrationsRef;
    private String selectedDate = "";
    private List<String> userRegisteredEventIds = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events");
        registrationsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registrations");
        
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        etSearch = findViewById(R.id.etSearch);
        filterSection = findViewById(R.id.filterSection);
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);

        cbRegistered = findViewById(R.id.cbRegistered);
        cbJoin = findViewById(R.id.cbJoin);
        cbFull = findViewById(R.id.cbFull);

        cbWorkshop = findViewById(R.id.cbWorkshop);
        cbSeminar = findViewById(R.id.cbSeminar);
        cbExhibition = findViewById(R.id.cbExhibition);
        cbHackathon = findViewById(R.id.cbHackathon);

        calendarFilter = findViewById(R.id.calendarFilter);
        rvEvents = findViewById(R.id.rvEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        
        eventAdapter = new EventAdapter(this, filteredEvents, userRegisteredEventIds);
        rvEvents.setAdapter(eventAdapter);

        ImageView btndashboard = findViewById(R.id.btndashboard);
        ImageView btnticket = findViewById(R.id.btnticket);
        ImageView btnprofile = findViewById(R.id.btnprofile);
        ImageView btnmain = findViewById(R.id.btnmain);

        fetchEvents();
        fetchUserRegistrations();

        // Search logic
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

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSection.setVisibility(filterSection.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilters();
                filterSection.setVisibility(View.GONE);
                Toast.makeText(events.this, "Filters Applied", Toast.LENGTH_SHORT).show();
            }
        });

        btnmain.setOnClickListener(v -> startActivity(new Intent(events.this, events.class)));
        btndashboard.setOnClickListener(v -> startActivity(new Intent(events.this, UserDashboard.class)));
        btnticket.setOnClickListener(v -> startActivity(new Intent(events.this, registered_events.class)));
        btnprofile.setOnClickListener(v -> startActivity(new Intent(events.this, profile_setting.class)));

        calendarFilter.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Standardizing format to match "Oct 24, 2026" style for simple comparison
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            selectedDate = months[month] + " " + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + ", " + year;
            applyFilters();
        });
    }

    private void fetchUserRegistrations() {
        if (currentUserId == null) return;

        registrationsRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userRegisteredEventIds.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Registration reg = ds.getValue(Registration.class);
                            if (reg != null) {
                                userRegisteredEventIds.add(reg.eventId);
                            }
                        }
                        applyFilters();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                if (!snapshot.exists()) {
                    addSampleEvents();
                    return;
                }
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
                Toast.makeText(events.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleEvents() {
        String[] titles = {"Global Tech Summit 2026", "AI Workshop", "Cloud Expo", "HackTheNorth", "Design Seminar", "Blockchain Meetup", "Cyber Security Forum"};
        String[] dates = {"Oct 24, 2026", "Oct 25, 2026", "Oct 26, 2026", "Oct 27, 2026", "Oct 28, 2026", "Oct 29, 2026", "Oct 30, 2026"};
        String[] categories = {"Seminar", "Workshop", "Exhibition", "Hackathon", "Seminar", "Workshop", "Seminar"};
        long baseTimestamp = System.currentTimeMillis() + 86400000L * 30; // 30 days from now

        for (int i = 0; i < titles.length; i++) {
            String id = eventsRef.push().getKey();
            Event event = new Event(id, titles[i], dates[i], "09:00 AM", categories[i], "Venue " + (i+1), "Description for " + titles[i], "", 0, 100, "Tentative", baseTimestamp + (i * 86400000L));
            eventsRef.child(id).setValue(event);
        }
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        
        // Status filters
        boolean fRegistered = cbRegistered.isChecked();
        boolean fJoin = cbJoin.isChecked();
        boolean fFull = cbFull.isChecked();
        boolean noStatusFilter = !fRegistered && !fJoin && !fFull;

        // Category filters
        boolean fWorkshop = cbWorkshop.isChecked();
        boolean fSeminar = cbSeminar.isChecked();
        boolean fExhibition = cbExhibition.isChecked();
        boolean fHackathon = cbHackathon.isChecked();
        boolean noCategoryFilter = !fWorkshop && !fSeminar && !fExhibition && !fHackathon;

        filteredEvents.clear();
        for (Event event : allEvents) {
            boolean matchesSearch = event.title.toLowerCase().contains(query);
            
            boolean isUserRegistered = userRegisteredEventIds.contains(event.id);
            boolean isFull = event.currentParticipants >= event.maxParticipants;
            
            boolean matchesStatus = noStatusFilter;
            
            if (!noStatusFilter) {
                if (fRegistered && isUserRegistered) {
                    matchesStatus = true;
                }
                if (fFull && isFull && !isUserRegistered) {
                    matchesStatus = true;
                }
                if (fJoin && !isFull && !isUserRegistered) {
                    matchesStatus = true;
                }
            }

            boolean matchesCategory = noCategoryFilter ||
                                      (fWorkshop && event.category.equalsIgnoreCase("Workshop")) ||
                                      (fSeminar && event.category.equalsIgnoreCase("Seminar")) ||
                                      (fExhibition && event.category.equalsIgnoreCase("Exhibition")) ||
                                      (fHackathon && event.category.equalsIgnoreCase("Hackathon"));

            boolean matchesDate = selectedDate.isEmpty() || event.date.equalsIgnoreCase(selectedDate);

            if (matchesSearch && matchesStatus && matchesCategory && matchesDate) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateList(filteredEvents, userRegisteredEventIds);
        
        if (filteredEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }
}
