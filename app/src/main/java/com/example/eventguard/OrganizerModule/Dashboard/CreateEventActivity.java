package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.OrganizerModule.Scanner.OrganizerScanner;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etName, etDescription, etDate, etTime, etVenue, etMaxCapacity;
    private Spinner spinnerCategory;
    private Button btnSubmit;
    private TextView tvFormTitle;

    private DatabaseReference eventsRef;
    private String eventId;
    private boolean isUpdate = false;
    private Event existingEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNav), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        eventsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events");

        initViews();
        setupCategorySpinner();

        isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        eventId = getIntent().getStringExtra("eventId");

        if (isUpdate && eventId != null) {
            tvFormTitle.setText("Update Event");
            btnSubmit.setText("Update Event");
            loadEventData();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());

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
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etEventDescription);
        etDate = findViewById(R.id.etEventDate);
        etTime = findViewById(R.id.etEventTime);
        etVenue = findViewById(R.id.etEventVenue);
        etMaxCapacity = findViewById(R.id.etMaxCapacity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmit = findViewById(R.id.btnSubmitEvent);
    }

    private void setupCategorySpinner() {
        String[] categories = {"Workshop", "Seminar", "Exhibition", "Hackathon", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year1, monthOfYear, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            etDate.setText(sdf.format(selected.getTime()));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String am_pm = (hourOfDay < 12) ? "AM" : "PM";
            int hourDisplay = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
            etTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hourDisplay, minute1, am_pm));
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void loadEventData() {
        eventsRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                existingEvent = snapshot.getValue(Event.class);
                if (existingEvent != null) {
                    etName.setText(existingEvent.title);
                    etDescription.setText(existingEvent.description);
                    etDate.setText(existingEvent.date);
                    etTime.setText(existingEvent.time);
                    etVenue.setText(existingEvent.location);
                    etMaxCapacity.setText(String.valueOf(existingEvent.maxParticipants));

                    // Set spinner selection
                    ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
                    int position = adapter.getPosition(existingEvent.category);
                    if (position >= 0) spinnerCategory.setSelection(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void validateAndSubmit() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String capacityStr = etMaxCapacity.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || desc.isEmpty() || date.isEmpty() || time.isEmpty() || venue.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxCapacity = Integer.parseInt(capacityStr);
        int currentParticipants = isUpdate ? existingEvent.currentParticipants : 0;
        
        long timestamp = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            String dateTimeStr = date + " " + (time.isEmpty() ? "12:00 AM" : time);
            timestamp = sdf.parse(dateTimeStr).getTime();
        } catch (Exception e) {
            timestamp = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // Fallback
        }

        String status = isUpdate ? existingEvent.status : "Available";
        
        long oneDayMillis = 24 * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();
        
        if (currentParticipants >= maxCapacity) {
            status = "Full";
        } else if (currentTime >= (timestamp - oneDayMillis)) {
            status = "Closed";
        } else if (!"Closed".equalsIgnoreCase(status)) {
            status = "Available";
        }

        String id = isUpdate ? eventId : eventsRef.push().getKey();
        String imageUrl = isUpdate ? existingEvent.imageUrl : ""; // Carry forward or empty

        Event event = new Event(id, name, date, time, category, venue, desc, imageUrl, currentParticipants, maxCapacity, status, timestamp);

        eventsRef.child(id).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateEventActivity.this, OrganizerEvents.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CreateEventActivity.this, "Failed to save event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
