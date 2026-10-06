package com.example.interviewai.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.interviewai.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class UserSessionManager {

    private static final String PREF_NAME = "interview_ai_user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_DEFAULT_LANG = "pref_default_lang";
    private static final String KEY_USER_FIELD = "pref_user_field";

    private final SharedPreferences prefs;

    public UserSessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean register(String name, String email, String password) {
        return register(name, email, password, User.DEFAULT_FIELD);
    }

    public boolean register(String name, String email, String password, String field) {
        if (name == null || email == null || password == null) return false;

        String selectedField = (field != null && !field.trim().isEmpty()) ? field.trim() : User.DEFAULT_FIELD;
        User user = new User(name.trim(), email.trim().toLowerCase(), password, System.currentTimeMillis(), selectedField);
        saveUser(user);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
        return true;
    }

    public boolean login(String email, String password) {
        if (email == null || password == null) return false;

        User user = getCurrentUser();
        if (user != null && user.getEmail().equalsIgnoreCase(email.trim()) && user.getPassword().equals(password)) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
            return true;
        }

        // Default demonstration account — clearly labelled as prototype only
        if ("demo@ainterview.app".equalsIgnoreCase(email.trim()) && "password123".equals(password)) {
            String defaultField = prefs.getString(KEY_USER_FIELD, User.DEFAULT_FIELD);
            User demoUser = new User("Mahasiswa Demo", "demo@ainterview.app", "password123", System.currentTimeMillis(), defaultField);
            saveUser(demoUser);
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
            return true;
        }

        return false;
    }

    public void saveUser(User user) {
        if (user == null) return;
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", user.getName());
            obj.put("email", user.getEmail());
            obj.put("password", user.getPassword());
            obj.put("registeredDate", user.getRegisteredDate());
            obj.put("field", user.getField());
            prefs.edit()
                    .putString(KEY_USER_DATA, obj.toString())
                    .putString(KEY_USER_FIELD, user.getField())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        String raw = prefs.getString(KEY_USER_DATA, null);
        String savedField = prefs.getString(KEY_USER_FIELD, User.DEFAULT_FIELD);
        if (raw == null) {
            return new User("Mahasiswa", "kandidat@kampus.id", "password123", System.currentTimeMillis(), savedField);
        }
        try {
            JSONObject obj = new JSONObject(raw);
            return new User(
                    obj.optString("name", "Mahasiswa"),
                    obj.optString("email", "kandidat@kampus.id"),
                    obj.optString("password", "password123"),
                    obj.optLong("registeredDate", System.currentTimeMillis()),
                    obj.optString("field", savedField)
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return new User("Mahasiswa", "kandidat@kampus.id", "password123", System.currentTimeMillis(), savedField);
        }
    }

    public String getUserField() {
        return prefs.getString(KEY_USER_FIELD, getCurrentUser().getField());
    }

    public void setUserField(String field) {
        String validField = (field != null && !field.trim().isEmpty()) ? field.trim() : User.DEFAULT_FIELD;
        prefs.edit().putString(KEY_USER_FIELD, validField).apply();
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.setField(validField);
            saveUser(currentUser);
        }
    }

    public boolean resetPassword(String email, String newPassword) {
        if (email == null || email.trim().isEmpty() || newPassword == null) return false;
        String cleanEmail = email.trim().toLowerCase();

        User current = getCurrentUser();
        if (current != null && cleanEmail.equalsIgnoreCase(current.getEmail())) {
            current.setPassword(newPassword);
            saveUser(current);
            return true;
        }

        if ("demo@ainterview.app".equalsIgnoreCase(cleanEmail)) {
            String field = prefs.getString(KEY_USER_FIELD, User.DEFAULT_FIELD);
            User demoUser = new User("Mahasiswa Demo", "demo@ainterview.app", newPassword, System.currentTimeMillis(), field);
            saveUser(demoUser);
            return true;
        }

        // Simpan user terdaftar baru dengan password yang direset untuk demonstrasi lokal
        String field = prefs.getString(KEY_USER_FIELD, User.DEFAULT_FIELD);
        String name = cleanEmail.contains("@") ? cleanEmail.substring(0, cleanEmail.indexOf('@')) : "Kandidat";
        User newUser = new User(name, cleanEmail, newPassword, System.currentTimeMillis(), field);
        saveUser(newUser);
        return true;
    }

    public void logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
    }

    public String getDefaultLanguage() {
        return prefs.getString(KEY_DEFAULT_LANG, "Indonesian");
    }

    public void setDefaultLanguage(String language) {
        prefs.edit().putString(KEY_DEFAULT_LANG, language).apply();
    }
}
