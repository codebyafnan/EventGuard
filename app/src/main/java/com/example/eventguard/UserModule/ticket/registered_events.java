package com.example.eventguard.UserModule.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.UserModule.Dashboard.UserDashboard;
import com.example.eventguard.R;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.models.Registration;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class registered_events extends AppCompatActivity {

    private RecyclerView rvRegisteredEvents;
    private RegisteredEventAdapter adapter;
    private List<Registration> registrationList = new ArrayList<>();
    private DatabaseReference registrationsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registered_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        registrationsRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registrations");

        rvRegisteredEvents = findViewById(R.id.rvRegisteredEvents);
        rvRegisteredEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegisteredEventAdapter(this, registrationList);
        rvRegisteredEvents.setAdapter(adapter);

        fetchUserRegistrations();

        ImageView btndashboard = findViewById(R.id.btndashboard);
        ImageView btnmain = findViewById(R.id.btnmain);
        ImageView btnprofile = findViewById(R.id.btnprofile);
        ImageView btnticket = findViewById(R.id.btnticket);

        //Navbar Button
        btnticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(registered_events.this, registered_events.class);
                startActivity(registered_ev);
            }
        });

        btndashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(registered_events.this, UserDashboard.class);
                startActivity(registered_ev);
            }
        });

        btnmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(registered_events.this, events.class);
                startActivity(registered_ev);
            }
        });

        btnprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(registered_events.this, profile_setting.class);
                startActivity(registered_ev);
            }
        });

    }

    private void fetchUserRegistrations() {
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        registrationsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        registrationList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Registration reg = postSnapshot.getValue(Registration.class);
                            if (reg != null) {
                                registrationList.add(reg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(registered_events.this, "Failed to load registrations", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}