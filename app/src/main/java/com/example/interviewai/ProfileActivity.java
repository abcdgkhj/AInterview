package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.InterviewSession;
import com.example.interviewai.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtMemberSince;
    private TextView txtProfileField;
    private TextView txtStatSessions;
    private TextView txtStatAverageScore;
    private TextView txtCurrentFieldHint;
    private View btnDataDiri;
    private View btnGantiBidang;
    private Button btnLogout;
    private ImageView btnBack;

    private UserSessionManager sessionManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtMemberSince = findViewById(R.id.txtMemberSince);
        txtProfileField = findViewById(R.id.txtProfileField);
        txtStatSessions = findViewById(R.id.txtStatSessions);
        txtStatAverageScore = findViewById(R.id.txtStatAverageScore);
        txtCurrentFieldHint = findViewById(R.id.txtCurrentFieldHint);
        btnDataDiri = findViewById(R.id.btnDataDiri);
        btnGantiBidang = findViewById(R.id.btnGantiBidang);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Data Diri -> opens DataDiriActivity
        if (btnDataDiri != null) {
            btnDataDiri.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DataDiriActivity.class);
                startActivity(intent);
            });
        }

        // Ganti Bidang -> shows single-choice dialog
        if (btnGantiBidang != null) {
            btnGantiBidang.setOnClickListener(v -> showGantiBidangDialog());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        populateUserData();
        populateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateUserData();
        populateStats();
    }

    private void populateUserData() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            txtProfileName.setText(user.getName());
            txtProfileEmail.setText(user.getEmail());
            String dateFormatted = dateFormat.format(new Date(user.getRegisteredDate()));
            txtMemberSince.setText("Bergabung: " + dateFormatted);
            if (txtProfileField != null) {
                txtProfileField.setText("Bidang: " + user.getField());
            }
            if (txtCurrentFieldHint != null) {
                txtCurrentFieldHint.setText("Bidang saat ini: " + user.getField());
            }
        }
    }

    private void populateStats() {
        List<InterviewSession> sessions = InterviewStorage.getSessions(this);
        if (sessions == null || sessions.isEmpty()) {
            txtStatSessions.setText("0");
            txtStatAverageScore.setText("-");
        } else {
            int count = sessions.size();
            int totalScore = 0;
            for (InterviewSession s : sessions) {
                totalScore += s.getOverallScore();
            }
            int avg = Math.round((float) totalScore / count);

            txtStatSessions.setText(String.valueOf(count));
            txtStatAverageScore.setText(String.valueOf(avg));
        }
    }

    private void showGantiBidangDialog() {
        String[] fields = User.AVAILABLE_FIELDS;
        String currentField = sessionManager.getUserField();

        // Find the currently selected index
        int selectedIndex = 0;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equalsIgnoreCase(currentField)) {
                selectedIndex = i;
                break;
            }
        }

        final int[] chosenIndex = { selectedIndex };

        new AlertDialog.Builder(this)
                .setTitle("Ganti Bidang Keahlian")
                .setSingleChoiceItems(fields, selectedIndex, (dialog, which) -> {
                    chosenIndex[0] = which;
                })
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String selectedField = fields[chosenIndex[0]];
                    sessionManager.setUserField(selectedField);
                    populateUserData();
                    Toast.makeText(this, "Bidang diubah ke: " + selectedField, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_logout_title))
                .setMessage(getString(R.string.confirm_logout_message))
                .setPositiveButton("Keluar", (dialog, which) -> {
                    sessionManager.logout();
                    Toast.makeText(this, "Anda telah keluar dari sesi.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }
}
