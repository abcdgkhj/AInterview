package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.notification.AppNotification;

public class InterviewSetupActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_QUESTION_COUNT = "extra_question_count";
    public static final String EXTRA_LANGUAGE = "extra_language";

    private RadioGroup rgDifficulty;
    private RadioButton rbBeginner;
    private RadioButton rbIntermediate;
    private RadioButton rbAdvanced;

    private RadioGroup rgQuestionCount;
    private RadioButton rbCount3;
    private RadioButton rbCount5;
    private RadioButton rbCount10;

    private RadioGroup rgLanguage;
    private RadioButton rbLangId;
    private RadioButton rbLangEn;

    private Button btnSaveConfig;
    private ImageView btnBack;

    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_setup);

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

        rgDifficulty = findViewById(R.id.rgDifficulty);
        rbBeginner = findViewById(R.id.rbBeginner);
        rbIntermediate = findViewById(R.id.rbIntermediate);
        rbAdvanced = findViewById(R.id.rbAdvanced);

        rgQuestionCount = findViewById(R.id.rgQuestionCount);
        rbCount3 = findViewById(R.id.rbCount3);
        rbCount5 = findViewById(R.id.rbCount5);
        rbCount10 = findViewById(R.id.rbCount10);

        rgLanguage = findViewById(R.id.rgLanguage);
        rbLangId = findViewById(R.id.rbLangId);
        rbLangEn = findViewById(R.id.rbLangEn);

        btnSaveConfig = findViewById(R.id.btnSaveConfig);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadSavedConfiguration();

        if (btnSaveConfig != null) {
            btnSaveConfig.setOnClickListener(v -> saveConfiguration());
        }
    }

    private void loadSavedConfiguration() {
        // 1. Difficulty (Default: Sedang)
        String difficulty = sessionManager.getInterviewDifficulty();
        if (QuestionBank.DIFFICULTY_MUDAH.equalsIgnoreCase(difficulty) || QuestionBank.DIFFICULTY_BEGINNER.equalsIgnoreCase(difficulty)) {
            rbBeginner.setChecked(true);
        } else if (QuestionBank.DIFFICULTY_SULIT.equalsIgnoreCase(difficulty) || QuestionBank.DIFFICULTY_ADVANCED.equalsIgnoreCase(difficulty)) {
            rbAdvanced.setChecked(true);
        } else {
            rbIntermediate.setChecked(true);
        }

        // 2. Question Count (Default: 5 Soal)
        int count = sessionManager.getInterviewQuestionCount();
        if (count == 3) {
            rbCount3.setChecked(true);
        } else if (count == 10) {
            rbCount10.setChecked(true);
        } else {
            rbCount5.setChecked(true);
        }

        // 3. Language (Default: Bahasa Indonesia)
        String language = sessionManager.getInterviewLanguage();
        if (QuestionBank.LANG_EN.equalsIgnoreCase(language)) {
            rbLangEn.setChecked(true);
        } else {
            rbLangId.setChecked(true);
        }
    }

    private void saveConfiguration() {
        String difficulty = QuestionBank.DIFFICULTY_SEDANG;
        if (rbBeginner.isChecked()) {
            difficulty = QuestionBank.DIFFICULTY_MUDAH;
        } else if (rbAdvanced.isChecked()) {
            difficulty = QuestionBank.DIFFICULTY_SULIT;
        }

        int count = 5;
        if (rbCount3.isChecked()) {
            count = 3;
        } else if (rbCount10.isChecked()) {
            count = 10;
        }

        String language = rbLangEn.isChecked() ? QuestionBank.LANG_EN : QuestionBank.LANG_ID;

        sessionManager.setInterviewDifficulty(difficulty);
        sessionManager.setInterviewQuestionCount(count);
        sessionManager.setInterviewLanguage(language);

        Intent intent = new Intent(InterviewSetupActivity.this, HomeActivity.class);
        intent.putExtra("extra_show_config_success", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
