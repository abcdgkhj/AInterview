package com.example.interviewai.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.interviewai.model.InterviewSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InterviewStorage {

    private static final String PREF_NAME = "interview_ai_prefs";
    private static final String KEY_SESSIONS = "saved_sessions";

    public static void saveSession(Context context, InterviewSession session) {
        if (context == null || session == null) return;

        List<InterviewSession> existing = getSessions(context);
        // Tambahkan sesi terbaru di awal daftar
        existing.add(0, session);

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        try {
            for (InterviewSession s : existing) {
                jsonArray.put(s.toJsonObject());
            }
            prefs.edit().putString(KEY_SESSIONS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<InterviewSession> getSessions(Context context) {
        List<InterviewSession> list = new ArrayList<>();
        if (context == null) return list;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String rawJson = prefs.getString(KEY_SESSIONS, null);

        if (rawJson != null && !rawJson.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(rawJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    list.add(InterviewSession.fromJsonObject(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public static void clearHistory(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_SESSIONS).apply();
    }

    /**
     * Deletes a single session identified by its sessionId.
     * If the sessionId is not found, the list is unchanged.
     */
    public static void deleteSession(Context context, String sessionId) {
        if (context == null || sessionId == null) return;
        List<InterviewSession> existing = getSessions(context);
        int indexToRemove = -1;
        for (int i = 0; i < existing.size(); i++) {
            if (sessionId.equals(existing.get(i).getSessionId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0) {
            existing.remove(indexToRemove);
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            JSONArray jsonArray = new JSONArray();
            try {
                for (InterviewSession s : existing) {
                    jsonArray.put(s.toJsonObject());
                }
                prefs.edit().putString(KEY_SESSIONS, jsonArray.toString()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
