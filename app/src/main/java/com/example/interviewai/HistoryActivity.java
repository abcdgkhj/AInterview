package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.adapter.HistoryAdapter;
import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.notification.AppNotification;
import com.example.interviewai.model.InterviewSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_SHOW_SAVED_NOTIFICATION = "extra_show_saved_notification";
    public static final String EXTRA_NOTIF_START_TIME = "extra_notif_start_time";

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private Button btnClearHistory;
    private ImageView btnBack;
    private TextView txtHintDeleteHint;
    private BottomNavigationView bottomNavHome;
    private boolean hasShownSavedNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Safe window insets handling for R.id.main
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        rvHistory = findViewById(R.id.rvHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnBack = findViewById(R.id.btnBack);
        txtHintDeleteHint = findViewById(R.id.txtHintDeleteHint);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateToHome());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> showClearConfirmationDialog());
        }

        setupBottomNav();
        loadHistoryData();

        // Tampilkan notifikasi "Menyimpan Hasil" jika Activity dibuka setelah proses simpan dengan sisa durasi timer
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_SHOW_SAVED_NOTIFICATION, false) && !hasShownSavedNotification) {
            hasShownSavedNotification = true;
            long notifStartTime = getIntent().getLongExtra(EXTRA_NOTIF_START_TIME, System.currentTimeMillis());
            getIntent().removeExtra(EXTRA_SHOW_SAVED_NOTIFICATION);
            getIntent().removeExtra(EXTRA_NOTIF_START_TIME);

            long elapsed = System.currentTimeMillis() - notifStartTime;
            long remainingDuration = 3500L - elapsed;

            if (remainingDuration > 400L) {
                if (rootView != null) {
                    rootView.post(() -> AppNotification.showInfo(HistoryActivity.this, "Menyimpan Hasil", "Hasil interview berhasil disimpan.", remainingDuration));
                } else {
                    AppNotification.showInfo(HistoryActivity.this, "Menyimpan Hasil", "Hasil interview berhasil disimpan.", remainingDuration);
                }
            }
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavHome != null && bottomNavHome.getSelectedItemId() != R.id.nav_history) {
            bottomNavHome.setSelectedItemId(R.id.nav_history);
        }
        loadHistoryData();
    }

    private void setupBottomNav() {
        bottomNavHome = findViewById(R.id.bottomNavHome);
        if (bottomNavHome == null) return;
        // Remove Material 3 purple capsule/pill active indicator
        bottomNavHome.setItemActiveIndicatorEnabled(false);
        bottomNavHome.setSelectedItemId(R.id.nav_history);
        bottomNavHome.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                if (rvHistory != null) {
                    rvHistory.smoothScrollToPosition(0);
                }
                return true;
            } else if (itemId == R.id.nav_home) {
                navigateToHome();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadHistoryData() {
        List<InterviewSession> sessions = InterviewStorage.getSessions(this);

        if (sessions == null || sessions.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            if (btnClearHistory != null) btnClearHistory.setVisibility(View.GONE);
            if (txtHintDeleteHint != null) txtHintDeleteHint.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            if (btnClearHistory != null) btnClearHistory.setVisibility(View.VISIBLE);
            if (txtHintDeleteHint != null) txtHintDeleteHint.setVisibility(View.VISIBLE);

            HistoryAdapter adapter = new HistoryAdapter(this, sessions);
            adapter.setOnHistoryChangedListener(() -> {
                // Called when an item is deleted via long-press
                // Reload from storage to sync state
                List<InterviewSession> updated = InterviewStorage.getSessions(this);
                if (updated == null || updated.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                    if (btnClearHistory != null) btnClearHistory.setVisibility(View.GONE);
                    if (txtHintDeleteHint != null) txtHintDeleteHint.setVisibility(View.GONE);
                }
            });
            rvHistory.setAdapter(adapter);
        }
    }

    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Riwayat?")
                .setMessage("Semua catatan hasil interview akan dihapus secara permanen. Tindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    InterviewStorage.clearHistory(this);
                    loadHistoryData();
                    AppNotification.showSuccess(this, "Riwayat Dibersihkan", "Semua riwayat interview berhasil dihapus.");
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppNotification.dismiss(this);
    }
}
