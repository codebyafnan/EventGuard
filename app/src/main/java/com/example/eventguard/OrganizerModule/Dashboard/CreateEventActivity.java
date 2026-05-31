package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

    private EditText etName, etDescription, etDate, etTime, etVenue, etMaxCapacity, etImageUrl;
    private Spinner spinnerCategory;
    private Button btnSubmit;
    private TextView tvFormTitle;
    private ImageView ivEventPreview;
    private View btnSelectImage;

    private DatabaseReference eventsRef;
    private String eventId;
    private boolean isUpdate = false;
    private Event existingEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

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

        etImageUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(CreateEventActivity.this)
                            .load(url)
                            .placeholder(R.drawable.event_detail_banner)
                            .error(R.drawable.event_detail_banner)
                            .into(ivEventPreview);
                }
            }
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
        etImageUrl = findViewById(R.id.etImageUrl);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmit = findViewById(R.id.btnSubmitEvent);
        ivEventPreview = findViewById(R.id.ivEventPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
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
                    etImageUrl.setText(existingEvent.imageUrl);

                    if (existingEvent.imageUrl != null && !existingEvent.imageUrl.isEmpty()) {
                        Glide.with(CreateEventActivity.this)
                                .load(existingEvent.imageUrl)
                                .placeholder(R.drawable.event_detail_banner)
                                .into(ivEventPreview);
                    }
                    
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
        String imageUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty() || date.isEmpty() || time.isEmpty() || venue.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxCapacity = Integer.parseInt(capacityStr);
        int currentParticipants = isUpdate ? existingEvent.currentParticipants : 0;
        String status = isUpdate ? existingEvent.status : "Available";
        if (currentParticipants >= maxCapacity) status = "Full";

        String id = isUpdate ? eventId : eventsRef.push().getKey();
        
        // Simplified timestamp logic for demo
        long timestamp = System.currentTimeMillis(); 

        Event event = new Event(id, name, date, time, category, venue, desc, imageUrl, currentParticipants, maxCapacity, status, timestamp);

        eventsRef.child(id).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreateEventActivity.this, "Failed to save event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
