package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.User;

import java.util.List;

public class InterviewSetupActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_QUESTION_COUNT = "extra_question_count";
    public static final String EXTRA_LANGUAGE = "extra_language";

    private EditText edtCandidateName;
    private Spinner spnCategory;
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

    private Button btnStartSession;
    private ImageView btnBack;

    private UserSessionManager sessionManager;
    private String initialCategory = QuestionBank.CATEGORY_GENERAL;

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

        if (getIntent() != null && getIntent().hasExtra(HomeActivity.EXTRA_INITIAL_CATEGORY)) {
            String cat = getIntent().getStringExtra(HomeActivity.EXTRA_INITIAL_CATEGORY);
            if (cat != null && !cat.isEmpty()) {
                initialCategory = cat;
            }
        }

        edtCandidateName = findViewById(R.id.edtCandidateName);
        spnCategory = findViewById(R.id.spnCategory);
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

        btnStartSession = findViewById(R.id.btnStartSession);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        prefillUserData();
        setupCategorySpinner();

        btnStartSession.setOnClickListener(v -> startInterview());
    }

    private void prefillUserData() {
        User user = sessionManager.getCurrentUser();
        if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
            edtCandidateName.setText(user.getName().trim());
        } else {
            edtCandidateName.setText("Kandidat");
        }

        String defaultLang = sessionManager.getDefaultLanguage();
        if ("English".equalsIgnoreCase(defaultLang)) {
            rbLangEn.setChecked(true);
        } else {
            rbLangId.setChecked(true);
        }
    }

    private void setupCategorySpinner() {
        List<String> categories = QuestionBank.getAvailableCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(adapter);

        int selectedIndex = categories.indexOf(initialCategory);
        if (selectedIndex >= 0) {
            spnCategory.setSelection(selectedIndex);
        }
    }

    private void startInterview() {
        String candidateName = edtCandidateName.getText().toString().trim();
        if (candidateName.isEmpty()) {
            candidateName = "Kandidat";
        }

        String category = QuestionBank.CATEGORY_GENERAL;
        if (spnCategory.getSelectedItem() != null) {
            category = spnCategory.getSelectedItem().toString();
        }

        String difficulty = QuestionBank.DIFFICULTY_INTERMEDIATE;
        if (rbBeginner.isChecked()) {
            difficulty = QuestionBank.DIFFICULTY_BEGINNER;
        } else if (rbAdvanced.isChecked()) {
            difficulty = QuestionBank.DIFFICULTY_ADVANCED;
        }

        int count = 5;
        if (rbCount3.isChecked()) {
            count = 3;
        } else if (rbCount10.isChecked()) {
            count = 10;
        }

        String language = rbLangEn.isChecked() ? QuestionBank.LANG_EN : QuestionBank.LANG_ID;

        Intent intent = new Intent(InterviewSetupActivity.this, InterviewActivity.class);
        intent.putExtra(MainActivity.EXTRA_CANDIDATE_NAME, candidateName);
        intent.putExtra(MainActivity.EXTRA_ROLE_CATEGORY, category);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(EXTRA_QUESTION_COUNT, count);
        intent.putExtra(EXTRA_LANGUAGE, language);
        startActivity(intent);
    }
}
