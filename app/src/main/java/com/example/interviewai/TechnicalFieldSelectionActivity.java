package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.User;

public class TechnicalFieldSelectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtSavedFieldHint;
    private View cardOptionIT;
    private RadioButton rbFieldIT;
    private View cardOptionMarketing;
    private RadioButton rbFieldMarketing;
    private View cardOptionAccounting;
    private RadioButton rbFieldAccounting;
    private Button btnStartTechnicalInterview;

    private UserSessionManager sessionManager;
    private String selectedField = User.FIELD_IT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical_field_selection);

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

        btnBack = findViewById(R.id.btnBack);
        txtSavedFieldHint = findViewById(R.id.txtSavedFieldHint);
        cardOptionIT = findViewById(R.id.cardOptionIT);
        rbFieldIT = findViewById(R.id.rbFieldIT);
        cardOptionMarketing = findViewById(R.id.cardOptionMarketing);
        rbFieldMarketing = findViewById(R.id.rbFieldMarketing);
        cardOptionAccounting = findViewById(R.id.cardOptionAccounting);
        rbFieldAccounting = findViewById(R.id.rbFieldAccounting);
        btnStartTechnicalInterview = findViewById(R.id.btnStartTechnicalInterview);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupInitialFieldSelection();
        setupClickListeners();
    }

    private void setupInitialFieldSelection() {
        String savedField = sessionManager.getUserField();
        if (savedField == null || savedField.trim().isEmpty()) {
            savedField = User.DEFAULT_FIELD;
        }

        if (txtSavedFieldHint != null) {
            txtSavedFieldHint.setText("Bidang profil tersimpan: " + savedField + " (Otomatis dipilih sebagai default)");
        }

        selectField(savedField);
    }

    private void selectField(String field) {
        selectedField = field;

        rbFieldIT.setChecked(false);
        rbFieldMarketing.setChecked(false);
        rbFieldAccounting.setChecked(false);

        if (User.FIELD_MARKETING.equalsIgnoreCase(field)) {
            rbFieldMarketing.setChecked(true);
            selectedField = User.FIELD_MARKETING;
        } else if (User.FIELD_ACCOUNTING.equalsIgnoreCase(field)) {
            rbFieldAccounting.setChecked(true);
            selectedField = User.FIELD_ACCOUNTING;
        } else {
            rbFieldIT.setChecked(true);
            selectedField = User.FIELD_IT;
        }

        if (btnStartTechnicalInterview != null) {
            btnStartTechnicalInterview.setText("Mulai Interview Teknis (" + selectedField + ")");
        }
    }

    private void setupClickListeners() {
        cardOptionIT.setOnClickListener(v -> selectField(User.FIELD_IT));
        cardOptionMarketing.setOnClickListener(v -> selectField(User.FIELD_MARKETING));
        cardOptionAccounting.setOnClickListener(v -> selectField(User.FIELD_ACCOUNTING));

        btnStartTechnicalInterview.setOnClickListener(v -> startTechnicalInterview());
    }

    private void startTechnicalInterview() {
        User user = sessionManager.getCurrentUser();
        String candidateName = (user != null && user.getName() != null && !user.getName().trim().isEmpty())
                ? user.getName().trim() : "Kandidat";

        String category = QuestionBank.getTechnicalCategoryForField(selectedField);

        Intent intent = new Intent(TechnicalFieldSelectionActivity.this, InterviewActivity.class);
        intent.putExtra(MainActivity.EXTRA_CANDIDATE_NAME, candidateName);
        intent.putExtra(MainActivity.EXTRA_ROLE_CATEGORY, category);
        intent.putExtra(InterviewActivity.EXTRA_DIFFICULTY, QuestionBank.DIFFICULTY_INTERMEDIATE);
        intent.putExtra(InterviewActivity.EXTRA_QUESTION_COUNT, 5);
        intent.putExtra(InterviewActivity.EXTRA_LANGUAGE, sessionManager.getDefaultLanguage());
        startActivity(intent);
        finish();
    }
}
