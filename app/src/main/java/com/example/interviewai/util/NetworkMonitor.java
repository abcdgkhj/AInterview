package com.example.interviewai.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Utility pemantauan konektivitas internet real-time dan pengecekan kapabilitas internet
 * menggunakan ConnectivityManager, NetworkCallback, NetworkCapabilities (NET_CAPABILITY_INTERNET & NET_CAPABILITY_VALIDATED).
 */
public class NetworkMonitor {

    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private final Context context;
    private final NetworkStateListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isMonitoring = false;
    private boolean lastKnownConnected = false;

    public NetworkMonitor(@NonNull Context context, @NonNull NetworkStateListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Memeriksa apakah saat ini perangkat benar-benar memiliki koneksi internet aktif dan tervalidasi.
     */
    public static boolean isInternetAvailable(@Nullable Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) return false;
                NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                if (caps == null) return false;
                return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                android.net.NetworkInfo activeInfo = cm.getActiveNetworkInfo();
                return activeInfo != null && activeInfo.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Memulai pemantauan koneksi real-time.
     */
    public synchronized void startMonitoring() {
        if (isMonitoring || connectivityManager == null) return;

        lastKnownConnected = isInternetAvailable(context);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // Periksa apakah network yang available sudah memiliki kapabilitas internet tervalidasi
                checkAndUpdateState();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                updateState(hasInternet);
            }

            @Override
            public void onLost(@NonNull Network network) {
                // Verifikasi apakah masih ada network alternatif yang terhubung
                checkAndUpdateState();
            }

            @Override
            public void onUnavailable() {
                updateState(false);
            }
        };

        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
            isMonitoring = true;
        } catch (Exception e) {
            isMonitoring = false;
        }
    }

    private void checkAndUpdateState() {
        boolean connected = isInternetAvailable(context);
        updateState(connected);
    }

    private synchronized void updateState(boolean isConnected) {
        if (isConnected != lastKnownConnected) {
            lastKnownConnected = isConnected;
            if (listener != null) {
                mainHandler.post(() -> {
                    if (isConnected) {
                        listener.onNetworkAvailable();
                    } else {
                        listener.onNetworkLost();
                    }
                });
            }
        }
    }

    /**
     * Menghentikan pemantauan dan melepas callback untuk mencegah memory leak.
     */
    public synchronized void stopMonitoring() {
        if (!isMonitoring || connectivityManager == null || networkCallback == null) return;
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception ignored) {
        } finally {
            isMonitoring = false;
            networkCallback = null;
        }
    }

    /**
     * Dialog 1: Sebelum Memulai Wawancara (Menu Wawancara Umum / Wawancara Teknis).
     */
    public static AlertDialog showNoInternetDialog(@NonNull Context context, @Nullable Runnable onDismiss) {
        return new AlertDialog.Builder(context)
                .setTitle("Koneksi Internet Tidak Tersedia")
                .setMessage("Anda tidak dapat memulai wawancara karena perangkat tidak terhubung ke internet. Pastikan koneksi internet Anda aktif, kemudian coba kembali.")
                .setPositiveButton("Mengerti", (dialog, which) -> {
                    dialog.dismiss();
                    if (onDismiss != null) onDismiss.run();
                })
                .setCancelable(true)
                .show();
    }

    /**
     * Dialog 2: Selama Sesi Wawancara Berlangsung (Koneksi Terputus).
     */
    public static AlertDialog showDisconnectedDialog(@NonNull Context context, @Nullable Runnable onDismiss) {
        return new AlertDialog.Builder(context)
                .setTitle("Koneksi Internet Terputus")
                .setMessage("Koneksi internet Anda terputus. AInterview tidak dapat menghasilkan pertanyaan berikutnya maupun memproses dan memberikan feedback AI selama perangkat tidak terhubung ke internet. Silakan periksa koneksi internet Anda.")
                .setPositiveButton("Mengerti", (dialog, which) -> {
                    dialog.dismiss();
                    if (onDismiss != null) onDismiss.run();
                })
                .setCancelable(true)
                .show();
    }
}
