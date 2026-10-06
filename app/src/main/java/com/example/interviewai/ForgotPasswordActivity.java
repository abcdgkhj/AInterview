package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String DEFAULT_MOCK_PASSWORD = "reset123";

    private TextInputLayout tilEmail;
    private TextInputEditText edtEmail;
    private Button btnResetPassword;
    private TextView btnBackToLogin;
    private ImageView btnBack;

    private LinearLayout cardResetForm;
    private LinearLayout cardResetSuccess;
    private TextView txtSuccessMessage;
    private Button btnSuccessBackToLogin;

    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

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
        edtEmail = findViewById(R.id.edtEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBack = findViewById(R.id.btnBack);

        cardResetForm = findViewById(R.id.cardResetForm);
        cardResetSuccess = findViewById(R.id.cardResetSuccess);
        txtSuccessMessage = findViewById(R.id.txtSuccessMessage);
        btnSuccessBackToLogin = findViewById(R.id.btnSuccessBackToLogin);

        // Pre-fill email jika diteruskan dari Login screen
        if (getIntent() != null && getIntent().hasExtra("extra_email")) {
            String incomingEmail = getIntent().getStringExtra("extra_email");
            if (incomingEmail != null && !incomingEmail.isEmpty()) {
                edtEmail.setText(incomingEmail);
            }
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnResetPassword.setOnClickListener(v -> attemptResetPassword());

        btnBackToLogin.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            returnToLogin(email);
        });

        if (btnSuccessBackToLogin != null) {
            btnSuccessBackToLogin.setOnClickListener(v -> {
                String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
                returnToLogin(email);
            });
        }
    }

    private void attemptResetPassword() {
        tilEmail.setError(null);

        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

        // 1. Validasi email kosong
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            edtEmail.requestFocus();
            return;
        }

        // 2. Validasi format email tidak valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            edtEmail.requestFocus();
            return;
        }

        // 3. Eksekusi alur reset lokal/mock
        sessionManager.resetPassword(email, DEFAULT_MOCK_PASSWORD);

        // 4. Tampilkan pesan sukses dan opsi kembali ke Login
        String successMsg = getString(R.string.msg_reset_success_desc, email, DEFAULT_MOCK_PASSWORD);
        if (txtSuccessMessage != null) {
            txtSuccessMessage.setText(successMsg);
        }

        if (cardResetForm != null) {
            cardResetForm.setVisibility(View.GONE);
        }
        if (cardResetSuccess != null) {
            cardResetSuccess.setVisibility(View.VISIBLE);
        }

        Toast.makeText(this, getString(R.string.toast_reset_success, DEFAULT_MOCK_PASSWORD), Toast.LENGTH_LONG).show();
    }

    private void returnToLogin(String email) {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        if (email != null && !email.isEmpty()) {
            intent.putExtra("extra_email", email);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
