package com.example.interviewai.model;

import com.example.interviewai.ai.EvaluationStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class InterviewItem implements Serializable {
    private String question;
    private String userAnswer;
    private int score;
    private String strength;
    private String improvement;
    private String sampleAnswer;
    /**
     * Tracks whether AI evaluation has been performed for this item.
     * Defaults to {@link EvaluationStatus#NOT_AVAILABLE} in the current build.
     * Update this field when a real AI service returns results.
     */
    private EvaluationStatus evaluationStatus;

    /**
     * Full constructor including evaluation status.
     * Use when creating items with a known AI evaluation state.
     */
    public InterviewItem(String question, String userAnswer, int score, String strength,
                         String improvement, String sampleAnswer, EvaluationStatus evaluationStatus) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.score = score;
        this.strength = strength;
        this.improvement = improvement;
        this.sampleAnswer = sampleAnswer;
        this.evaluationStatus = evaluationStatus != null ? evaluationStatus : EvaluationStatus.NOT_AVAILABLE;
    }

    /**
     * Convenience constructor. Evaluation status defaults to NOT_AVAILABLE.
     * All existing callers use this constructor and remain compatible.
     */
    public InterviewItem(String question, String userAnswer, int score, String strength, String improvement, String sampleAnswer) {
        this(question, userAnswer, score, strength, improvement, sampleAnswer, EvaluationStatus.NOT_AVAILABLE);
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("question", question);
        obj.put("userAnswer", userAnswer);
        obj.put("score", score);
        obj.put("strength", strength);
        obj.put("improvement", improvement);
        obj.put("sampleAnswer", sampleAnswer);
        obj.put("evaluationStatus", evaluationStatus != null ? evaluationStatus.getKey() : EvaluationStatus.NOT_AVAILABLE.getKey());
        return obj;
    }

    public static InterviewItem fromJsonObject(JSONObject obj) throws JSONException {
        EvaluationStatus status = EvaluationStatus.fromKey(obj.optString("evaluationStatus", null));
        return new InterviewItem(
                obj.optString("question", ""),
                obj.optString("userAnswer", ""),
                obj.optInt("score", 70),
                obj.optString("strength", ""),
                obj.optString("improvement", ""),
                obj.optString("sampleAnswer", ""),
                status
        );
    }

    public String getQuestion() {
        return question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public int getScore() {
        return score;
    }

    public String getStrength() {
        return strength;
    }

    public String getImprovement() {
        return improvement;
    }

    public String getSampleAnswer() {
        return sampleAnswer;
    }

    public EvaluationStatus getEvaluationStatus() {
        return evaluationStatus != null ? evaluationStatus : EvaluationStatus.NOT_AVAILABLE;
    }

    /**
     * Updates the evaluation status on this item after an AI response is received.
     * Call this from the result layer once {@link com.example.interviewai.ai.EvaluationResponse}
     * is available.
     */
    public void setEvaluationStatus(EvaluationStatus status) {
        this.evaluationStatus = status != null ? status : EvaluationStatus.NOT_AVAILABLE;
    }
}
