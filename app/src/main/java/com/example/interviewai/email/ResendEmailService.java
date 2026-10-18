package com.example.interviewai.email;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service pengiriman email nyata menggunakan Resend REST API (https://resend.com).
 */
public class ResendEmailService {

    private static final String TAG = "ResendEmailService";
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface SendOtpCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage, boolean isKeyMissing);
    }

    /**
     * Mengirimkan kode OTP reset password ke email tujuan.
     */
    public static void sendOtpEmail(String recipientEmail, String otpCode, SendOtpCallback callback) {
        String apiKey = ResendApiKey.API_KEY != null ? ResendApiKey.API_KEY.trim() : "";

        // Validasi ketersediaan API key
        if (apiKey.isEmpty() || apiKey.startsWith("re_your_")) {
            if (callback != null) {
                mainHandler.post(() -> callback.onFailure(
                        "API Key Resend belum diisi pada file ResendApiKey.java.",
                        true
                ));
            }
            return;
        }

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(RESEND_API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setDoOutput(true);

                // Buat HTML Body Email yang elegan
                String htmlBody = buildOtpHtmlContent(recipientEmail, otpCode);

                JSONObject payload = new JSONObject();
                payload.put("from", ResendApiKey.SENDER_EMAIL);

                JSONArray toArray = new JSONArray();
                toArray.put(recipientEmail);
                payload.put("to", toArray);

                payload.put("subject", "Kode Verifikasi Reset Password - AInterview (" + otpCode + ")");
                payload.put("html", htmlBody);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();

                String rawResponse = sb.toString();
                if (responseCode >= 200 && responseCode < 300) {
                    Log.i(TAG, "Email OTP berhasil dikirim ke " + recipientEmail + ": " + rawResponse);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess("Email verifikasi telah berhasil dikirim ke " + recipientEmail));
                    }
                } else {
                    String parsedError = parseResendError(rawResponse, responseCode);
                    Log.e(TAG, "Gagal mengirim email via Resend (" + responseCode + "): " + parsedError);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onFailure(parsedError, false));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception saat memanggil Resend API: " + e.getMessage(), e);
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure("Koneksi gagal: " + e.getMessage(), false));
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    /**
     * Membangun tampilan email HTML yang rapi dengan branding AInterview.
     */
    private static String buildOtpHtmlContent(String email, String otpCode) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "</head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #F8FAFC; margin: 0; padding: 20px; color: #1E293B;'>"
                + "  <div style='max-width: 500px; margin: 0 auto; background: #FFFFFF; border-radius: 16px; padding: 32px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); border: 1px solid #E2E8F0;'>"
                + "    <div style='text-align: center; margin-bottom: 24px;'>"
                + "      <h1 style='color: #4F46E5; margin: 0; font-size: 26px; font-weight: 800;'>AInterview</h1>"
                + "      <p style='color: #64748B; margin-top: 4px; font-size: 13px;'>Smart Job Interview Practice Platform</p>"
                + "    </div>"
                + "    <h2 style='font-size: 18px; color: #0F172A; margin-bottom: 8px;'>Reset Kata Sandi Akun Anda</h2>"
                + "    <p style='color: #475569; font-size: 14px; line-height: 1.6; margin-top: 0;'>"
                + "      Halo <strong>" + email + "</strong>,<br>"
                + "      Kami menerima permintaan untuk mengatur ulang kata sandi akun AInterview Anda. Gunakan kode verifikasi (OTP) berikut untuk melanjutkan:"
                + "    </p>"
                + "    <div style='background-color: #EEF2FF; border: 2px dashed #4F46E5; border-radius: 12px; padding: 18px; text-align: center; margin: 24px 0;'>"
                + "      <span style='font-size: 34px; font-weight: 800; letter-spacing: 8px; color: #4F46E5; font-family: monospace;'>" + otpCode + "</span>"
                + "    </div>"
                + "    <div style='background: #FFFBEB; border-left: 4px solid #F59E0B; padding: 12px; border-radius: 6px; margin-bottom: 24px;'>"
                + "      <p style='margin: 0; font-size: 12px; color: #92400E;'>"
                + "        <strong>Perhatian:</strong> Kode OTP ini hanya berlaku selama <strong>10 menit</strong>. Jangan pernah membagikan kode ini kepada siapa pun."
                + "      </p>"
                + "    </div>"
                + "    <p style='color: #64748B; font-size: 13px; line-height: 1.5;'>"
                + "      Jika Anda tidak merasa meminta reset kata sandi, Anda dapat mengabaikan email ini dengan aman."
                + "    </p>"
                + "    <hr style='border: none; border-top: 1px solid #E2E8F0; margin: 24px 0;' />"
                + "    <p style='color: #94A3B8; font-size: 11px; text-align: center; margin: 0;'>"
                + "      &copy; AInterview Platform &bull; Tim Keamanan Akun"
                + "    </p>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    private static String parseResendError(String responseBody, int statusCode) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "Resend API mengembalikan status error " + statusCode;
        }
        try {
            JSONObject obj = new JSONObject(responseBody);
            if (obj.has("message")) {
                return obj.getString("message");
            }
        } catch (Exception ignored) {}
        return "Resend Error (" + statusCode + "): " + responseBody;
    }
}
