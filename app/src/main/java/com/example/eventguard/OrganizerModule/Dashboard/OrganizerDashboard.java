package com.example.eventguard.OrganizerModule.Dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventguard.Auths.DatabaseHelper;
import com.example.eventguard.Auths.Login;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.OrganizerModule.Scanner.OrganizerScanner;
import com.example.eventguard.R;
import com.example.eventguard.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.eventguard.UserModule.Profile.profile_setting;

import java.util.Locale;

public class OrganizerDashboard extends AppCompatActivity {

    private TextView tvAdminName, tvAdminStats, tvAdminEmail, tvAdminPhone, tvAdminLocation, tvActiveEventsCount;
    private ImageView ivAdminProfile;
    private FirebaseAuth auth;
    private DatabaseReference userRef, eventsRef;
    private DatabaseHelper dbHelper;
    private String joinedDateStr = "January 2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNav), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance(this);
        String databaseUrl = "https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/";
        userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Users").child(currentUser.getUid());
        eventsRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Events");

        // Initialize UI
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminStats = findViewById(R.id.tvAdminStats);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminPhone = findViewById(R.id.tvAdminPhone);
        tvAdminLocation = findViewById(R.id.tvAdminLocation);
        tvActiveEventsCount = findViewById(R.id.tvActiveEventsCount);
        ivAdminProfile = findViewById(R.id.ivAdminProfile);

        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("isUpdate", false);
            startActivity(intent);
        });

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, profile_setting.class));
        });

        // Navigation
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navScanner).setOnClickListener(v -> startActivity(new Intent(this, OrganizerScanner.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, profile_setting.class)));
        // Dashboard is current

        findViewById(R.id.btnFindEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.btnScanner).setOnClickListener(v -> startActivity(new Intent(this, OrganizerScanner.class)));
        findViewById(R.id.btnAnalytics).setOnClickListener(v -> startActivity(new Intent(this, OrganizerAnalyticsList.class)));

        loadAdminData(currentUser.getUid());
        loadStats();
    }

    private void loadAdminData(String uid) {
        User localUser = dbHelper.getUser(uid);
        if (localUser != null) {
            displayAdmin(localUser);
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        displayAdmin(user);
                        dbHelper.saveUser(user, uid);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStats() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvActiveEventsCount.setText(String.format(Locale.getDefault(), "%02d", count));
                
                String stats = "Joined " + joinedDateStr + " • " + count + " Events\nOrganizer";
                tvAdminStats.setText(stats);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayAdmin(User user) {
        tvAdminName.setText(user.name);
        tvAdminEmail.setText(user.email);
        tvAdminPhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "Not set");
        tvAdminLocation.setText(user.country != null && !user.country.isEmpty() ? user.country : "Not set");

        if (user.joinedDate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
            joinedDateStr = sdf.format(new java.util.Date(user.joinedDate));
        }

        if (user.profilePic != null && !user.profilePic.isEmpty()) {
            if (user.profilePic.startsWith("user_profile") || user.profilePic.startsWith("male") || user.profilePic.startsWith("female")) {
                int resId = getResources().getIdentifier(user.profilePic, "drawable", getPackageName());
                ivAdminProfile.setImageResource(resId != 0 ? resId : R.drawable.user_profile);
            } else {
                Glide.with(this).load(user.profilePic).placeholder(R.drawable.user_profile).into(ivAdminProfile);
            }
        }
    }
}
