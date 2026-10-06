package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.model.InterviewItem;
import com.example.interviewai.model.InterviewQuestion;
import com.example.interviewai.model.InterviewSession;
import com.example.interviewai.workflow.InterviewWorkflowManager;

public class InterviewActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_QUESTION_COUNT = "extra_question_count";
    public static final String EXTRA_LANGUAGE = "extra_language";

    private TextView txtQuestionNumber;
    private TextView txtQuestion;
    private TextView txtQuestionTip;
    private TextView txtRoleBadge;
    private TextView txtDifficultyBadge;
    private TextView txtLangBadge;
    private TextView txtProgressPercent;
    private TextView txtWordCount;
    private ProgressBar progressIndicator;
    private EditText edtAnswer;

    // Immediate Feedback Views
    private LinearLayout layoutImmediateFeedback;
    private TextView txtImmediateScore;
    private TextView txtImmediateStrength;
    private TextView txtImmediateImprovement;
    private TextView txtImmediateSuggestion;

    // Buttons
    private Button btnSubmitAnswer;
    private Button btnNextQuestion;
    private Button btnFinishEarly;
    private ImageView btnBack;

    private InterviewWorkflowManager workflowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        // Safe window insets handling for R.id.main
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initializeWorkflow();

        // Tangani kondisi jika bank soal kosong atau parameter tidak valid
        if (!workflowManager.hasQuestions()) {
            handleEmptyQuestionState();
            return;
        }

        initViews();
        setupWordCounter();
        renderCurrentQuestion();
    }

    private void initializeWorkflow() {
        String candidateName = "Kandidat";
        String category = QuestionBank.CATEGORY_GENERAL;
        String difficulty = QuestionBank.DIFFICULTY_INTERMEDIATE;
        String language = QuestionBank.LANG_ID;
        int count = 5;

        if (getIntent() != null) {
            String name = getIntent().getStringExtra(MainActivity.EXTRA_CANDIDATE_NAME);
            if (name != null && !name.trim().isEmpty()) {
                candidateName = name.trim();
            }

            String cat = getIntent().getStringExtra(MainActivity.EXTRA_ROLE_CATEGORY);
            if (cat != null && !cat.trim().isEmpty()) {
                category = cat;
            }

            String diff = getIntent().getStringExtra(EXTRA_DIFFICULTY);
            if (diff != null && !diff.trim().isEmpty()) {
                difficulty = diff;
            }

            String lang = getIntent().getStringExtra(EXTRA_LANGUAGE);
            if (lang != null && !lang.trim().isEmpty()) {
                language = lang;
            }

            count = getIntent().getIntExtra(EXTRA_QUESTION_COUNT, 5);
        }

        workflowManager = new InterviewWorkflowManager(candidateName, category, difficulty, language, count);
    }

    private void handleEmptyQuestionState() {
        new AlertDialog.Builder(this)
                .setTitle("Pertanyaan Tidak Ditemukan")
                .setMessage("Tidak ada pertanyaan yang sesuai dengan kriteria yang Anda pilih. Silakan pilih kategori atau kesulitan lain.")
                .setCancelable(false)
                .setPositiveButton("Kembali ke Setup", (dialog, which) -> finish())
                .show();
    }

    private void initViews() {
        txtQuestionNumber = findViewById(R.id.txtQuestionNumber);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtQuestionTip = findViewById(R.id.txtQuestionTip);
        txtRoleBadge = findViewById(R.id.txtRoleBadge);
        txtDifficultyBadge = findViewById(R.id.txtDifficultyBadge);
        txtLangBadge = findViewById(R.id.txtLangBadge);
        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        txtWordCount = findViewById(R.id.txtWordCount);
        progressIndicator = findViewById(R.id.progressIndicator);
        edtAnswer = findViewById(R.id.edtAnswer);

        layoutImmediateFeedback = findViewById(R.id.layoutImmediateFeedback);
        txtImmediateScore = findViewById(R.id.txtImmediateScore);
        txtImmediateStrength = findViewById(R.id.txtImmediateStrength);
        txtImmediateImprovement = findViewById(R.id.txtImmediateImprovement);
        txtImmediateSuggestion = findViewById(R.id.txtImmediateSuggestion);

        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnFinishEarly = findViewById(R.id.btnFinishEarly);
        btnBack = findViewById(R.id.btnBack);

        if (txtRoleBadge != null) {
            txtRoleBadge.setText(workflowManager.getCategory());
        }
        if (txtDifficultyBadge != null) {
            txtDifficultyBadge.setText(workflowManager.getDifficulty());
        }
        if (txtLangBadge != null) {
            boolean isEn = "English".equalsIgnoreCase(workflowManager.getLanguage());
            txtLangBadge.setText(isEn ? "EN" : "ID");
        }

        btnSubmitAnswer.setOnClickListener(v -> handleSubmitAnswerAction());
        btnNextQuestion.setOnClickListener(v -> handleNextOrFinishAction());

        if (btnFinishEarly != null) {
            btnFinishEarly.setOnClickListener(v -> {
                saveInputToManager();
                promptFinishConfirmation();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> showExitConfirmationDialog());
        }
    }

    private void saveInputToManager() {
        String answer = edtAnswer.getText() != null ? edtAnswer.getText().toString() : "";
        workflowManager.saveCurrentAnswer(answer);
    }

    /**
     * Alur: User Answer -> Submit -> Show Immediate Feedback -> Show Next Question button
     */
    private void handleSubmitAnswerAction() {
        String answerText = edtAnswer.getText() != null ? edtAnswer.getText().toString().trim() : "";
        if (answerText.isEmpty()) {
            Toast.makeText(this, R.string.toast_empty_answer, Toast.LENGTH_SHORT).show();
            return;
        }

        saveInputToManager();

        // Evaluasi jawaban menggunakan template heuristic
        InterviewItem feedbackItem = workflowManager.evaluateCurrentAnswer();
        if (feedbackItem != null) {
            displayFeedback(feedbackItem);
        }
    }

    private void displayFeedback(InterviewItem feedbackItem) {
        if (layoutImmediateFeedback != null) {
            layoutImmediateFeedback.setVisibility(View.VISIBLE);
        }
        if (txtImmediateScore != null) {
            txtImmediateScore.setText("Skor: " + feedbackItem.getScore());
        }
        if (txtImmediateStrength != null) {
            txtImmediateStrength.setText(feedbackItem.getStrength());
        }
        if (txtImmediateImprovement != null) {
            txtImmediateImprovement.setText(feedbackItem.getImprovement());
        }
        if (txtImmediateSuggestion != null) {
            txtImmediateSuggestion.setText(feedbackItem.getSampleAnswer());
        }

        // Lock answer field after submitting
        edtAnswer.setEnabled(false);

        // Hide submit button, show next/finish button
        btnSubmitAnswer.setVisibility(View.GONE);
        btnNextQuestion.setVisibility(View.VISIBLE);

        if (workflowManager.isLastQuestion()) {
            btnNextQuestion.setText(getString(R.string.btn_finish_interview));
        } else {
            btnNextQuestion.setText(getString(R.string.btn_next));
        }
    }

    private void handleNextOrFinishAction() {
        if (workflowManager.isLastQuestion()) {
            promptFinishConfirmation();
        } else {
            if (workflowManager.goToNext()) {
                renderCurrentQuestion();
            }
        }
    }

    private void renderCurrentQuestion() {
        InterviewQuestion q = workflowManager.getCurrentQuestion();
        if (q == null) return;

        int total = workflowManager.getTotalQuestions();
        int currentNum = workflowManager.getCurrentQuestionNumber();
        int progress = workflowManager.getProgressPercentage();

        txtQuestionNumber.setText(getString(R.string.question_number_format, currentNum, total));
        txtQuestion.setText(q.getQuestionText());

        if (txtQuestionTip != null) {
            txtQuestionTip.setText(q.getTip());
        }

        if (progressIndicator != null) {
            progressIndicator.setProgress(progress);
        }

        if (txtProgressPercent != null) {
            txtProgressPercent.setText(progress + "%");
        }

        // Check if current question has already been evaluated
        if (workflowManager.isCurrentQuestionEvaluated()) {
            InterviewItem evaluatedItem = workflowManager.getCurrentEvaluatedItem();
            edtAnswer.setText(workflowManager.getCurrentAnswer());
            edtAnswer.setEnabled(false);
            if (evaluatedItem != null) {
                displayFeedback(evaluatedItem);
            }
        } else {
            // New unanswered question state: Question -> User Answer -> Submit
            edtAnswer.setEnabled(true);
            String savedAnswer = workflowManager.getCurrentAnswer();
            edtAnswer.setText(savedAnswer);
            edtAnswer.setSelection(edtAnswer.getText().length());

            if (layoutImmediateFeedback != null) {
                layoutImmediateFeedback.setVisibility(View.GONE);
            }
            btnSubmitAnswer.setVisibility(View.VISIBLE);
            btnNextQuestion.setVisibility(View.GONE);
        }
    }

    private void setupWordCounter() {
        edtAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (s != null) ? s.toString().trim() : "";
                int countWords = text.isEmpty() ? 0 : text.split("\\s+").length;
                if (txtWordCount != null) {
                    txtWordCount.setText(countWords + " kata");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void promptFinishConfirmation() {
        saveInputToManager();

        int unansweredCount = workflowManager.getUnansweredCount();
        String message;
        if (unansweredCount > 0) {
            message = getString(R.string.confirm_finish_partial, unansweredCount);
        } else {
            message = getString(R.string.confirm_finish_all_answered);
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_finish_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.btn_confirm_finish), (dialog, which) -> completeAndNavigate())
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void completeAndNavigate() {
        // Mencegah submit ganda
        if (workflowManager.isSubmitting()) {
            return;
        }
        workflowManager.setSubmitting(true);

        Toast.makeText(this, "Menyimpan hasil interview...", Toast.LENGTH_SHORT).show();

        // Bentuk sesi yang telah selesai
        InterviewSession session = workflowManager.createCompletedSession();

        // Simpan sesi ke SharedPreferences lokal
        InterviewStorage.saveSession(this, session);

        // Buka ResultActivity
        Intent intent = new Intent(InterviewActivity.this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SESSION, session);
        startActivity(intent);
        finish();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_exit_title))
                .setMessage(getString(R.string.confirm_exit_message))
                .setPositiveButton("Ya, Keluar", (dialog, which) -> finish())
                .setNegativeButton("Lanjutkan Latihan", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}