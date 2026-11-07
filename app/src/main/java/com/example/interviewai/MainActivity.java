package com.example.interviewai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.interviewai.data.UserSessionManager;

/**
 * Splash Screen & Entry Point Router Activity.
 *
 * <p>On Android 12+ (API 31+), the system displays the splash screen automatically
 * using {@code windowSplashScreenBackground} and {@code windowSplashScreenAnimatedIcon}
 * defined in the theme. This Activity then navigates immediately to avoid showing
 * a second splash layout (which would cause the logo to "jump" in size).
 *
 * <p>On pre-Android 12, the splash is shown via the window background
 * ({@code bg_splash_window.xml}) and a brief layout display with a gentle fade-in
 * animation before navigating to the next screen.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CANDIDATE_NAME = "extra_candidate_name";
    public static final String EXTRA_ROLE_CATEGORY = "extra_role_category";

    /** Splash duration for pre-Android 12 devices only. */
    private static final long SPLASH_DURATION_PRE12_MS = 1200L;

    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure edge-to-edge full screen with translucent status bar
        setupEdgeToEdgeStatusBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // ============================================================
            // Android 12+ (API 31+): System splash screen is already shown.
            // Navigate immediately — DO NOT show a second splash layout.
            // The system splash smoothly transitions into the destination.
            // ============================================================
            navigateToNextScreen();
        } else {
            // ============================================================
            // Pre-Android 12: Show splash layout with gentle fade-in.
            // The window background (bg_splash_window.xml) already displays
            // the logo at 92dp centered on the lavender gradient, so the
            // transition from window background to layout is seamless.
            // ============================================================
            setContentView(R.layout.activity_splash);

            ImageView imgLogo = findViewById(R.id.imgSplashLogo);
            if (imgLogo != null) {
                // Gentle fade-in only — no scale animation to avoid logo "jumping"
                imgLogo.setAlpha(0f);
                imgLogo.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            // Navigate after brief display
            splashHandler.postDelayed(this::navigateToNextScreen, SPLASH_DURATION_PRE12_MS);
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

    private void navigateToNextScreen() {
        if (hasNavigated || isFinishing() || isDestroyed()) {
            return;
        }
        hasNavigated = true;

        UserSessionManager sessionManager = new UserSessionManager(this);

        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(MainActivity.this, WelcomeActivity.class);
        }

        // Forward any incoming intent extras
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashHandler.removeCallbacksAndMessages(null);
    }
}
