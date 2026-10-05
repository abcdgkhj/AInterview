package com.example.interviewai.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterviewSession implements Serializable {
    private String sessionId;
    private String candidateName;
    private String roleCategory;
    private String difficulty;
    private String language;
    private long timestamp;
    private int overallScore;
    private String summaryStatus;
    private String summaryFeedback;
    private List<InterviewItem> items;

    public InterviewSession(String sessionId, String candidateName, String roleCategory, long timestamp,
                            int overallScore, String summaryStatus, String summaryFeedback, List<InterviewItem> items) {
        this(sessionId, candidateName, roleCategory, "Intermediate", "Indonesian", timestamp,
                overallScore, summaryStatus, summaryFeedback, items);
    }

    public InterviewSession(String sessionId, String candidateName, String roleCategory,
                            String difficulty, String language, long timestamp,
                            int overallScore, String summaryStatus, String summaryFeedback, List<InterviewItem> items) {
        this.sessionId = sessionId;
        this.candidateName = candidateName;
        this.roleCategory = roleCategory;
        this.difficulty = difficulty != null ? difficulty : "Intermediate";
        this.language = language != null ? language : "Indonesian";
        this.timestamp = timestamp;
        this.overallScore = overallScore;
        this.summaryStatus = summaryStatus;
        this.summaryFeedback = summaryFeedback;
        this.items = items != null ? items : new ArrayList<>();
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("sessionId", sessionId);
        obj.put("candidateName", candidateName);
        obj.put("roleCategory", roleCategory);
        obj.put("difficulty", difficulty);
        obj.put("language", language);
        obj.put("timestamp", timestamp);
        obj.put("overallScore", overallScore);
        obj.put("summaryStatus", summaryStatus);
        obj.put("summaryFeedback", summaryFeedback);

        JSONArray arr = new JSONArray();
        for (InterviewItem item : items) {
            arr.put(item.toJsonObject());
        }
        obj.put("items", arr);
        return obj;
    }

    public static InterviewSession fromJsonObject(JSONObject obj) throws JSONException {
        List<InterviewItem> list = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(InterviewItem.fromJsonObject(arr.getJSONObject(i)));
            }
        }

        return new InterviewSession(
                obj.optString("sessionId", ""),
                obj.optString("candidateName", "Kandidat"),
                obj.optString("roleCategory", "Umum"),
                obj.optString("difficulty", "Intermediate"),
                obj.optString("language", "Indonesian"),
                obj.optLong("timestamp", System.currentTimeMillis()),
                obj.optInt("overallScore", 75),
                obj.optString("summaryStatus", "Baik"),
                obj.optString("summaryFeedback", ""),
                list
        );
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getRoleCategory() {
        return roleCategory;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getLanguage() {
        return language;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public String getSummaryStatus() {
        return summaryStatus;
    }

    public String getSummaryFeedback() {
        return summaryFeedback;
    }

    public List<InterviewItem> getItems() {
        return items;
    }

    public int getTotalQuestions() {
        return items != null ? items.size() : 0;
    }

    public int getAnsweredQuestionsCount() {
        if (items == null) return 0;
        int count = 0;
        for (InterviewItem item : items) {
            if (item.getUserAnswer() != null && !item.getUserAnswer().trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
