package com.example.eventguard.UserModule.Profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventguard.Auths.Login;
import com.example.eventguard.Auths.DatabaseHelper;
import com.example.eventguard.R;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.UserModule.Dashboard.UserDashboard;
import com.example.eventguard.models.User;
import com.example.eventguard.UserModule.ticket.registered_events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class profile_setting extends AppCompatActivity {

    private EditText etName, etEmail, etBio, etPhone, etCountry;
    private TextView tvProfileName, tvProfileRole, tvAccountRole;
    private ImageView ivProfileLarge;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private Uri imageUri;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfileLarge.setImageURI(imageUri); // Preview immediately
                    
                    // Save locally
                    String localPath = saveImageToInternalStorage(imageUri);
                    if (localPath != null) {
                        updateProfilePic(localPath);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setting);
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
        
        // Initialize UI Elements
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);
        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvAccountRole = findViewById(R.id.tvAccountRole);
        ivProfileLarge = findViewById(R.id.ivProfileLarge);

        FrameLayout flAvatarDefault = findViewById(R.id.flAvatarDefault);
        FrameLayout flAvatarMale = findViewById(R.id.flAvatarMale);
        FrameLayout flAvatarFemale = findViewById(R.id.flAvatarFemale);
        LinearLayout btnGallery = findViewById(R.id.btnGallery);
        ImageView btnChangePic = findViewById(R.id.btnChangePic);
        
        LinearLayout btnSave = findViewById(R.id.btnSave);
        LinearLayout btnsec = findViewById(R.id.btnsec);
        LinearLayout btnlogout = findViewById(R.id.btnlogout);

        // Navbar
        ImageView btnprofile = findViewById(R.id.btnprofile);
        ImageView btndashboard = findViewById(R.id.btndashboard);
        ImageView btnticket = findViewById(R.id.btnticket);
        ImageView btnmain = findViewById(R.id.btnmain);

        loadUserData();

        // Avatar Selection
        flAvatarDefault.setOnClickListener(v -> updateProfilePic("user_profile"));
        flAvatarMale.setOnClickListener(v -> updateProfilePic("male_profile"));
        flAvatarFemale.setOnClickListener(v -> updateProfilePic("female_profile"));

        // Gallery Selection
        btnGallery.setOnClickListener(v -> openImagePicker());
        btnChangePic.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> saveUserData());

        btnsec.setOnClickListener(v -> startActivity(new Intent(profile_setting.this, security.class)));

        btnlogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(profile_setting.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Navbar navigation
        btnmain.setOnClickListener(v -> startActivity(new Intent(profile_setting.this, events.class)));
        btndashboard.setOnClickListener(v -> startActivity(new Intent(profile_setting.this, UserDashboard.class)));
        btnticket.setOnClickListener(v -> startActivity(new Intent(profile_setting.this, registered_events.class)));
        btnprofile.setOnClickListener(v -> {}); // Already here

    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            final String uid = auth.getCurrentUser().getUid();
            // Try loading from SQLite first for offline/faster UI
            User localUser = dbHelper.getUser(uid);
            if (localUser != null) {
                displayUser(localUser);
            }

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            displayUser(user);
                            // Save to SQLite for persistence
                            dbHelper.saveUser(user, uid);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(profile_setting.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayUser(User user) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        etName.setText(user.name);
        etEmail.setText(user.email);
        etBio.setText(user.bio);
        etPhone.setText(user.phone);
        etCountry.setText(user.country);
        tvProfileName.setText(user.name);
        if (user.role != null) {
            tvProfileRole.setText(user.role);
            tvAccountRole.setText(user.role);
        }

        // Load Profile Picture using UID
        File localFile = new File(getFilesDir(), "profile_" + userId + ".jpg");

        if (user.profilePic != null && (user.profilePic.equals("user_profile") || user.profilePic.equals("male_profile") || user.profilePic.equals("female_profile"))) {
            // User selected a default avatar
            int resId = getResources().getIdentifier(user.profilePic, "drawable", getPackageName());
            ivProfileLarge.setImageResource(resId != 0 ? resId : R.drawable.user_profile);
        } else if (localFile.exists()) {
            // Load local custom image with signature to bypass cache
            Glide.with(profile_setting.this)
                    .load(localFile)
                    .placeholder(R.drawable.user_profile)
                    .error(R.drawable.user_profile)
                    .signature(new com.bumptech.glide.signature.ObjectKey(localFile.lastModified()))
                    .into(ivProfileLarge);
        } else if (user.profilePic != null && !user.profilePic.isEmpty()) {
            // Fallback for path stored in database
            Object imageSource = user.profilePic.startsWith("/") ? new File(user.profilePic) : user.profilePic;
            Glide.with(profile_setting.this)
                    .load(imageSource)
                    .placeholder(R.drawable.user_profile)
                    .error(R.drawable.user_profile)
                    .into(ivProfileLarge);
        } else {
            ivProfileLarge.setImageResource(R.drawable.user_profile);
        }
    }

    private void saveUserData() {
        String newName = etName.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newCountry = etCountry.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", newName);
        updates.put("bio", newBio);
        updates.put("phone", newPhone);
        updates.put("country", newCountry);

        // Update SQLite immediately for responsiveness
        if (auth.getCurrentUser() != null) {
            User localUser = dbHelper.getUser(auth.getCurrentUser().getUid());
            if (localUser != null) {
                localUser.name = newName;
                localUser.bio = newBio;
                localUser.phone = newPhone;
                localUser.country = newCountry;
                dbHelper.saveUser(localUser, auth.getCurrentUser().getUid());
            }
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(profile_setting.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(profile_setting.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Uri uri) {
        if (auth.getCurrentUser() == null) return null;
        String uid = auth.getCurrentUser().getUid();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            // Use UID in filename to avoid conflicts and add timestamp to bypass Glide cache
            File file = new File(getFilesDir(), "profile_" + uid + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateProfilePic(String picValue) {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            userRef.child("profilePic").setValue(picValue)
                    .addOnSuccessListener(aVoid -> {
                        // Update SQLite immediately
                        User localUser = dbHelper.getUser(uid);
                        if (localUser != null) {
                            localUser.profilePic = picValue;
                            dbHelper.saveUser(localUser, uid);
                        }
                        Toast.makeText(profile_setting.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(profile_setting.this, "Failed to update database", Toast.LENGTH_SHORT).show());
        }
    }
}
