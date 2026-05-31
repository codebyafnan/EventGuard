package com.example.eventguard.Auths;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.eventguard.R;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.models.User;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is already logged in, check role and redirect immediately
            checkUserRoleAndRedirect();
            // If the redirect was not immediate (slow path), show the splash screen
            if (!isFinishing()) {
                setContentView(R.layout.activity_main);
            }
        } else {
            // Not logged in, show splash screen then redirect to Login
            setContentView(R.layout.activity_main);
            new Handler().postDelayed(() -> {
                if (!isFinishing()) {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }
            }, 1500);
        }
    }

    private void checkUserRoleAndRedirect() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fast path: Check SQLite first
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        User localUser = dbHelper.getUser(uid);
        if (localUser != null && localUser.role != null) {
            redirectBasedOnRole(localUser.role);
            return;
        }

        // Slow path: Check Firebase if not in SQLite
        com.google.firebase.database.DatabaseReference userRef = com.google.firebase.database.FirebaseDatabase
                .getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        dbHelper.saveUser(user, uid); // Save for next time
                        redirectBasedOnRole(user.role);
                    } else {
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });
    }

    private void redirectBasedOnRole(String role) {
        if ("admin".equals(role)) {
            startActivity(new Intent(MainActivity.this, com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents.class));
        } else {
            startActivity(new Intent(MainActivity.this, events.class));
        }
        finish();
    }
}