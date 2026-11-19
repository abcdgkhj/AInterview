package com.example.interviewai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.notification.AppNotification;

/**
 * Welcome & Onboarding Gateway Screen (Startup Entry Point).
 *
 * Seamlessly transitions from the Android System Splash Screen into the welcome screen
 * without flicker, screen blinking, or duplicate splash delay.
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private Button btnLogin;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Switch from cold-start splash theme to normal application theme seamlessly
        setTheme(R.style.Theme_InterviewAI);
        super.onCreate(savedInstanceState);

        sessionManager = new UserSessionManager(this);

        // If user is already logged in, bypass welcome and proceed directly to HomeActivity
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            finish();
            return;
        }

        // Configure edge-to-edge full screen matching system splash background
        setupEdgeToEdgeStatusBar();

        setContentView(R.layout.activity_welcome);

        // Apply insets only to buttons container so logo stays at true physical screen center
        View buttonsContainer = findViewById(R.id.buttonsContainer);
        if (buttonsContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(buttonsContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom + (int) (32 * getResources().getDisplayMetrics().density)
                );
                return insets;
            });
        }

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnLogin = findViewById(R.id.btnLogin);

        // Button 1: "Daftar Akun" -> Navigates directly to RegisterActivity (Formulir Data Diri)
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Button 2: "Sudah Punya Akun? Masuk" -> Navigates to LoginActivity
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Handle logout success notification if redirected from ProfileActivity
        if (getIntent() != null && getIntent().getBooleanExtra("extra_show_logout_success", false)) {
            AppNotification.showSuccess(this, "Berhasil Keluar", "Anda telah keluar dari akun.");
            getIntent().removeExtra("extra_show_logout_success");
        }
    }

    private void setupEdgeToEdgeStatusBar() {
        Window window = getWindow();
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            // Dark/black text and icons on pure white background
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }
    }
}
