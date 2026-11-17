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

/**
 * Welcome & Onboarding Gateway Screen.
 *
 * Displays the 140dp AInterview brand logo centered on screen (matching Splash Screen exactly),
 * with "Daftar Akun" (navigates to RegisterActivity) and "Sudah Punya Akun? Masuk" (navigates to LoginActivity).
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private Button btnLogin;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure edge-to-edge full screen matching Splash Screen exactly
        setupEdgeToEdgeStatusBar();

        setContentView(R.layout.activity_welcome);

        sessionManager = new UserSessionManager(this);

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

            // Smooth gentle fade-in for buttons container on first entrance
            if (savedInstanceState == null) {
                buttonsContainer.setAlpha(0f);
                buttonsContainer.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(150)
                        .start();
            }
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
