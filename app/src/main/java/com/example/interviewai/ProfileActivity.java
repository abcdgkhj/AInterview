package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.data.QuestionBank;
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
    private TextView txtStatSessions;
    private TextView txtStatAverageScore;
    private Spinner spnDefaultLanguage;
    private SwitchCompat switchTips;
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
        txtStatSessions = findViewById(R.id.txtStatSessions);
        txtStatAverageScore = findViewById(R.id.txtStatAverageScore);
        spnDefaultLanguage = findViewById(R.id.spnDefaultLanguage);
        switchTips = findViewById(R.id.switchTips);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        populateUserData();
        populateStats();
        setupLanguageSpinner();

        if (switchTips != null) {
            switchTips.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "diaktifkan" : "dinonaktifkan";
                Toast.makeText(this, "Tips interview " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void populateUserData() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            txtProfileName.setText(user.getName());
            txtProfileEmail.setText(user.getEmail());
            String dateFormatted = dateFormat.format(new Date(user.getRegisteredDate()));
            txtMemberSince.setText("Bergabung: " + dateFormatted);
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

    private void setupLanguageSpinner() {
        List<String> languages = QuestionBank.getAvailableLanguages();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDefaultLanguage.setAdapter(adapter);

        String currentDefault = sessionManager.getDefaultLanguage();
        int index = languages.indexOf(currentDefault);
        if (index >= 0) {
            spnDefaultLanguage.setSelection(index);
        }

        spnDefaultLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = languages.get(position);
                sessionManager.setDefaultLanguage(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
