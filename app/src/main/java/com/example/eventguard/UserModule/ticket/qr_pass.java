package com.example.eventguard.UserModule.ticket;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eventguard.Auths.QRCodeHelper;
import com.example.eventguard.UserModule.Dashboard.UserDashboard;
import com.example.eventguard.R;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.models.Event;
import com.example.eventguard.models.Registration;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;

public class qr_pass extends AppCompatActivity {

    private String registrationId;
    private ImageView ivQRCode;
    private TextView tvEventTitle, tvStatusInfo, tvAttendeeName, tvLocation, tvTime, tvDate;
    private DatabaseReference registrationRef;
    private Bitmap qrBitmap;
    private CardView cvPassCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registrationId = getIntent().getStringExtra("registrationId");
        ivQRCode = findViewById(R.id.ivQRCode);
        tvEventTitle = findViewById(R.id.tvPassEventTitle);
        tvStatusInfo = findViewById(R.id.tvStatusInfo);
        tvAttendeeName = findViewById(R.id.tvAttendeeName);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvTime = findViewById(R.id.tvDetailTime);
        tvDate = findViewById(R.id.tvPassDate);
        cvPassCard = findViewById(R.id.cvPassCard);

        if (registrationId != null) {
            registrationRef = FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Registrations").child(registrationId);
            loadRegistrationData();
        }

        findViewById(R.id.btnDownloadTicket).setOnClickListener(v -> downloadTicket());

        ImageView btndashboard = findViewById(R.id.btndashboard);
        ImageView btnmain = findViewById(R.id.btnmain);
        ImageView btnticket = findViewById(R.id.btnticket);
        ImageView btnprofile = findViewById(R.id.btnprofile);

        btndashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(qr_pass.this, UserDashboard.class);
                startActivity(registered_ev);
            }
        });

        btnticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(qr_pass.this, registered_events.class);
                startActivity(registered_ev);
            }
        });

        btnprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(qr_pass.this, profile_setting.class);
                startActivity(registered_ev);
            }
        });

        btnmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registered_ev =
                        new Intent(qr_pass.this, events.class);
                startActivity(registered_ev);
            }
        });
    }

    private void loadRegistrationData() {
        registrationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Registration reg = snapshot.getValue(Registration.class);
                if (reg != null) {
                    tvEventTitle.setText(reg.eventTitle);
                    tvDate.setText(reg.eventDate);
                    tvLocation.setText(reg.eventLocation != null ? reg.eventLocation : "Venue TBA");
                    tvTime.setText(reg.eventTime != null ? reg.eventTime : "TBA");

                    // Fallback to fetch from Event if registration details are missing
                    if (reg.eventLocation == null || reg.eventTime == null) {
                        FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("Events").child(reg.eventId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot eventSnapshot) {
                                        Event event = eventSnapshot.getValue(Event.class);
                                        if (event != null) {
                                            if (reg.eventLocation == null) tvLocation.setText(event.location);
                                            if (reg.eventTime == null) tvTime.setText(event.time != null ? event.time : "09:00 AM");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                    
                    // Check Status
                    long currentTime = System.currentTimeMillis();
                    if (reg.isAttendanceMarked) {
                        tvStatusInfo.setText("Ticket Already Verified");
                        tvStatusInfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        ivQRCode.setAlpha(0.2f);
                    } else if (reg.eventTimestamp < currentTime && reg.eventTimestamp != 0) {
                        tvStatusInfo.setText("Event Expired");
                        tvStatusInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        ivQRCode.setAlpha(0.2f);
                    } else {
                        tvStatusInfo.setText("Valid Pass - One-time use");
                        tvStatusInfo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        ivQRCode.setAlpha(1.0f);
                    }

                    // Fetch user name
                    FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("Users").child(reg.userId).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        tvAttendeeName.setText(snapshot.getValue(String.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                    
                    generateStaticQR();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generateStaticQR() {
        // Simple registration ID as content for offline verification
        qrBitmap = QRCodeHelper.generateQRCode(registrationId);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
        }
    }

    private void downloadTicket() {
        if (cvPassCard == null) return;

        try {
            // Create bitmap of the ticket card
            Bitmap bitmap = Bitmap.createBitmap(cvPassCard.getWidth(), cvPassCard.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cvPassCard.draw(canvas);

            // Save to Gallery using MediaStore
            String filename = "EventGuard_Ticket_" + registrationId + ".png";
            OutputStream fos;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/EventGuard");

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                // For older versions, would need WRITE_EXTERNAL_STORAGE permission
                // Simplified for this task
                Toast.makeText(this, "Saving requires storage permission on older Android versions", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "Ticket Saved to Gallery (DCIM/EventGuard)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save ticket", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}