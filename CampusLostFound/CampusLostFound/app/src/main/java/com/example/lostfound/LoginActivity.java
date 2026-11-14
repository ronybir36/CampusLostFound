package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfound.util.PrefsManager;
import com.example.lostfound.util.Ui;
import com.example.lostfound.util.Validator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEt;
    private TextInputEditText passwordEt;
    private CheckBox rememberCb;
    private Button loginBtn;
    private ProgressBar progress;

    private FirebaseAuth auth;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        rememberCb = findViewById(R.id.rememberCb);
        loginBtn = findViewById(R.id.loginBtn);
        progress = findViewById(R.id.progress);
        TextView goRegister = findViewById(R.id.goRegister);

        auth = FirebaseAuth.getInstance();
        prefs = new PrefsManager(this);

        emailEt.setText(prefs.getLastEmail());
        rememberCb.setChecked(prefs.isRememberMe());

        loginBtn.setOnClickListener(v -> attemptLogin());
        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        boolean emailOk = Validator.isValidEmail(emailEt);
        boolean passwordOk = Validator.isValidPassword(passwordEt);
        if (!emailOk || !passwordOk) {
            return;
        }

        String email = Validator.text(emailEt);
        String password = Validator.rawText(passwordEt);

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        prefs.setRememberMe(rememberCb.isChecked());
                        prefs.setLastEmail(email);
                        goHome();
                    } else {
                        Ui.snack(this, "Login failed: " + Ui.errorText(task.getException()));
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginBtn.setEnabled(!loading);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
