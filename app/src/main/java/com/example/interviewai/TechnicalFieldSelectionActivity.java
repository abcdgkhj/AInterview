package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.data.UserSessionManager;
import com.example.interviewai.model.User;

public class TechnicalFieldSelectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtSavedFieldHint;
    private View cardOptionIT;
    private RadioButton rbFieldIT;
    private View cardOptionMarketing;
    private RadioButton rbFieldMarketing;
    private View cardOptionAccounting;
    private RadioButton rbFieldAccounting;
    private View cardOptionHR;
    private RadioButton rbFieldHR;
    private View cardOptionFinance;
    private RadioButton rbFieldFinance;
    private View cardOptionBusinessManagement;
    private RadioButton rbFieldBusinessManagement;
    private View cardOptionSales;
    private RadioButton rbFieldSales;
    private View cardOptionGraphicDesign;
    private RadioButton rbFieldGraphicDesign;
    private View cardOptionDataAnalytics;
    private RadioButton rbFieldDataAnalytics;
    private View cardOptionCustomerService;
    private RadioButton rbFieldCustomerService;
    private Button btnStartTechnicalInterview;

    private UserSessionManager sessionManager;
    private String selectedField = User.FIELD_IT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical_field_selection);

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

        btnBack = findViewById(R.id.btnBack);
        txtSavedFieldHint = findViewById(R.id.txtSavedFieldHint);
        cardOptionIT = findViewById(R.id.cardOptionIT);
        rbFieldIT = findViewById(R.id.rbFieldIT);
        cardOptionMarketing = findViewById(R.id.cardOptionMarketing);
        rbFieldMarketing = findViewById(R.id.rbFieldMarketing);
        cardOptionAccounting = findViewById(R.id.cardOptionAccounting);
        rbFieldAccounting = findViewById(R.id.rbFieldAccounting);
        cardOptionHR = findViewById(R.id.cardOptionHR);
        rbFieldHR = findViewById(R.id.rbFieldHR);
        cardOptionFinance = findViewById(R.id.cardOptionFinance);
        rbFieldFinance = findViewById(R.id.rbFieldFinance);
        cardOptionBusinessManagement = findViewById(R.id.cardOptionBusinessManagement);
        rbFieldBusinessManagement = findViewById(R.id.rbFieldBusinessManagement);
        cardOptionSales = findViewById(R.id.cardOptionSales);
        rbFieldSales = findViewById(R.id.rbFieldSales);
        cardOptionGraphicDesign = findViewById(R.id.cardOptionGraphicDesign);
        rbFieldGraphicDesign = findViewById(R.id.rbFieldGraphicDesign);
        cardOptionDataAnalytics = findViewById(R.id.cardOptionDataAnalytics);
        rbFieldDataAnalytics = findViewById(R.id.rbFieldDataAnalytics);
        cardOptionCustomerService = findViewById(R.id.cardOptionCustomerService);
        rbFieldCustomerService = findViewById(R.id.rbFieldCustomerService);
        btnStartTechnicalInterview = findViewById(R.id.btnStartTechnicalInterview);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupInitialFieldSelection();
        setupClickListeners();
    }

    private void setupInitialFieldSelection() {
        String savedField = sessionManager.getUserField();
        if (savedField == null || savedField.trim().isEmpty()) {
            savedField = User.DEFAULT_FIELD;
        }

        if (txtSavedFieldHint != null) {
            txtSavedFieldHint.setText("Bidang profil tersimpan: " + savedField + " (Otomatis dipilih sebagai default)");
        }

        selectField(savedField);
    }

    private void selectField(String field) {
        selectedField = field;

        if (rbFieldIT != null) rbFieldIT.setChecked(false);
        if (rbFieldMarketing != null) rbFieldMarketing.setChecked(false);
        if (rbFieldAccounting != null) rbFieldAccounting.setChecked(false);
        if (rbFieldHR != null) rbFieldHR.setChecked(false);
        if (rbFieldFinance != null) rbFieldFinance.setChecked(false);
        if (rbFieldBusinessManagement != null) rbFieldBusinessManagement.setChecked(false);
        if (rbFieldSales != null) rbFieldSales.setChecked(false);
        if (rbFieldGraphicDesign != null) rbFieldGraphicDesign.setChecked(false);
        if (rbFieldDataAnalytics != null) rbFieldDataAnalytics.setChecked(false);
        if (rbFieldCustomerService != null) rbFieldCustomerService.setChecked(false);

        if (User.FIELD_MARKETING.equalsIgnoreCase(field)) {
            if (rbFieldMarketing != null) rbFieldMarketing.setChecked(true);
            selectedField = User.FIELD_MARKETING;
        } else if (User.FIELD_ACCOUNTING.equalsIgnoreCase(field)) {
            if (rbFieldAccounting != null) rbFieldAccounting.setChecked(true);
            selectedField = User.FIELD_ACCOUNTING;
        } else if (User.FIELD_HR.equalsIgnoreCase(field) || "HR".equalsIgnoreCase(field)) {
            if (rbFieldHR != null) rbFieldHR.setChecked(true);
            selectedField = User.FIELD_HR;
        } else if (User.FIELD_FINANCE.equalsIgnoreCase(field)) {
            if (rbFieldFinance != null) rbFieldFinance.setChecked(true);
            selectedField = User.FIELD_FINANCE;
        } else if (User.FIELD_BUSINESS_MANAGEMENT.equalsIgnoreCase(field)) {
            if (rbFieldBusinessManagement != null) rbFieldBusinessManagement.setChecked(true);
            selectedField = User.FIELD_BUSINESS_MANAGEMENT;
        } else if (User.FIELD_SALES.equalsIgnoreCase(field)) {
            if (rbFieldSales != null) rbFieldSales.setChecked(true);
            selectedField = User.FIELD_SALES;
        } else if (User.FIELD_GRAPHIC_DESIGN.equalsIgnoreCase(field)) {
            if (rbFieldGraphicDesign != null) rbFieldGraphicDesign.setChecked(true);
            selectedField = User.FIELD_GRAPHIC_DESIGN;
        } else if (User.FIELD_DATA_ANALYTICS.equalsIgnoreCase(field)) {
            if (rbFieldDataAnalytics != null) rbFieldDataAnalytics.setChecked(true);
            selectedField = User.FIELD_DATA_ANALYTICS;
        } else if (User.FIELD_CUSTOMER_SERVICE.equalsIgnoreCase(field)) {
            if (rbFieldCustomerService != null) rbFieldCustomerService.setChecked(true);
            selectedField = User.FIELD_CUSTOMER_SERVICE;
        } else {
            if (rbFieldIT != null) rbFieldIT.setChecked(true);
            selectedField = User.FIELD_IT;
        }

        if (btnStartTechnicalInterview != null) {
            btnStartTechnicalInterview.setText("Mulai Interview Teknis (" + selectedField + ")");
        }
    }

    private void setupClickListeners() {
        if (cardOptionIT != null) cardOptionIT.setOnClickListener(v -> selectField(User.FIELD_IT));
        if (cardOptionMarketing != null) cardOptionMarketing.setOnClickListener(v -> selectField(User.FIELD_MARKETING));
        if (cardOptionAccounting != null) cardOptionAccounting.setOnClickListener(v -> selectField(User.FIELD_ACCOUNTING));
        if (cardOptionHR != null) cardOptionHR.setOnClickListener(v -> selectField(User.FIELD_HR));
        if (cardOptionFinance != null) cardOptionFinance.setOnClickListener(v -> selectField(User.FIELD_FINANCE));
        if (cardOptionBusinessManagement != null) cardOptionBusinessManagement.setOnClickListener(v -> selectField(User.FIELD_BUSINESS_MANAGEMENT));
        if (cardOptionSales != null) cardOptionSales.setOnClickListener(v -> selectField(User.FIELD_SALES));
        if (cardOptionGraphicDesign != null) cardOptionGraphicDesign.setOnClickListener(v -> selectField(User.FIELD_GRAPHIC_DESIGN));
        if (cardOptionDataAnalytics != null) cardOptionDataAnalytics.setOnClickListener(v -> selectField(User.FIELD_DATA_ANALYTICS));
        if (cardOptionCustomerService != null) cardOptionCustomerService.setOnClickListener(v -> selectField(User.FIELD_CUSTOMER_SERVICE));

        if (btnStartTechnicalInterview != null) {
            btnStartTechnicalInterview.setOnClickListener(v -> startTechnicalInterview());
        }
    }

    private void startTechnicalInterview() {
        User user = sessionManager.getCurrentUser();
        String candidateName = (user != null && user.getName() != null && !user.getName().trim().isEmpty())
                ? user.getName().trim() : "Kandidat";

        String category = QuestionBank.getTechnicalCategoryForField(selectedField);

        Intent intent = new Intent(TechnicalFieldSelectionActivity.this, InterviewActivity.class);
        intent.putExtra(MainActivity.EXTRA_CANDIDATE_NAME, candidateName);
        intent.putExtra(MainActivity.EXTRA_ROLE_CATEGORY, category);
        intent.putExtra(InterviewActivity.EXTRA_DIFFICULTY, QuestionBank.DIFFICULTY_INTERMEDIATE);
        intent.putExtra(InterviewActivity.EXTRA_QUESTION_COUNT, 5);
        intent.putExtra(InterviewActivity.EXTRA_LANGUAGE, sessionManager.getDefaultLanguage());
        startActivity(intent);
        finish();
    }
}
