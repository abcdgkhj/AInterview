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

import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.InterviewSession;
import com.example.interviewai.model.User;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_CATEGORY = "extra_initial_category";

    private TextView txtGreeting;
    private TextView txtHistorySessionCount;
    private TextView txtLatestScore;
    private ImageView btnHeaderProfile;
    private Button btnMainStartInterview;

    private View cardQuickHistory;
    private View cardQuickResult;
    private View cardQuickProfile;

    private View cardInterviewUmum;
    private View cardInterviewTeknis;

    private UserSessionManager sessionManager;
    private List<InterviewSession> sessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        txtGreeting = findViewById(R.id.txtGreeting);
        txtHistorySessionCount = findViewById(R.id.txtHistorySessionCount);
        txtLatestScore = findViewById(R.id.txtLatestScore);
        btnHeaderProfile = findViewById(R.id.btnHeaderProfile);
        btnMainStartInterview = findViewById(R.id.btnMainStartInterview);

        cardQuickHistory = findViewById(R.id.cardQuickHistory);
        cardQuickResult = findViewById(R.id.cardQuickResult);
        cardQuickProfile = findViewById(R.id.cardQuickProfile);

        cardInterviewUmum = findViewById(R.id.cardInterviewUmum);
        cardInterviewTeknis = findViewById(R.id.cardInterviewTeknis);

        setupEventHandlers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserDataAndStats();
    }

    private void refreshUserDataAndStats() {
        User user = sessionManager.getCurrentUser();
        if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
            txtGreeting.setText(getString(R.string.greeting_format, user.getName().trim()));
        } else {
            txtGreeting.setText("Halo, Kandidat!");
        }

        sessions = InterviewStorage.getSessions(this);
        int count = (sessions != null) ? sessions.size() : 0;
        txtHistorySessionCount.setText(count + " Sesi");

        if (count > 0 && sessions != null && !sessions.isEmpty()) {
            InterviewSession latest = sessions.get(0);
            txtLatestScore.setText("Skor " + latest.getOverallScore());
        } else {
            txtLatestScore.setText("Belum Ada");
        }
    }

    private void setupEventHandlers() {
        btnMainStartInterview.setOnClickListener(v -> startInterviewUmum());

        View.OnClickListener profileListener = v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        };
        if (btnHeaderProfile != null) btnHeaderProfile.setOnClickListener(profileListener);
        if (cardQuickProfile != null) cardQuickProfile.setOnClickListener(profileListener);

        if (cardQuickHistory != null) {
            cardQuickHistory.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            });
        }

        if (cardQuickResult != null) {
            cardQuickResult.setOnClickListener(v -> {
                if (sessions != null && !sessions.isEmpty()) {
                    Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                    InterviewSession latest = sessions.get(0);
                    intent.putExtra(ResultActivity.EXTRA_SESSION, latest);
                    intent.putExtra("extra_session_id", latest.getSessionId());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Belum ada sesi yang selesai. Selesaikan interview pertama Anda!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 1. Interview Umum -> langsung mulai sesi latihan tanpa memilih bidang
        if (cardInterviewUmum != null) {
            cardInterviewUmum.setOnClickListener(v -> startInterviewUmum());
        }

        // 2. Interview Teknis -> buka layar pemilihan bidang (IT, Marketing, Accounting)
        if (cardInterviewTeknis != null) {
            cardInterviewTeknis.setOnClickListener(v -> openTechnicalFieldSelection());
        }
    }

    private void startInterviewUmum() {
        User user = sessionManager.getCurrentUser();
        String candidateName = (user != null && user.getName() != null && !user.getName().trim().isEmpty())
                ? user.getName().trim() : "Kandidat";

        Intent intent = new Intent(HomeActivity.this, InterviewActivity.class);
        intent.putExtra(MainActivity.EXTRA_CANDIDATE_NAME, candidateName);
        intent.putExtra(MainActivity.EXTRA_ROLE_CATEGORY, QuestionBank.CATEGORY_UMUM);
        intent.putExtra(InterviewActivity.EXTRA_DIFFICULTY, QuestionBank.DIFFICULTY_INTERMEDIATE);
        intent.putExtra(InterviewActivity.EXTRA_QUESTION_COUNT, 5);
        intent.putExtra(InterviewActivity.EXTRA_LANGUAGE, sessionManager.getDefaultLanguage());
        startActivity(intent);
    }

    private void openTechnicalFieldSelection() {
        Intent intent = new Intent(HomeActivity.this, TechnicalFieldSelectionActivity.class);
        startActivity(intent);
    }
}
