package com.example.eventguard;

import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class dashboard extends AppCompatActivity {

    private TextView tvDashName, tvDashRole, tvDashTicketCount, tvDashEmail, tvDashBio, tvDashPhone, tvDashLocation, tvDashJoinedEvents;
    private ImageView ivDashProfile;
    private FirebaseAuth auth;
    private DatabaseReference userRef, registrationsRef;
    private DatabaseHelper dbHelper;
    private String currentJoinedDateFormatted = "January 2024";
    private String currentAttendedCountStr = "00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        userRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").child(currentUser.getUid());
        registrationsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Registrations");

        // Initialize UI
        tvDashName = findViewById(R.id.tvDashName);
        tvDashRole = findViewById(R.id.tvDashRole);
        tvDashTicketCount = findViewById(R.id.tvDashTicketCount);
        tvDashJoinedEvents = findViewById(R.id.tvDashJoinedEvents);
        tvDashEmail = findViewById(R.id.tvDashEmail);
        tvDashBio = findViewById(R.id.tvDashBio);
        tvDashPhone = findViewById(R.id.tvDashPhone);
        tvDashLocation = findViewById(R.id.tvDashLocation);
        ivDashProfile = findViewById(R.id.ivDashProfile);

        LinearLayout btn_ticket_card = findViewById(R.id.btn_ticket_card);
        LinearLayout btnEventCard = findViewById(R.id.btnEventCard);
        LinearLayout btnProSet = findViewById(R.id.btnProSet);

        ImageView btnprofile = findViewById(R.id.btnprofile);
        ImageView btnmain = findViewById(R.id.btnmain);
        ImageView btnticket = findViewById(R.id.btnticket);
        ImageView btndashboard = findViewById(R.id.btndashboard);

        loadDashboardData(currentUser.getUid());

        btn_ticket_card.setOnClickListener(v -> startActivity(new Intent(dashboard.this, registered_events.class)));
        btnEventCard.setOnClickListener(v -> startActivity(new Intent(dashboard.this, events.class)));
        btnProSet.setOnClickListener(v -> startActivity(new Intent(dashboard.this, profile_setting.class)));

        // Nav
        btnprofile.setOnClickListener(v -> startActivity(new Intent(dashboard.this, profile_setting.class)));
        btnmain.setOnClickListener(v -> startActivity(new Intent(dashboard.this, events.class)));
        btnticket.setOnClickListener(v -> startActivity(new Intent(dashboard.this, registered_events.class)));
        btndashboard.setOnClickListener(v -> {}); // Already here
    }

    private void loadDashboardData(String userId) {
        // Try loading from SQLite first for offline/faster UI
        User localUser = null;
        try {
            localUser = dbHelper.getUser(userId);
            if (localUser != null) {
                displayUser(localUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fetch User Info
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        displayUser(user);
                        // Update SQLite with the latest data from Firebase
                        try {
                            dbHelper.saveUser(user, userId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ignore errors if we have local data, otherwise show toast
                try {
                    if (dbHelper.getUser(userId) == null) {
                        Toast.makeText(dashboard.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Fetch Registration Count
        registrationsRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCount = snapshot.getChildrenCount();
                long attendedCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Registration reg = ds.getValue(Registration.class);
                    if (reg != null && reg.isAttendanceMarked) {
                        attendedCount++;
                    }
                }

                String totalCountStr = String.format(Locale.getDefault(), "%02d", totalCount);
                currentAttendedCountStr = String.format(Locale.getDefault(), "%02d", attendedCount);
                
                tvDashTicketCount.setText(totalCountStr);
                
                // Update the joined events text with the actual attended count
                String fullText = "Joined " + currentJoinedDateFormatted + " • " + currentAttendedCountStr + " Events\nAttended";
                tvDashJoinedEvents.setText(fullText);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayUser(User user) {
        tvDashName.setText(user.name);
        tvDashRole.setText(user.role);
        tvDashEmail.setText(user.email);
        tvDashBio.setText(user.bio != null && !user.bio.isEmpty() ? user.bio : "No bio added.");
        tvDashPhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "Not set");
        tvDashLocation.setText(user.country != null && !user.country.isEmpty() ? user.country : "Not set");

        // Format Joined Date
        if (user.joinedDate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
            currentJoinedDateFormatted = sdf.format(new java.util.Date(user.joinedDate));
        }
        
        // Initial update with current count
        String fullText = "Joined " + currentJoinedDateFormatted + " • " + currentAttendedCountStr + " Events\nAttended";
        tvDashJoinedEvents.setText(fullText);

        // Load Profile Picture
        if (user.profilePic != null && !user.profilePic.isEmpty()) {
            if (user.profilePic.equals("user_profile") || user.profilePic.equals("male_profile") || user.profilePic.equals("female_profile")) {
                int resId = getResources().getIdentifier(user.profilePic, "drawable", getPackageName());
                if (resId != 0) {
                    ivDashProfile.setImageResource(resId);
                } else {
                    ivDashProfile.setImageResource(R.drawable.user_profile);
                }
            } else {
                Glide.with(dashboard.this)
                        .load(user.profilePic)
                        .placeholder(R.drawable.user_profile)
                        .error(R.drawable.user_profile)
                        .into(ivDashProfile);
            }
        }
    }
}
