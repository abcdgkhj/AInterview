package com.example.interviewai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.interviewai.data.UserSessionManager;

/**
 * Splash Screen & Entry Point Router Activity.
 *
 * Displays the clean white splash screen with the 140dp AInterview brand logo
 * centered on screen, then transitions smoothly directly to WelcomeActivity (or HomeActivity if logged in).
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CANDIDATE_NAME = "extra_candidate_name";
    public static final String EXTRA_ROLE_CATEGORY = "extra_role_category";

    /** Splash duration before transitioning smoothly to the next screen (1200ms). */
    private static final long SPLASH_DURATION_MS = 1200L;

    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure edge-to-edge full screen with translucent status bar
        setupEdgeToEdgeStatusBar();

        // Show the official AInterview Splash layout with matching 140dp centered logo
        setContentView(R.layout.activity_splash);

        // Navigate directly after brief display
        splashHandler.postDelayed(this::navigateToNextScreen, SPLASH_DURATION_MS);
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

    private void navigateToNextScreen() {
        if (hasNavigated || isFinishing() || isDestroyed()) {
            return;
        }
        hasNavigated = true;

        UserSessionManager sessionManager = new UserSessionManager(this);

        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            // Direct smooth navigation from Splash Screen to Welcome Activity (no intermediary screens)
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashHandler.removeCallbacksAndMessages(null);
    }
}
