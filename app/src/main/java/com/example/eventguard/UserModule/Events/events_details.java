package com.example.eventguard.UserModule.Events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventguard.UserModule.Dashboard.UserDashboard;
import com.example.eventguard.R;
import com.example.eventguard.models.Event;
import com.example.eventguard.models.Registration;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.example.eventguard.UserModule.ticket.qr_pass;
import com.example.eventguard.UserModule.ticket.registered_events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class events_details extends AppCompatActivity {

    private String eventId, eventTitle, eventDate, eventDesc, eventLoc, eventTime, imageUrl, status;
    private long eventTimestamp;
    private int currentParticipants, maxParticipants;
    private boolean isRegistered;
    private DatabaseReference registrationsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        registrationsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registrations");

        eventId = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");
        eventDate = getIntent().getStringExtra("eventDate");
        eventDesc = getIntent().getStringExtra("eventDesc");
        eventLoc = getIntent().getStringExtra("eventLoc");
        eventTime = getIntent().getStringExtra("eventTime");
        imageUrl = getIntent().getStringExtra("imageUrl");
        eventTimestamp = getIntent().getLongExtra("eventTimestamp", 0);
        currentParticipants = getIntent().getIntExtra("currentParticipants", 0);
        maxParticipants = getIntent().getIntExtra("maxParticipants", 0);
        isRegistered = getIntent().getBooleanExtra("isRegistered", false);
        status = getIntent().getStringExtra("status");

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvSeats = findViewById(R.id.tvDetailSeats);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvRegisterText = findViewById(R.id.tvRegisterText);
        ImageView ivBanner = findViewById(R.id.ivDetailBanner);

        if (eventTitle != null) tvTitle.setText(eventTitle);
        if (eventDesc != null) tvDesc.setText(eventDesc);
        if (eventLoc != null) tvLocation.setText(eventLoc);
        if (eventTime != null) tvTime.setText(eventTime);
        if (eventDate != null) tvDate.setText(eventDate);
        if (eventTitle != null) tvCategory.setText(eventTitle.split(" ")[0]); // Temporary placeholder for Host
        tvSeats.setText(currentParticipants + " / " + maxParticipants);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.event_detail_banner)
                    .into(ivBanner);
        }

        LinearLayout btnRegister = findViewById(R.id.btnSaveSecurity);
        LinearLayout btnCancel = findViewById(R.id.btnCancle);

        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000;
        boolean isClosed = "Closed".equalsIgnoreCase(status) || (currentTime >= (eventTimestamp - oneDayMillis));

        if (isRegistered) {
            tvRegisterText.setText("Already Registered");
            tvStatus.setText("Registered");
            tvStatus.setBackgroundResource(R.drawable.green_badge);
            tvStatus.setTextColor(getResources().getColor(R.color.status_green_text));
            btnRegister.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (isClosed) {
            tvRegisterText.setText("Registration Closed");
            tvStatus.setText("Closed");
            tvStatus.setBackgroundResource(R.drawable.blue_badge);
            tvStatus.setTextColor(getResources().getColor(R.color.status_blue_text));
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.6f);
            btnCancel.setVisibility(View.GONE);
        } else if (currentParticipants >= maxParticipants) {
            tvRegisterText.setText("Event Full");
            tvStatus.setText("Full");
            tvStatus.setBackgroundResource(R.drawable.red_badge);
            tvStatus.setTextColor(getResources().getColor(R.color.status_red_text));
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.6f);
            btnCancel.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Join Event →");
            tvStatus.setBackgroundResource(R.drawable.blue_badge);
            tvStatus.setTextColor(getResources().getColor(R.color.status_blue_text));
            btnRegister.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
        }


        btnRegister.setOnClickListener(v -> registerForEvent());
        btnCancel.setOnClickListener(v -> cancelRegistration());

        ImageView btndashboard = findViewById(R.id.btndashboard);
        ImageView btnticket = findViewById(R.id.btnticket);
        ImageView btnprofile = findViewById(R.id.btnprofile);
        ImageView btnmain = findViewById(R.id.btnmain);

        btnmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(events_details.this, events.class);
                startActivity(registered_ev);
            }
        });

        btndashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(events_details.this, UserDashboard.class);
                startActivity(registered_ev);
            }
        });

        btnticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(events_details.this, registered_events.class);
                startActivity(registered_ev);
            }
        });

        btnprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(events_details.this, profile_setting.class);
                startActivity(registered_ev);
            }
        });

    }

    private void registerForEvent() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference eventRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events").child(eventId);

        registrationsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    Registration reg = ds.getValue(Registration.class);
                    if (reg != null && reg.eventId.equals(eventId)) {
                        Toast.makeText(events_details.this, "You are already registered for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                performRegistrationTransaction(eventRef, userId);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void cancelRegistration() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        registrationsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                String regId = null;
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    Registration reg = ds.getValue(Registration.class);
                    if (reg != null && reg.eventId.equals(eventId)) {
                        regId = ds.getKey();
                        break;
                    }
                }

                if (regId != null) {
                    String finalRegId = regId;
                    DatabaseReference eventRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Events").child(eventId);
                    
                    eventRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                        @NonNull
                        @Override
                        public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData mutableData) {
                            Event event = mutableData.getValue(Event.class);
                            if (event == null) return com.google.firebase.database.Transaction.success(mutableData);

                            if (event.currentParticipants > 0) {
                                event.currentParticipants = event.currentParticipants - 1;
                                
                                // Re-evaluate status based on new count and time
                                long currentTime = System.currentTimeMillis();
                                long oneDayMillis = 24 * 60 * 60 * 1000;
                                
                                if (!"Closed".equalsIgnoreCase(event.status)) {
                                    if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
                                        event.status = "Closed";
                                    } else {
                                        event.status = "Available";
                                    }
                                }
                            }
                            mutableData.setValue(event);
                            return com.google.firebase.database.Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(com.google.firebase.database.DatabaseError databaseError, boolean committed, com.google.firebase.database.DataSnapshot dataSnapshot) {
                            if (committed) {
                                registrationsRef.child(finalRegId).removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(events_details.this, "Registration Cancelled", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void performRegistrationTransaction(DatabaseReference eventRef, String userId) {
        eventRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData mutableData) {
                Event event = mutableData.getValue(Event.class);
                if (event == null) {
                    return com.google.firebase.database.Transaction.success(mutableData);
                }

                if ("Closed".equalsIgnoreCase(event.status)) {
                    return com.google.firebase.database.Transaction.abort();
                }

                if (event.currentParticipants >= event.maxParticipants) {
                    return com.google.firebase.database.Transaction.abort();
                }

                // Increment participants
                event.currentParticipants = event.currentParticipants + 1;
                
                long currentTime = System.currentTimeMillis();
                long oneDayMillis = 24 * 60 * 60 * 1000;

                if (event.currentParticipants >= event.maxParticipants) {
                    event.status = "Full";
                } else if (currentTime >= (event.eventTimestamp - oneDayMillis)) {
                    event.status = "Closed";
                }
                
                mutableData.setValue(event);
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(com.google.firebase.database.DatabaseError databaseError, boolean committed, com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (committed) {
                    // Create Registration Record
                    String registrationId = registrationsRef.push().getKey();
                    Registration registration = new Registration(registrationId, userId, eventId, eventTitle, eventDate, eventLoc, eventTime, eventTimestamp);

                    registrationsRef.child(registrationId).setValue(registration)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(events_details.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(events_details.this, qr_pass.class);
                                    intent.putExtra("registrationId", registrationId);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                } else {
                    Toast.makeText(events_details.this, "Event is full or registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}