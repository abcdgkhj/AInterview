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

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private Button btnClearHistory;
    private ImageView btnBack;
    private TextView txtHintDeleteHint;
    private BottomNavigationView bottomNavHome;

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
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> showClearConfirmationDialog());
        }

        setupBottomNav();
        loadHistoryData();
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
                Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
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
}
