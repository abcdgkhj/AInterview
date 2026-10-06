package com.example.interviewai;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.User;

/**
 * Allows the user to view and edit their personal information (name, email).
 * Field is displayed as read-only; changes must be made through "Ganti Bidang" in ProfileActivity.
 */
public class DataDiriActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtEmail;
    private TextView txtField;
    private Button btnSave;
    private ImageView btnBack;

    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_diri);

        sessionManager = new UserSessionManager(this);

        // Safe window insets handling
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        edtName = findViewById(R.id.edtDataDiriName);
        edtEmail = findViewById(R.id.edtDataDiriEmail);
        txtField = findViewById(R.id.txtDataDiriField);
        btnSave = findViewById(R.id.btnSaveDataDiri);
        btnBack = findViewById(R.id.btnBack);

        // Pastikan field email bersifat read-only dan tidak dapat diedit
        if (edtEmail != null) {
            edtEmail.setFocusable(false);
            edtEmail.setFocusableInTouchMode(false);
            edtEmail.setClickable(false);
            edtEmail.setCursorVisible(false);
            edtEmail.setKeyListener(null);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        populateCurrentData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void populateCurrentData() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            edtName.setText(user.getName());
            edtEmail.setText(user.getEmail());
            if (txtField != null) {
                txtField.setText(user.getField());
            }
        }
    }

    private void saveChanges() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";

        // Validate name
        if (name.isEmpty()) {
            edtName.setError(getString(R.string.error_empty_name));
            edtName.requestFocus();
            return;
        }

        // Update user data: email bersifat read-only sehingga alamat email akun tetap dipertahankan
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            user.setName(name);
            User updatedUser = new User(name, user.getEmail(), user.getPassword(), user.getRegisteredDate(), user.getField());
            sessionManager.saveUser(updatedUser);

            Toast.makeText(this, "Data diri berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
