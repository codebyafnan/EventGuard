package com.example.eventguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class events_details extends AppCompatActivity {

    private String eventId, eventTitle, eventDate, eventDesc, eventLoc, eventTime;
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
        eventTimestamp = getIntent().getLongExtra("eventTimestamp", 0);
        currentParticipants = getIntent().getIntExtra("currentParticipants", 0);
        maxParticipants = getIntent().getIntExtra("maxParticipants", 0);
        isRegistered = getIntent().getBooleanExtra("isRegistered", false);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvRegisterText = findViewById(R.id.tvRegisterText);

        if (eventTitle != null) tvTitle.setText(eventTitle);
        if (eventDesc != null) tvDesc.setText(eventDesc);
        if (eventLoc != null) tvLocation.setText(eventLoc);
        if (eventTime != null) tvTime.setText(eventTime);
        if (eventDate != null) tvDate.setText(eventDate);

        LinearLayout btnRegister = findViewById(R.id.btnSaveSecurity);
        LinearLayout btnCancel = findViewById(R.id.btnCancle);

        if (isRegistered) {
            tvRegisterText.setText("Already Registered");
            btnRegister.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (currentParticipants >= maxParticipants) {
            tvRegisterText.setText("Event Full");
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.6f);
            btnCancel.setVisibility(View.GONE);
        } else {
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
                        new Intent(events_details.this, dashboard.class);
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
                                event.status = "Available";
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

                if (event.currentParticipants >= event.maxParticipants) {
                    return com.google.firebase.database.Transaction.abort();
                }

                // Increment participants
                event.currentParticipants = event.currentParticipants + 1;
                if (event.currentParticipants >= event.maxParticipants) {
                    event.status = "Full";
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