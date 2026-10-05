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

    private View cardCatGeneral;
    private View cardCatSoftware;
    private View cardCatWeb;
    private View cardCatDatabase;
    private View cardCatBehavioral;

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

        cardCatGeneral = findViewById(R.id.cardCatGeneral);
        cardCatSoftware = findViewById(R.id.cardCatSoftware);
        cardCatWeb = findViewById(R.id.cardCatWeb);
        cardCatDatabase = findViewById(R.id.cardCatDatabase);
        cardCatBehavioral = findViewById(R.id.cardCatBehavioral);

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
        btnMainStartInterview.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_GENERAL));

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
                    intent.putExtra(ResultActivity.EXTRA_SESSION, sessions.get(0));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Belum ada sesi yang selesai. Selesaikan interview pertama Anda!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Category Cards
        if (cardCatGeneral != null) {
            cardCatGeneral.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_GENERAL));
        }
        if (cardCatSoftware != null) {
            cardCatSoftware.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_SOFTWARE));
        }
        if (cardCatWeb != null) {
            cardCatWeb.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_WEB));
        }
        if (cardCatDatabase != null) {
            cardCatDatabase.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_DATABASE));
        }
        if (cardCatBehavioral != null) {
            cardCatBehavioral.setOnClickListener(v -> openSetup(QuestionBank.CATEGORY_BEHAVIORAL));
        }
    }

    private void openSetup(String category) {
        Intent intent = new Intent(HomeActivity.this, InterviewSetupActivity.class);
        intent.putExtra(EXTRA_INITIAL_CATEGORY, category);
        startActivity(intent);
    }
}
