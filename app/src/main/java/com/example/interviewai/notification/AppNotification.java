package com.example.interviewai.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.interviewai.R;
import com.google.android.material.card.MaterialCardView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Modern In-App Notification Manager for AInterview.
 * Renders a guaranteed bright, crisp, high-contrast floating banner directly in the
 * active Activity window to bypass Android's system Toast dark-mode/window limitations.
 */
public class AppNotification {

    public enum Type {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        AI_PROCESS
    }

    public static final long DURATION_SHORT = 2800L;
    public static final long DURATION_MEDIUM = 3800L;
    public static final long DURATION_LONG = 5000L;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Map<Activity, ActiveNotification> ACTIVE_NOTIFICATIONS = new WeakHashMap<>();

    private static class ActiveNotification {
        final View container;
        final Runnable dismissRunnable;
        boolean isDismissing = false;

        ActiveNotification(View container, Runnable dismissRunnable) {
            this.container = container;
            this.dismissRunnable = dismissRunnable;
        }
    }

    // --- Public Convenience Showers ---

    public static void showSuccess(@Nullable Context context, @NonNull String message) {
        show(context, Type.SUCCESS, "Berhasil", message, DURATION_SHORT);
    }

    public static void showSuccess(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.SUCCESS, title, message, DURATION_SHORT);
    }

    public static void showSuccess(@Nullable Context context, @NonNull String title, @NonNull String message, long durationMs) {
        show(context, Type.SUCCESS, title, message, durationMs);
    }

    public static void showError(@Nullable Context context, @NonNull String message) {
        show(context, Type.ERROR, "Terjadi Kesalahan", message, DURATION_LONG);
    }

    public static void showError(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.ERROR, title, message, DURATION_LONG);
    }

    public static void showWarning(@Nullable Context context, @NonNull String message) {
        show(context, Type.WARNING, "Perhatian", message, DURATION_MEDIUM);
    }

    public static void showWarning(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.WARNING, title, message, DURATION_MEDIUM);
    }

    public static void showInfo(@Nullable Context context, @NonNull String message) {
        show(context, Type.INFO, "Informasi", message, DURATION_SHORT);
    }

    public static void showInfo(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.INFO, title, message, DURATION_SHORT);
    }

    public static void showAiProcess(@Nullable Context context, @NonNull String message) {
        show(context, Type.AI_PROCESS, "AInterview AI", message, DURATION_LONG);
    }

    public static void showAiProcess(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.AI_PROCESS, title, message, DURATION_LONG);
    }

    public static void showAiSuccess(@Nullable Context context, @NonNull String message) {
        show(context, Type.AI_PROCESS, "AInterview AI", message, DURATION_SHORT);
    }

    public static void showAiSuccess(@Nullable Context context, @NonNull String title, @NonNull String message) {
        show(context, Type.AI_PROCESS, title, message, DURATION_SHORT);
    }

    /**
     * Master method to display the custom in-app bright notification banner.
     */
    public static void show(@Nullable Context context,
                            @NonNull Type type,
                            @NonNull String title,
                            @NonNull String message,
                            long durationMs) {
        if (context == null) return;

        // Ensure execution on UI thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN_HANDLER.post(() -> show(context, type, title, message, durationMs));
            return;
        }

        Activity activity = resolveActivity(context);
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup targetViewGroup = decorView.findViewById(android.R.id.content);
        if (targetViewGroup == null) {
            targetViewGroup = decorView;
        }

        // Dismiss any existing active notification on this Activity
        dismiss(activity);

        // Inflate custom notification view
        LayoutInflater inflater = LayoutInflater.from(activity);
        View notifRoot = inflater.inflate(R.layout.layout_app_notification, targetViewGroup, false);

        MaterialCardView card = notifRoot.findViewById(R.id.cardNotification);
        ImageView imgBrandLogo = notifRoot.findViewById(R.id.imgBrandLogo);
        ImageView imgStatusBadge = notifRoot.findViewById(R.id.imgStatusBadge);
        View viewAccentBar = notifRoot.findViewById(R.id.viewAccentBar);
        TextView txtTitle = notifRoot.findViewById(R.id.txtNotificationTitle);
        TextView txtMessage = notifRoot.findViewById(R.id.txtNotificationMessage);
        ImageView btnClose = notifRoot.findViewById(R.id.btnCloseNotification);

        // 1. Force guaranteed bright white background & high-contrast styling
        if (card != null) {
            card.setCardBackgroundColor(Color.WHITE);
            card.setRadius(dpToPx(activity, 18));
            card.setCardElevation(dpToPx(activity, 10));
        }

        // 2. High contrast typography (Deep Slate text on crisp White background)
        if (txtTitle != null) {
            txtTitle.setText(title);
            txtTitle.setTextColor(Color.parseColor("#0F172A")); // Ultra-crisp slate black
        }
        if (txtMessage != null) {
            txtMessage.setText(message);
            txtMessage.setTextColor(Color.parseColor("#334155")); // Crisp readable dark slate
        }

        // 3. New official bright logo
        if (imgBrandLogo != null) {
            imgBrandLogo.setImageResource(R.drawable.ic_ainterview_logo);
        }

        // 4. Vibrant Status colors
        configureTypeStyling(activity, type, card, imgStatusBadge, viewAccentBar);

        // 5. Calculate safe top padding taking into account status bar / notch
        int statusBarHeight = getStatusBarHeight(activity);
        int topPadding = statusBarHeight + dpToPx(activity, 10);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.topMargin = topPadding;
        notifRoot.setLayoutParams(params);

        // Initial off-screen state for animation
        notifRoot.setAlpha(0f);
        notifRoot.setTranslationY(-dpToPx(activity, 120));

        targetViewGroup.addView(notifRoot);

        // Auto-dismiss runnable
        final WeakReference<Activity> actRef = new WeakReference<>(activity);
        final WeakReference<View> viewRef = new WeakReference<>(notifRoot);
        final Runnable dismissRunnable = () -> {
            Activity a = actRef.get();
            View v = viewRef.get();
            if (a != null && v != null) {
                animateDismiss(a, v);
            }
        };

        ACTIVE_NOTIFICATIONS.put(activity, new ActiveNotification(notifRoot, dismissRunnable));

        // Close button click
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss(activity));
        }

        // Tap card to dismiss
        if (card != null) {
            card.setOnClickListener(v -> dismiss(activity));
        }

        // Swipe up gesture detection to dismiss
        setupSwipeToDismiss(notifRoot, activity);

        // Smooth entry animation (Slide down + subtle spring overshoot + fade in)
        notifRoot.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .setInterpolator(new OvershootInterpolator(0.6f))
                .start();

        // Schedule auto-dismiss
        if (durationMs > 0) {
            MAIN_HANDLER.postDelayed(dismissRunnable, durationMs);
        }
    }

    /**
     * Dismiss active notification on the specified activity with slide-up animation.
     */
    public static void dismiss(@Nullable Activity activity) {
        if (activity == null) return;
        ActiveNotification active = ACTIVE_NOTIFICATIONS.get(activity);
        if (active != null && !active.isDismissing) {
            MAIN_HANDLER.removeCallbacks(active.dismissRunnable);
            animateDismiss(activity, active.container);
        }
    }

    private static void animateDismiss(@NonNull Activity activity, @NonNull View view) {
        ActiveNotification active = ACTIVE_NOTIFICATIONS.get(activity);
        if (active != null) {
            active.isDismissing = true;
        }

        view.animate()
                .alpha(0f)
                .translationY(-dpToPx(activity, 120))
                .setDuration(240)
                .setInterpolator(new AccelerateInterpolator(1.4f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewGroup parent = (ViewGroup) view.getParent();
                        if (parent != null) {
                            parent.removeView(view);
                        }
                        ACTIVE_NOTIFICATIONS.remove(activity);
                    }
                })
                .start();
    }

    private static void configureTypeStyling(@NonNull Context context,
                                            @NonNull Type type,
                                            @Nullable MaterialCardView card,
                                            @Nullable ImageView imgStatusBadge,
                                            @Nullable View viewAccentBar) {
        int accentColor;
        int statusIconRes;

        switch (type) {
            case SUCCESS:
                accentColor = Color.parseColor("#10B981"); // Bright vivid emerald
                statusIconRes = R.drawable.ic_check_circle;
                break;
            case ERROR:
                accentColor = Color.parseColor("#EF4444"); // Bright vivid red
                statusIconRes = R.drawable.ic_notif_error;
                break;
            case WARNING:
                accentColor = Color.parseColor("#F59E0B"); // Bright vivid amber
                statusIconRes = R.drawable.ic_notif_warning;
                break;
            case AI_PROCESS:
                accentColor = Color.parseColor("#8B5CF6"); // Bright vivid violet matching new logo
                statusIconRes = R.drawable.ic_notif_ai;
                break;
            case INFO:
            default:
                accentColor = Color.parseColor("#2563EB"); // Bright royal blue
                statusIconRes = R.drawable.ic_notif_info;
                break;
        }

        // Apply accent bar
        if (viewAccentBar != null) {
            GradientDrawable barDrawable = new GradientDrawable();
            barDrawable.setCornerRadius(dpToPx(context, 4));
            barDrawable.setColor(accentColor);
            viewAccentBar.setBackground(barDrawable);
        }

        // Apply status icon & tint
        if (imgStatusBadge != null) {
            imgStatusBadge.setImageResource(statusIconRes);
            imgStatusBadge.setImageTintList(ColorStateList.valueOf(accentColor));
        }

        // Crisp subtle stroke border with light matching tint
        if (card != null) {
            int strokeTint = Color.argb(60, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor));
            card.setStrokeColor(strokeTint);
        }
    }

    private static void setupSwipeToDismiss(View view, Activity activity) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private float startTranslationY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        startTranslationY = v.getTranslationY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - startY;
                        if (deltaY < 0) { // Dragging upwards
                            v.setTranslationY(startTranslationY + deltaY);
                            float alpha = Math.max(0.15f, 1f - (Math.abs(deltaY) / dpToPx(activity, 80)));
                            v.setAlpha(alpha);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float finalDeltaY = event.getRawY() - startY;
                        if (finalDeltaY < -dpToPx(activity, 25)) {
                            // Swiped up enough to dismiss
                            dismiss(activity);
                            return true;
                        } else {
                            // Snap back smoothly
                            v.animate().translationY(0f).alpha(1f).setDuration(160).start();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private static int getStatusBarHeight(@NonNull Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return Math.max(result, dpToPx(activity, 24));
    }

    private static int dpToPx(@NonNull Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Nullable
    private static Activity resolveActivity(@Nullable Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            Context base = ((ContextWrapper) context).getBaseContext();
            return resolveActivity(base);
        }
        return null;
    }
}
