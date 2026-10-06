package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.adapter.FeedbackAdapter;
import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.model.InterviewSession;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION = "extra_interview_session";

    private TextView txtCandidateRole;
    private TextView txtOverallScore;
    private TextView txtSummaryStatus;
    private TextView txtAnsweredStat;
    private TextView txtSummaryFeedback;
    private RecyclerView rvFeedback;
    private Button btnRetake;
    private Button btnHome;
    private Button btnViewHistory;
    private ImageView btnBack;

    private InterviewSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Safe window insets handling for R.id.main
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        txtCandidateRole = findViewById(R.id.txtCandidateRole);
        txtOverallScore = findViewById(R.id.txtOverallScore);
        txtSummaryStatus = findViewById(R.id.txtSummaryStatus);
        txtAnsweredStat = findViewById(R.id.txtAnsweredStat);
        txtSummaryFeedback = findViewById(R.id.txtSummaryFeedback);
        rvFeedback = findViewById(R.id.rvFeedback);
        btnRetake = findViewById(R.id.btnRetake);
        btnHome = findViewById(R.id.btnHome);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnBack = findViewById(R.id.btnBack);

        // Ambil data session dari Intent
        if (getIntent() != null && getIntent().hasExtra(EXTRA_SESSION)) {
            session = (InterviewSession) getIntent().getSerializableExtra(EXTRA_SESSION);
        }

        String sessionIdExtra = getIntent() != null ? getIntent().getStringExtra("extra_session_id") : null;
        if (sessionIdExtra != null && (session == null || session.getItems() == null || session.getItems().isEmpty())) {
            List<InterviewSession> storedSessions = InterviewStorage.getSessions(this);
            for (InterviewSession s : storedSessions) {
                if (sessionIdExtra.equals(s.getSessionId())) {
                    session = s;
                    break;
                }
            }
        }

        // Fallback jika session tidak ada dari intent: ambil sesi terbaru dari storage
        if (session == null) {
            List<InterviewSession> storedSessions = InterviewStorage.getSessions(this);
            if (storedSessions != null && !storedSessions.isEmpty()) {
                session = storedSessions.get(0);
            }
        }

        displaySessionData();
        setupActions();
    }

    private void ensureCompleteSessionItems(InterviewSession session) {
        if (session == null || session.getItems() == null) return;
        List<com.example.interviewai.model.InterviewItem> items = session.getItems();
        int targetCount = Math.max(5, session.getTotalQuestions());
        if (items.size() < targetCount) {
            List<com.example.interviewai.model.InterviewQuestion> fallbackQuestions =
                    com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                            session.getRoleCategory(),
                            session.getDifficulty(),
                            session.getLanguage(),
                            targetCount
                    );
            for (int i = items.size(); i < targetCount; i++) {
                if (i < fallbackQuestions.size()) {
                    com.example.interviewai.model.InterviewQuestion q = fallbackQuestions.get(i);
                    items.add(com.example.interviewai.workflow.InterviewWorkflowManager.evaluateAnswer(
                            q,
                            "",
                            session.getLanguage()
                    ));
                }
            }
        }
    }

    private void displaySessionData() {
        if (session == null) {
            Toast.makeText(this, "Tidak ada data sesi untuk ditampilkan.", Toast.LENGTH_SHORT).show();
            navigateToHome();
            return;
        }

        ensureCompleteSessionItems(session);

        String diff = session.getDifficulty() != null ? session.getDifficulty() : "Intermediate";
        String meta = session.getCandidateName() + " • " + session.getRoleCategory() + " (" + diff + ")";
        txtCandidateRole.setText(meta);

        // Tampilkan persentase keterisian latihan
        txtOverallScore.setText(String.valueOf(session.getOverallScore()));
        txtSummaryStatus.setText(session.getSummaryStatus());
        txtSummaryFeedback.setText(session.getSummaryFeedback());

        if (txtAnsweredStat != null) {
            int answered = session.getAnsweredQuestionsCount();
            int total = session.getTotalQuestions();
            txtAnsweredStat.setText(getString(R.string.result_answered_stat, answered, total));
        }

        // Setup RecyclerView daftar tinjauan jawaban
        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        rvFeedback.setNestedScrollingEnabled(false);
        FeedbackAdapter adapter = new FeedbackAdapter(session.getItems());
        rvFeedback.setAdapter(adapter);
    }

    private void setupActions() {
        if (btnRetake != null) {
            btnRetake.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, InterviewActivity.class);
                if (session != null) {
                    intent.putExtra(MainActivity.EXTRA_CANDIDATE_NAME, session.getCandidateName());
                    intent.putExtra(MainActivity.EXTRA_ROLE_CATEGORY, session.getRoleCategory());
                    intent.putExtra(InterviewActivity.EXTRA_DIFFICULTY, session.getDifficulty());
                    intent.putExtra(InterviewActivity.EXTRA_LANGUAGE, session.getLanguage());
                    intent.putExtra(InterviewActivity.EXTRA_QUESTION_COUNT, session.getTotalQuestions());
                }
                startActivity(intent);
                finish();
            });
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> navigateToHome());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateToHome());
        }

        if (btnViewHistory != null) {
            btnViewHistory.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, HistoryActivity.class);
                startActivity(intent);
            });
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }
}
