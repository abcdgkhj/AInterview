package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText edtFullName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;

    private Button btnRegister;
    private TextView btnGoToLogin;
    private ImageView btnBack;
    private Spinner spnField;

    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        spnField = findViewById(R.id.spnField);
        if (spnField != null) {
            ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    com.example.interviewai.model.User.AVAILABLE_FIELDS
            );
            fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnField.setAdapter(fieldAdapter);
        }

        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String fullName = edtFullName.getText() != null ? edtFullName.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        boolean cancel = false;
        View focusView = null;

        // Validasi Konfirmasi Password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_empty_password));
            focusView = edtConfirmPassword;
            cancel = true;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            focusView = edtConfirmPassword;
            cancel = true;
        }

        // Validasi Password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            focusView = edtPassword;
            cancel = true;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            focusView = edtPassword;
            cancel = true;
        }

        // Validasi Email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            cancel = true;
        }

        // Validasi Nama Lengkap
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_empty_name));
            focusView = edtFullName;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        String selectedField = com.example.interviewai.model.User.DEFAULT_FIELD;
        if (spnField != null && spnField.getSelectedItem() != null) {
            selectedField = spnField.getSelectedItem().toString();
        }

        boolean registered = sessionManager.register(fullName, email, password, selectedField);
        if (registered) {
            Toast.makeText(this, getString(R.string.success_registered), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Gagal mendaftarkan akun. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
        }
    }
}
