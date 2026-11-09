package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.interviewai.adapter.HeroCarouselAdapter;
import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.HeroSlideItem;
import com.example.interviewai.model.User;
import com.example.interviewai.notification.AppNotification;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_CATEGORY = "extra_initial_category";
    private static final long AUTO_SLIDE_INTERVAL_MS = 5000L;

    private TextView txtGreeting;
    private NestedScrollView scrollHome;
    private View txtSectionCategories;

    private View cardInterviewUmum;
    private View cardInterviewTeknis;

    private ViewPager2 pagerHeroCarousel;
    private ImageView[] dots;
    private BottomNavigationView bottomNavHome;

    private UserSessionManager sessionManager;

    private final Handler autoSlideHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSlideRunnable;
    private boolean isAutoSlideRunning = false;
    private int slideCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new UserSessionManager(this);

        // Safe insets handling for R.id.main (White background, light status bar)
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        txtGreeting = findViewById(R.id.txtGreeting);
        scrollHome = findViewById(R.id.scrollHome);
        txtSectionCategories = findViewById(R.id.txtSectionCategories);

        cardInterviewUmum = findViewById(R.id.cardInterviewUmum);
        cardInterviewTeknis = findViewById(R.id.cardInterviewTeknis);

        pagerHeroCarousel = findViewById(R.id.pagerHeroCarousel);
        bottomNavHome = findViewById(R.id.bottomNavHome);

        setupCarousel();
        setupBottomNav();
        setupEventHandlers();

        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("extra_show_welcome", false)) {
                User u = sessionManager.getCurrentUser();
                String name = (u != null && u.getName() != null) ? u.getName().trim() : "Kandidat";
                AppNotification.showSuccess(this, "Selamat Datang", "Selamat datang kembali, " + name + "!");
                getIntent().removeExtra("extra_show_welcome");
            } else if (getIntent().getBooleanExtra("extra_show_register_success", false)) {
                AppNotification.showSuccess(this, "Pendaftaran Berhasil", "Akun AInterview Anda berhasil dibuat!");
                getIntent().removeExtra("extra_show_register_success");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserData();
        if (bottomNavHome != null) {
            bottomNavHome.setSelectedItemId(R.id.nav_home);
        }
        startAutoSlide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoSlide();
    }

    private void refreshUserData() {
        User user = sessionManager.getCurrentUser();
        if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
            txtGreeting.setText(getString(R.string.greeting_format, user.getName().trim()));
        } else {
            txtGreeting.setText("Halo, Kandidat!");
        }
    }

    private void setupCarousel() {
        List<HeroSlideItem> slides = new ArrayList<>();
        slides.add(new HeroSlideItem(R.drawable.onboarding_slide_1));
        slides.add(new HeroSlideItem(R.drawable.onboarding_slide_2));
        slides.add(new HeroSlideItem(R.drawable.onboarding_slide_3));
        slides.add(new HeroSlideItem(R.drawable.onboarding_slide_4));

        slideCount = slides.size();

        HeroCarouselAdapter adapter = new HeroCarouselAdapter(slides);
        pagerHeroCarousel.setAdapter(adapter);

        dots = new ImageView[4];
        dots[0] = findViewById(R.id.dot0);
        dots[1] = findViewById(R.id.dot1);
        dots[2] = findViewById(R.id.dot2);
        dots[3] = findViewById(R.id.dot3);

        updateDots(0);

        pagerHeroCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
            }
        });

        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                if (pagerHeroCarousel != null && slideCount > 0) {
                    int nextItem = (pagerHeroCarousel.getCurrentItem() + 1) % slideCount;
                    pagerHeroCarousel.setCurrentItem(nextItem, true);
                }
                autoSlideHandler.postDelayed(this, AUTO_SLIDE_INTERVAL_MS);
            }
        };
    }

    private void startAutoSlide() {
        if (!isAutoSlideRunning && autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            autoSlideHandler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL_MS);
            isAutoSlideRunning = true;
        }
    }

    private void stopAutoSlide() {
        if (autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            isAutoSlideRunning = false;
        }
    }

    private void updateDots(int activePosition) {
        if (dots == null) return;
        float density = getResources().getDisplayMetrics().density;
        int widthActive = (int) (26 * density + 0.5f);
        int widthInactive = (int) (8 * density + 0.5f);
        int height = (int) (8 * density + 0.5f);
        for (int i = 0; i < dots.length; i++) {
            if (dots[i] != null) {
                boolean isActive = (i == activePosition);
                dots[i].setImageResource(isActive ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive);
                ViewGroup.LayoutParams lp = dots[i].getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    // keep margins if any
                }
                lp.width = isActive ? widthActive : widthInactive;
                lp.height = height;
                dots[i].setLayoutParams(lp);
            }
        }
    }

    private void scrollToInterviewOptions() {
        if (scrollHome != null && txtSectionCategories != null) {
            scrollHome.smoothScrollTo(0, txtSectionCategories.getTop() - 20);
        }
    }

    private void setupBottomNav() {
        if (bottomNavHome == null) return;
        bottomNavHome.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home, scroll to top
                if (scrollHome != null) {
                    scrollHome.smoothScrollTo(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupEventHandlers() {
        // 1. Interview Umum -> langsung mulai sesi latihan tanpa memilih bidang
        if (cardInterviewUmum != null) {
            cardInterviewUmum.setOnClickListener(v -> startInterviewUmum());
        }

        // 2. Interview Teknis -> buka layar pemilihan bidang (10 bidang keahlian)
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
