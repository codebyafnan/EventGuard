package com.example.eventguard.OrganizerModule.Scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventguard.OrganizerModule.Dashboard.OrganizerDashboard;
import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import com.example.eventguard.UserModule.Profile.profile_setting;

public class OrganizerScanner extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageButton btnFlash;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_scanner);

        barcodeScannerView = findViewById(R.id.barcodeScanner);
        btnFlash = findViewById(R.id.btnFlash);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        btnFlash.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeScannerView.setTorchOff();
                isFlashOn = false;
            } else {
                barcodeScannerView.setTorchOn();
                isFlashOn = true;
            }
        });

        setupRecentScans();
        setupNavigation();
    }

    private void setupRecentScans() {
        RecyclerView rv = findViewById(R.id.rvRecentScans);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Adapter would be set here with mock or real data
    }

    private void setupNavigation() {
        findViewById(R.id.navEvents).setOnClickListener(v -> startActivity(new Intent(this, OrganizerEvents.class)));
        findViewById(R.id.navDashboard).setOnClickListener(v -> startActivity(new Intent(this, OrganizerDashboard.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, profile_setting.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
}
