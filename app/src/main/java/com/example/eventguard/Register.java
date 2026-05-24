package com.example.eventguard;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        LinearLayout goTabLogin = findViewById(R.id.GoTabLogin);

        tvGoToLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));

        goTabLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        CheckBox cbTerms = findViewById(R.id.cbTerms);

        ImageView togglePassword = findViewById(R.id.togglePassword);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        final boolean[] isPasswordVisible = {false};
        final boolean[] isConfirmPasswordVisible = {false};

        // Password toggle
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.eye_close);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.icon_eye);
            }
            isPasswordVisible[0] = !isPasswordVisible[0];
            etPassword.setSelection(etPassword.getText().length());
        });

        // Confirm Password toggle
        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible[0]) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.eye_close);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.icon_eye);
            }
            isConfirmPasswordVisible[0] = !isConfirmPasswordVisible[0];
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        // Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                User user = new User(name, email, "attendee");
                                // The User constructor for 3 args already sets joinedDate = System.currentTimeMillis()

                                FirebaseDatabase.getInstance("https://eventguard-601b6-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                        .getReference("Users")
                                        .child(uid)
                                        .setValue(user);

                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this, Login.class));
                                finish();
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}