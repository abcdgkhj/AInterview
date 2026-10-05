package com.example.interviewai.ai;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents the AI evaluation result for a single question-answer pair.
 *
 * <p>This model is populated by an {@link AnswerEvaluationService} implementation.
 * In the current build (no AI backend), this class is never instantiated with real data;
 * all items carry {@link EvaluationStatus#NOT_AVAILABLE} instead.</p>
 *
 * <h3>Future integration note:</h3>
 * <p>When a real AI service is added, parse its JSON response into this model.
 * The fields map directly to what should be displayed in {@link com.example.interviewai.adapter.FeedbackAdapter}.
 * Do NOT display any field from this class unless {@link #status} equals
 * {@link EvaluationStatus#COMPLETED}.</p>
 */
public class EvaluationResponse implements Serializable {

    /**
     * The current evaluation status. Always check this before reading any other field.
     */
    private final EvaluationStatus status;

    /**
     * Overall score for this answer, in the range 0?100.
     * Only meaningful when {@code status == COMPLETED}.
     * Do NOT display this value if status is NOT_AVAILABLE, FAILED, or PENDING.
     */
    private final int overallScore;

    /**
     * What the candidate did well in this answer.
     * Only meaningful when {@code status == COMPLETED}.
     */
    private final String strengths;

    /**
     * Specific, actionable suggestions to improve the answer.
     * Only meaningful when {@code status == COMPLETED}.
     */
    private final String improvements;

    /**
     * An ideal model answer for comparison.
     * Only meaningful when {@code status == COMPLETED}.
     */
    private final String modelAnswer;

    /**
     * Per-criterion scores (keys from {@link EvaluationRequest#DEFAULT_CRITERIA}).
     * Each value is in the range 0?100.
     * Only meaningful when {@code status == COMPLETED}.
     */
    private final Map<String, Integer> criterionScores;

    /**
     * Human-readable explanation of why this score was assigned.
     * Only meaningful when {@code status == COMPLETED}.
     */
    private final String scoreRationale;

    private EvaluationResponse(Builder builder) {
        this.status = builder.status;
        this.overallScore = builder.overallScore;
        this.strengths = builder.strengths;
        this.improvements = builder.improvements;
        this.modelAnswer = builder.modelAnswer;
        this.criterionScores = builder.criterionScores;
        this.scoreRationale = builder.scoreRationale;
    }

    // ??? Factory methods ???????????????????????????????????????????????????????

    /**
     * Creates a placeholder response indicating AI evaluation is not yet available.
     * Use this for all items in the current prototype build.
     */
    public static EvaluationResponse notAvailable() {
        return new Builder(EvaluationStatus.NOT_AVAILABLE).build();
    }

    /**
     * Creates a placeholder response indicating evaluation is queued.
     * Use this when an async API call has been dispatched but not yet resolved.
     */
    public static EvaluationResponse pending() {
        return new Builder(EvaluationStatus.PENDING).build();
    }

    /**
     * Creates a failure response. Call this if the API returns an error or times out.
     * @param errorMessage Human-readable description of what went wrong.
     */
    public static EvaluationResponse failed(String errorMessage) {
        return new Builder(EvaluationStatus.FAILED)
                .strengths(errorMessage)
                .build();
    }

    // ??? Getters ???????????????????????????????????????????????????????????????

    public EvaluationStatus getStatus()                { return status; }
    public int getOverallScore()                       { return overallScore; }
    public String getStrengths()                       { return strengths; }
    public String getImprovements()                    { return improvements; }
    public String getModelAnswer()                     { return modelAnswer; }
    public Map<String, Integer> getCriterionScores()   { return criterionScores; }
    public String getScoreRationale()                  { return scoreRationale; }

    public boolean isCompleted() { return status == EvaluationStatus.COMPLETED; }
    public boolean isAvailable() { return status == EvaluationStatus.NOT_AVAILABLE
                                        || status == EvaluationStatus.NOT_REQUESTED; }

    // ??? Builder ???????????????????????????????????????????????????????????????

    /**
     * Builder for constructing a complete EvaluationResponse from an AI API response.
     *
     * <h3>Future integration point:</h3>
     * <pre>
     *   // Parse API JSON and build:
     *   EvaluationResponse response = new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
     *       .overallScore(apiJson.getInt("score"))
     *       .strengths(apiJson.getString("strengths"))
     *       .improvements(apiJson.getString("improvements"))
     *       .modelAnswer(apiJson.getString("model_answer"))
     *       .scoreRationale(apiJson.getString("rationale"))
     *       .build();
     * </pre>
     */
    public static class Builder {
        private final EvaluationStatus status;
        private int overallScore = 0;
        private String strengths = "";
        private String improvements = "";
        private String modelAnswer = "";
        private Map<String, Integer> criterionScores = null;
        private String scoreRationale = "";

        public Builder(EvaluationStatus status) {
            this.status = status;
        }

        public Builder overallScore(int score)                          { this.overallScore = score; return this; }
        public Builder strengths(String s)                              { this.strengths = s != null ? s : ""; return this; }
        public Builder improvements(String s)                           { this.improvements = s != null ? s : ""; return this; }
        public Builder modelAnswer(String s)                            { this.modelAnswer = s != null ? s : ""; return this; }
        public Builder criterionScores(Map<String, Integer> scores)     { this.criterionScores = scores; return this; }
        public Builder scoreRationale(String s)                         { this.scoreRationale = s != null ? s : ""; return this; }

        public EvaluationResponse build() {
            return new EvaluationResponse(this);
        }
    }
}
