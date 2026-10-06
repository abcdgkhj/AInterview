package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private Button btnSignIn;
    private TextView btnGoToRegister;
    private TextView btnForgotPassword;
    private ImageView btnBack;
    private View cardDemoAccount;

    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new UserSessionManager(this);

        // Safe insets handling for R.id.main
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnBack = findViewById(R.id.btnBack);
        cardDemoAccount = findViewById(R.id.cardDemoAccount);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (cardDemoAccount != null) {
            cardDemoAccount.setOnClickListener(v -> {
                edtEmail.setText("demo@ainterview.app");
                edtPassword.setText("password123");
                tilEmail.setError(null);
                tilPassword.setError(null);
                Toast.makeText(this, "Akun demo terisi otomatis.", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                String currentEmail = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
                if (!currentEmail.isEmpty()) {
                    intent.putExtra("extra_email", currentEmail);
                }
                startActivity(intent);
            });
        }

        btnSignIn.setOnClickListener(v -> attemptLogin());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("extra_email")) {
            String email = intent.getStringExtra("extra_email");
            if (email != null && !email.isEmpty() && edtEmail != null) {
                edtEmail.setText(email);
                if (edtPassword != null) {
                    edtPassword.requestFocus();
                }
            }
        }
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            focusView = edtPassword;
            cancel = true;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            focusView = edtPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        boolean success = sessionManager.login(email, password);
        if (success) {
            Toast.makeText(this, "Selamat datang kembali!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            tilPassword.setError(getString(R.string.error_auth_failed));
            edtPassword.requestFocus();
            Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_LONG).show();
        }
    }
}
