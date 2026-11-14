package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfound.util.PrefsManager;
import com.example.lostfound.util.Ui;
import com.example.lostfound.util.Validator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a Firebase Auth account, then saves the student's profile under
 * users/{uid} in the Realtime Database.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEt;
    private TextInputEditText studentIdEt;
    private TextInputEditText emailEt;
    private TextInputEditText passwordEt;
    private TextInputEditText confirmEt;
    private Button registerBtn;
    private ProgressBar progress;

    private FirebaseAuth auth;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEt = findViewById(R.id.nameEt);
        studentIdEt = findViewById(R.id.studentIdEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        confirmEt = findViewById(R.id.confirmEt);
        registerBtn = findViewById(R.id.registerBtn);
        progress = findViewById(R.id.progress);
        TextView goLogin = findViewById(R.id.goLogin);

        auth = FirebaseAuth.getInstance();
        prefs = new PrefsManager(this);

        registerBtn.setOnClickListener(v -> attemptRegister());
        goLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        // Each check runs even if an earlier one failed, so every error shows at once
        boolean valid = Validator.requireText(nameEt, "Name is required");
        valid = Validator.requireText(studentIdEt, "Student ID is required") && valid;
        valid = Validator.isValidEmail(emailEt) && valid;
        valid = Validator.isValidPassword(passwordEt) && valid;
        valid = Validator.passwordsMatch(passwordEt, confirmEt) && valid;
        if (!valid) {
            return;
        }

        String name = Validator.text(nameEt);
        String studentId = Validator.text(studentIdEt);
        String email = Validator.text(emailEt);
        String password = Validator.rawText(passwordEt);

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful() || auth.getUid() == null) {
                        setLoading(false);
                        Ui.snack(this, "Registration failed: " + Ui.errorText(task.getException()));
                        return;
                    }
                    saveProfile(auth.getUid(), name, studentId, email);
                });
    }

    private void saveProfile(String uid, String name, String studentId, String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("studentId", studentId);
        profile.put("email", email);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .setValue(profile)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        prefs.setRememberMe(true);
                        prefs.setLastEmail(email);
                        goHome();
                    } else {
                        Ui.snack(this, "Profile could not be saved: " + Ui.errorText(task.getException()));
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerBtn.setEnabled(!loading);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
