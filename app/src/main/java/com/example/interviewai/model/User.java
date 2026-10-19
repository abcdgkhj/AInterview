package com.example.interviewai.model;

import java.io.Serializable;

public class User implements Serializable {

    public static final String FIELD_IT = "IT";
    public static final String FIELD_MARKETING = "Marketing";
    public static final String FIELD_ACCOUNTING = "Accounting";
    public static final String FIELD_HR = "Human Resources";
    public static final String FIELD_HUMAN_RESOURCES = "Human Resources";
    public static final String FIELD_FINANCE = "Finance";
    public static final String FIELD_BUSINESS_MANAGEMENT = "Business Management";
    public static final String FIELD_SALES = "Sales";
    public static final String FIELD_GRAPHIC_DESIGN = "Graphic Design";
    public static final String FIELD_DATA_ANALYTICS = "Data Analytics";
    public static final String FIELD_CUSTOMER_SERVICE = "Customer Service";
    public static final String DEFAULT_FIELD = FIELD_IT;

    public static final String[] AVAILABLE_FIELDS = {
            FIELD_IT,
            FIELD_MARKETING,
            FIELD_ACCOUNTING,
            FIELD_HR,
            FIELD_FINANCE,
            FIELD_BUSINESS_MANAGEMENT,
            FIELD_SALES,
            FIELD_GRAPHIC_DESIGN,
            FIELD_DATA_ANALYTICS,
            FIELD_CUSTOMER_SERVICE
    };

    private String name;
    private String email;
    private String password;
    private long registeredDate;
    private String field;

    public User(String name, String email, String password, long registeredDate) {
        this(name, email, password, registeredDate, DEFAULT_FIELD);
    }

    public User(String name, String email, String password, long registeredDate, String field) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registeredDate = registeredDate;
        this.field = (field != null && !field.trim().isEmpty()) ? field.trim() : DEFAULT_FIELD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }

    public String getField() {
        return (field != null && !field.trim().isEmpty()) ? field : DEFAULT_FIELD;
    }

    public void setField(String field) {
        this.field = (field != null && !field.trim().isEmpty()) ? field.trim() : DEFAULT_FIELD;
    }
}
