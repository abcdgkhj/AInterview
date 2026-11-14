package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.adapter.FeedbackAdapter;
import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.model.InterviewSession;

import com.example.interviewai.notification.AppNotification;

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });
    }

    private void ensureCompleteSessionItems(InterviewSession session) {
        // Jangan pernah memaksakan jumlah pertanyaan sesi menjadi 5.
        // Jumlah pertanyaan murni mengikuti jumlah yang dipilih saat interview.
    }

    private void displaySessionData() {
        if (session == null) {
            AppNotification.showWarning(this, "Data Sesi", "Tidak ada data sesi untuk ditampilkan.");
            navigateToHome();
            return;
        }

        String diff = session.getDifficulty() != null ? session.getDifficulty() : "Intermediate";
        String meta = session.getCandidateName() + " • " + session.getRoleCategory() + " (" + diff + ")";
        txtCandidateRole.setText(meta);

        // Tampilkan skor akhir interview
        txtOverallScore.setText(String.valueOf(session.getOverallScore()));
        txtSummaryStatus.setText(session.getSummaryStatus());

        // Hapus teks ringkasan yang tidak relevan / tidak sesuai kondisi jawaban
        String feedback = session.getSummaryFeedback();
        if (txtSummaryFeedback != null) {
            if (feedback == null || feedback.trim().isEmpty()
                    || feedback.contains("Jawaban Anda masih singkat atau belum terjawab lengkap")
                    || feedback.contains("Answers were brief or partially incomplete")) {
                txtSummaryFeedback.setVisibility(View.GONE);
            } else {
                txtSummaryFeedback.setText(feedback);
                txtSummaryFeedback.setVisibility(View.VISIBLE);
            }
        }

        // Tampilkan jumlah pertanyaan terjawab secara dinamis: X dari Y
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
}
