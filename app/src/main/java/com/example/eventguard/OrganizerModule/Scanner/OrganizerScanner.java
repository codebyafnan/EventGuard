package com.example.eventguard.OrganizerModule.Scanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Dashboard.OrganizerDashboard;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.R;
import com.example.eventguard.models.Attendance;
import com.example.eventguard.models.Registration;
import com.example.eventguard.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import com.example.eventguard.OrganizerModule.OrganizerProfile.organizer_profile_setting;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrganizerScanner extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageButton btnFlash;
    private boolean isFlashOn = false;

    private String selectedEventId;
    private String selectedEventName;
    private DatabaseReference registrationsRef, attendanceRef, usersRef;
    private FirebaseAuth auth;

    private RecyclerView rvRecentScans;
    private RecentScanAdapter scanAdapter;
    private List<Attendance> recentScans = new ArrayList<>();

    private boolean isProcessing = false;
    private String lastScannedData = "";
    private long lastScanTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_scanner);

        selectedEventId = getIntent().getStringExtra("eventId");
        selectedEventName = getIntent().getStringExtra("eventName");

        if (selectedEventId == null) {
            Toast.makeText(this, "No event selected!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = FirebaseAuth.getInstance();
        String databaseUrl = "https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/";
        registrationsRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Registrations");
        attendanceRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Attendance");
        usersRef = FirebaseDatabase.getInstance(databaseUrl).getReference("Users");

        barcodeScannerView = findViewById(R.id.barcodeScanner);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        // We will use custom callback to handle continuous scanning and validation
        barcodeScannerView.decodeContinuous(callback);

        btnFlash = findViewById(R.id.btnFlash);
        if (btnFlash != null) {
            btnFlash.setOnClickListener(v -> {
                if (isFlashOn) {
                    barcodeScannerView.setTorchOff();
                    isFlashOn = false;
                } else {
                    barcodeScannerView.setTorchOn();
                    isFlashOn = true;
                }
            });
        }



        setupRecentScans();
        setupNavigation();
        loadRecentScans();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) return;
            processScanResult(result.getText());
        }

        @Override
        public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
    };

    private void processScanResult(String rawData) {
        if (isProcessing) return;

        // Prevent repeated scans of the same code within 3 seconds
        if (rawData.equals(lastScannedData) && System.currentTimeMillis() - lastScanTime < 3000) {
            return;
        }

        try {
            JSONObject json = new JSONObject(rawData);
            String qrEventId = json.optString("eventId");
            String qrUserId = json.optString("userId");
            String qrType = json.optString("type");

            if (!"EventGuardPass".equals(qrType)) {
                showToast("Invalid QR code.");
                return;
            }

            if (selectedEventId == null || !selectedEventId.equals(qrEventId)) {
                showToast("Invalid pass for this event.");
                return;
            }

            isProcessing = true;
            lastScannedData = rawData;
            lastScanTime = System.currentTimeMillis();
            
            verifyUserRegistration(qrUserId, qrEventId);

        } catch (JSONException e) {
            // Only show toast if it looks like it was meant to be a JSON but failed
            if (rawData.startsWith("{")) {
                showToast("Invalid QR code format.");
            }
        }
    }

    private void verifyUserRegistration(String userId, String eventId) {
        registrationsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isRegistered = false;
                DataSnapshot targetRegistration = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Registration reg = ds.getValue(Registration.class);
                    if (reg != null && reg.eventId != null && reg.eventId.equals(eventId)) {
                        isRegistered = true;
                        targetRegistration = ds;
                        break;
                    }
                }

                if (!isRegistered) {
                    showToast("User is not registered for this event.");
                    isProcessing = false;
                } else {
                    checkAndMarkAttendance(userId, eventId, targetRegistration.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isProcessing = false;
            }
        });
    }

    private void checkAndMarkAttendance(String userId, String eventId, String regKey) {
        attendanceRef.child(eventId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    showToast("User has already checked in.");
                    isProcessing = false;
                } else {
                    markAttendance(userId, eventId, regKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isProcessing = false;
            }
        });
    }

    private void markAttendance(String userId, String eventId, String regKey) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String attendanceId = attendanceRef.push().getKey();
                    Attendance attendance = new Attendance(
                            attendanceId,
                            userId,
                            user.name,
                            user.email,
                            eventId,
                            selectedEventName,
                            "Present",
                            System.currentTimeMillis(),
                            auth.getUid()
                    );

                    attendanceRef.child(eventId).child(userId).setValue(attendance)
                            .addOnSuccessListener(aVoid -> {
                                showToast("Attendance marked for " + user.name);
                                // Also update registration record
                                registrationsRef.child(regKey).child("isAttendanceMarked").setValue(true);
                                isProcessing = false;
                            })
                            .addOnFailureListener(e -> {
                                showToast("Failed to mark attendance.");
                                isProcessing = false;
                            });
                } else {
                    showToast("User details not found.");
                    isProcessing = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isProcessing = false;
            }
        });
    }

    private long lastToastTime = 0;
    private void showToast(String message) {
        if (System.currentTimeMillis() - lastToastTime > 2000) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            lastToastTime = System.currentTimeMillis();
        }
    }

    private void setupRecentScans() {
        rvRecentScans = findViewById(R.id.rvRecentScans);
        rvRecentScans.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new RecentScanAdapter(recentScans);
        rvRecentScans.setAdapter(scanAdapter);
    }

    private void loadRecentScans() {
        attendanceRef.child(selectedEventId).orderByChild("timestamp").limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recentScans.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Attendance att = ds.getValue(Attendance.class);
                            if (att != null) {
                                recentScans.add(0, att); // Add to top
                            }
                        }
                        scanAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navDashboard).setOnClickListener(v -> startActivity(new Intent(this, OrganizerDashboard.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, organizer_profile_setting.class)));
        findViewById(R.id.navScanner).setOnClickListener(v -> {
            startActivity(new Intent(this, ScannerRegisteredEvents.class));
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (capture != null) capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (capture != null) capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (capture != null) capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capture != null) capture.onSaveInstanceState(outState);
    }
}
