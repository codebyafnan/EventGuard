package com.example.eventguard.UserModule.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventguard.R;
import com.example.eventguard.UserModule.Dashboard.UserDashboard;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.UserModule.Profile.profile_setting;
import com.example.eventguard.UserModule.ticket.registered_events;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class security extends AppCompatActivity {

    private EditText etCurrentPassword, etPassword, etConfirmPassword;
    private ImageView toggleCurrent, toggleNew, toggleConfirm;
    private boolean isCurrentVisible = false, isNewVisible = false, isConfirmVisible = false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Initialize Views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        toggleCurrent = findViewById(R.id.toggleCurrentPassword);
        toggleNew = findViewById(R.id.togglePassword);
        toggleConfirm = findViewById(R.id.toggleConfirmPassword);
        
        LinearLayout btnSave = findViewById(R.id.btnSaveSecurity);

        // Password Toggles
        toggleCurrent.setOnClickListener(v -> toggleVisibility(etCurrentPassword, toggleCurrent, isCurrentVisible = !isCurrentVisible));
        toggleNew.setOnClickListener(v -> toggleVisibility(etPassword, toggleNew, isNewVisible = !isNewVisible));
        toggleConfirm.setOnClickListener(v -> toggleVisibility(etConfirmPassword, toggleConfirm, isConfirmVisible = !isConfirmVisible));

        btnSave.setOnClickListener(v -> updatePassword());

        // Navbar
        findViewById(R.id.btnmain).setOnClickListener(v -> startActivity(new Intent(this, events.class)));
        findViewById(R.id.btnticket).setOnClickListener(v -> startActivity(new Intent(this, registered_events.class)));
        findViewById(R.id.btndashboard).setOnClickListener(v -> startActivity(new Intent(this, UserDashboard.class)));
        findViewById(R.id.btnprofile).setOnClickListener(v -> startActivity(new Intent(this, profile_setting.class)));
    }

    private void toggleVisibility(EditText editText, ImageView imageView, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.icon_eye); // Ensure this drawable exists or use a default
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.eye_close);
        }
        editText.setSelection(editText.getText().length());
    }

    private void updatePassword() {
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(security.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(security.this, "Update failed: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(security.this, "Current password incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
