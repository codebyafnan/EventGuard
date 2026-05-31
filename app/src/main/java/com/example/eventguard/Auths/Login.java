package com.example.eventguard.Auths;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventguard.OrganizerModule.OrganizerEvents.OrganizerEvents;
import com.example.eventguard.R;
import com.example.eventguard.UserModule.Events.events;
import com.example.eventguard.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        TextView registerLink = findViewById(R.id.tvGoToRegister);
        LinearLayout registerContainer = findViewById(R.id.GoTabRegister);

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class)));

        registerContainer.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class)));

        EditText emailField = findViewById(R.id.etEmail);
        EditText passwordField = findViewById(R.id.etPassword);
        Button loginButton = findViewById(R.id.btnLogin);

        ImageView togglePassword = findViewById(R.id.togglePassword);
        final boolean[] isPasswordVisible = {false};

        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.eye_close);
            } else {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.icon_eye);
            }
            isPasswordVisible[0] = !isPasswordVisible[0];
            passwordField.setSelection(passwordField.getText().length());
        });

        loginButton.setOnClickListener(v -> {

            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Email and Password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(Login.this, "User error", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = user.getUid();

                            DatabaseReference userRef = FirebaseDatabase
                                    .getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                    .getReference("Users")
                                    .child(uid);

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (!snapshot.exists()) {
                                        Toast.makeText(Login.this, "User data not found", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    User userObj = snapshot.getValue(User.class);
                                    if (userObj != null) {
                                        DatabaseHelper dbHelper = new DatabaseHelper(Login.this);
                                        dbHelper.saveUser(userObj, uid);
                                    }

                                    String role = snapshot.child("role").getValue(String.class);
                                    String name = snapshot.child("name").getValue(String.class);
                                    if (name == null) name = "User";

                                    if ("organizer".equals(role)) {
                                        Toast.makeText(Login.this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, OrganizerEvents.class));
                                    } else {
                                        Toast.makeText(Login.this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, events.class));
                                    }

                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Login.this,
                                            "DB Error: " + error.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login Failed";
                            Toast.makeText(Login.this,
                                    "Login Failed: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}