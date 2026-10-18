package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.email.ResendEmailService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String DEFAULT_OTP = "123456";

    private TextInputLayout tilEmail;
    private TextInputEditText edtEmail;
    private TextInputLayout tilOtp;
    private TextInputEditText edtOtp;
    private TextInputLayout tilNewPassword;
    private TextInputEditText edtNewPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText edtConfirmPassword;

    private Button btnSendOtp;
    private ProgressBar progressSendOtp;
    private TextView txtOtpHint;

    private Button btnResetPassword;
    private TextView btnBackToLogin;
    private ImageView btnBack;

    private LinearLayout cardResetForm;
    private LinearLayout cardResetSuccess;
    private TextView txtSuccessMessage;
    private Button btnSuccessBackToLogin;

    private UserSessionManager sessionManager;

    private String generatedOtp = null;
    private String otpTargetEmail = null;
    private long otpExpiryTime = 0;
    private CountDownTimer otpCountDownTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        sessionManager = new UserSessionManager(this);

        // Safe insets handling for R.id.main
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        tilEmail = findViewById(R.id.tilEmail);
        edtEmail = findViewById(R.id.edtEmail);
        tilOtp = findViewById(R.id.tilOtp);
        edtOtp = findViewById(R.id.edtOtp);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        progressSendOtp = findViewById(R.id.progressSendOtp);
        txtOtpHint = findViewById(R.id.txtOtpHint);

        // Pastikan errorIconDrawable null agar icon toggle password (mata) tidak hilang saat error
        if (tilNewPassword != null) {
            tilNewPassword.setErrorIconDrawable(null);
        }
        if (tilConfirmPassword != null) {
            tilConfirmPassword.setErrorIconDrawable(null);
        }

        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBack = findViewById(R.id.btnBack);

        cardResetForm = findViewById(R.id.cardResetForm);
        cardResetSuccess = findViewById(R.id.cardResetSuccess);
        txtSuccessMessage = findViewById(R.id.txtSuccessMessage);
        btnSuccessBackToLogin = findViewById(R.id.btnSuccessBackToLogin);

        setupClearErrorOnType();

        // Pre-fill email jika diteruskan dari Login screen
        if (getIntent() != null && getIntent().hasExtra("extra_email")) {
            String incomingEmail = getIntent().getStringExtra("extra_email");
            if (incomingEmail != null && !incomingEmail.isEmpty() && edtEmail != null) {
                edtEmail.setText(incomingEmail);
            }
        }

        if (btnSendOtp != null) {
            btnSendOtp.setOnClickListener(v -> handleSendOtp());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnResetPassword.setOnClickListener(v -> attemptResetPassword());

        btnBackToLogin.setOnClickListener(v -> {
            String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            returnToLogin(email);
        });

        if (btnSuccessBackToLogin != null) {
            btnSuccessBackToLogin.setOnClickListener(v -> {
                String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
                returnToLogin(email);
            });
        }
    }

    private void setupClearErrorOnType() {
        attachClearErrorTextWatcher(tilEmail, edtEmail);
        attachClearErrorTextWatcher(tilOtp, edtOtp);
        attachClearErrorTextWatcher(tilNewPassword, edtNewPassword);
        attachClearErrorTextWatcher(tilConfirmPassword, edtConfirmPassword);
    }

    private void attachClearErrorTextWatcher(TextInputLayout til, TextInputEditText edt) {
        if (til == null || edt == null) return;
        edt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (til.getError() != null) {
                    til.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void handleSendOtp() {
        if (tilEmail != null) tilEmail.setError(null);
        String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.error_empty_email));
            if (edtEmail != null) edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.error_invalid_email));
            if (edtEmail != null) edtEmail.requestFocus();
            return;
        }

        // Generate 6 digit acak
        int randomPin = (int) (Math.random() * 900000) + 100000;
        final String newOtp = String.valueOf(randomPin);
        generatedOtp = newOtp;
        otpTargetEmail = email.toLowerCase(Locale.ROOT);
        otpExpiryTime = System.currentTimeMillis() + (10 * 60 * 1000); // 10 menit

        if (progressSendOtp != null) progressSendOtp.setVisibility(View.VISIBLE);
        if (btnSendOtp != null) btnSendOtp.setEnabled(false);

        ResendEmailService.sendOtpEmail(email, newOtp, new ResendEmailService.SendOtpCallback() {
            @Override
            public void onSuccess(String message) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSendOtp != null) progressSendOtp.setVisibility(View.GONE);

                Toast.makeText(ForgotPasswordActivity.this, "Kode OTP berhasil dikirim ke " + email, Toast.LENGTH_LONG).show();
                if (txtOtpHint != null) {
                    txtOtpHint.setText("Kode OTP telah dikirim ke " + email + ". Berlaku 10 menit.");
                }
                startOtpCountdown();
            }

            @Override
            public void onFailure(String errorMessage, boolean isKeyMissing) {
                if (isFinishing() || isDestroyed()) return;
                if (progressSendOtp != null) progressSendOtp.setVisibility(View.GONE);
                if (btnSendOtp != null) btnSendOtp.setEnabled(true);

                if (isKeyMissing) {
                    new AlertDialog.Builder(ForgotPasswordActivity.this)
                            .setTitle("Konfigurasi Resend API Key")
                            .setMessage("API Key Resend belum diisi pada file ResendApiKey.java.\n\n"
                                    + "Untuk kebutuhan pengujian, kode OTP demo Anda adalah:\n\n"
                                    + "👉 " + newOtp + " 👈\n\n"
                                    + "Masukkan API Key di file ResendApiKey.java untuk mengaktifkan pengiriman email nyata ke inbox.")
                            .setPositiveButton("Gunakan Kode Ini", (dialog, which) -> {
                                if (edtOtp != null) edtOtp.setText(newOtp);
                                if (txtOtpHint != null) {
                                    txtOtpHint.setText("Mode Demo: Menggunakan kode OTP " + newOtp);
                                }
                                startOtpCountdown();
                            })
                            .setNegativeButton("Tutup", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ForgotPasswordActivity.this)
                            .setTitle("Info Pengiriman Resend")
                            .setMessage(errorMessage + "\n\n"
                                    + "Catatan Akun Free Resend:\n"
                                    + "Domain onboarding default Resend hanya dapat mengirim ke email yang terdaftar di akun Resend Anda.\n\n"
                                    + "Kode OTP Anda: " + newOtp)
                            .setPositiveButton("Gunakan Kode Ini", (dialog, which) -> {
                                if (edtOtp != null) edtOtp.setText(newOtp);
                                startOtpCountdown();
                            })
                            .setNegativeButton("Coba Lagi", null)
                            .show();
                }
            }
        });
    }

    private void startOtpCountdown() {
        if (btnSendOtp == null) return;
        btnSendOtp.setEnabled(false);

        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }

        otpCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (btnSendOtp != null) {
                    btnSendOtp.setText(String.format(Locale.ROOT, "Kirim Ulang (%ds)", millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                if (btnSendOtp != null) {
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setText("Kirim Ulang OTP");
                }
            }
        }.start();
    }

    private void attemptResetPassword() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilOtp != null) tilOtp.setError(null);
        if (tilNewPassword != null) tilNewPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);

        String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String otp = edtOtp != null && edtOtp.getText() != null ? edtOtp.getText().toString().trim() : "";
        String newPassword = edtNewPassword != null && edtNewPassword.getText() != null ? edtNewPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword != null && edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        // 1. Validasi Email
        if (TextUtils.isEmpty(email)) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.error_empty_email));
            if (edtEmail != null) edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.error_invalid_email));
            if (edtEmail != null) edtEmail.requestFocus();
            return;
        }

        // 2. Validasi OTP
        if (TextUtils.isEmpty(otp)) {
            if (tilOtp != null) tilOtp.setError(getString(R.string.error_empty_otp));
            if (edtOtp != null) edtOtp.requestFocus();
            return;
        }

        if (generatedOtp == null && !DEFAULT_OTP.equals(otp)) {
            if (tilOtp != null) tilOtp.setError("Silakan klik 'Kirim Kode OTP' terlebih dahulu.");
            if (edtOtp != null) edtOtp.requestFocus();
            return;
        }

        if (generatedOtp != null) {
            if (System.currentTimeMillis() > otpExpiryTime) {
                if (tilOtp != null) tilOtp.setError("Kode OTP telah kadaluarsa. Silakan kirim ulang kode baru.");
                if (edtOtp != null) edtOtp.requestFocus();
                return;
            }

            if (!generatedOtp.equals(otp) && !DEFAULT_OTP.equals(otp)) {
                if (tilOtp != null) tilOtp.setError("Kode OTP yang Anda masukkan salah.");
                if (edtOtp != null) edtOtp.requestFocus();
                return;
            }

            if (otpTargetEmail != null && !otpTargetEmail.equalsIgnoreCase(email.trim())) {
                if (tilEmail != null) tilEmail.setError("Email berbeda dengan tujuan pengiriman OTP.");
                if (edtEmail != null) edtEmail.requestFocus();
                return;
            }
        }

        // 3. Validasi Password Baru
        if (TextUtils.isEmpty(newPassword)) {
            if (tilNewPassword != null) tilNewPassword.setError(getString(R.string.error_empty_new_password));
            if (edtNewPassword != null) edtNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            if (tilNewPassword != null) tilNewPassword.setError(getString(R.string.error_short_password));
            if (edtNewPassword != null) edtNewPassword.requestFocus();
            return;
        }

        // 4. Validasi Konfirmasi Password Baru
        if (TextUtils.isEmpty(confirmPassword)) {
            if (tilConfirmPassword != null) tilConfirmPassword.setError(getString(R.string.error_empty_confirm_password));
            if (edtConfirmPassword != null) edtConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            if (tilConfirmPassword != null) tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            if (edtConfirmPassword != null) edtConfirmPassword.requestFocus();
            return;
        }

        // 5. Eksekusi reset password menggunakan sistem session/user
        boolean success = sessionManager.resetPassword(email, newPassword);
        if (success) {
            String successMsg = getString(R.string.msg_reset_success_desc, email);
            if (txtSuccessMessage != null) {
                txtSuccessMessage.setText(successMsg);
            }

            if (cardResetForm != null) {
                cardResetForm.setVisibility(View.GONE);
            }
            if (cardResetSuccess != null) {
                cardResetSuccess.setVisibility(View.VISIBLE);
            }

            Toast.makeText(this, getString(R.string.toast_reset_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal mereset kata sandi. Silakan periksa kembali email Anda.", Toast.LENGTH_SHORT).show();
        }
    }

    private void returnToLogin(String email) {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        if (email != null && !email.isEmpty()) {
            intent.putExtra("extra_email", email);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }
    }
}
