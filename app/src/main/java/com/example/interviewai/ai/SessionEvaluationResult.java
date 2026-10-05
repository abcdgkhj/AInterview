package com.example.interviewai.ai;

import com.example.interviewai.model.InterviewSession;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the complete AI evaluation result for an entire interview session.
 *
 * <p>This is a session-level summary produced after all individual answers have been
 * evaluated. It contains aggregated scores, an overall readiness label, and
 * dimension-level insights.</p>
 *
 * <h3>Future integration note:</h3>
 * <p>Produce this from the AI service's session summary endpoint (if supported),
 * or compute it locally by aggregating all per-answer {@link EvaluationResponse} objects
 * after they have completed. Attach the result to the {@link InterviewSession} before
 * saving to storage so that the History screen can display AI scores alongside
 * completion rates.</p>
 *
 * <p>Storage: add a {@code aiSessionResult} field to {@link InterviewSession} and
 * update {@link InterviewSession#toJsonObject()} / {@link InterviewSession#fromJsonObject}
 * to serialize/deserialize it.</p>
 */
public class SessionEvaluationResult implements Serializable {

    /** Aggregated status of all per-answer evaluations. */
    private final EvaluationStatus status;

    /**
     * Weighted overall score across all answered questions (0?100).
     * Only valid when {@code status == COMPLETED}.
     */
    private final int aggregatedScore;

    /**
     * A human-readable readiness label, e.g. "Ready", "Needs Practice".
     * Only valid when {@code status == COMPLETED}.
     */
    private final String readinessLabel;

    /**
     * High-level narrative feedback for the entire session.
     * Only valid when {@code status == COMPLETED}.
     */
    private final String sessionFeedback;

    /**
     * Per-criterion aggregated scores (averaged across all answers).
     * Keys: {@link EvaluationRequest#DEFAULT_CRITERIA}
     * Only valid when {@code status == COMPLETED}.
     */
    private final List<String> keyStrengths;

    /** List of priority improvement areas across the session. */
    private final List<String> priorityImprovements;

    public SessionEvaluationResult(EvaluationStatus status, int aggregatedScore,
                                   String readinessLabel, String sessionFeedback,
                                   List<String> keyStrengths, List<String> priorityImprovements) {
        this.status = status;
        this.aggregatedScore = aggregatedScore;
        this.readinessLabel = readinessLabel != null ? readinessLabel : "";
        this.sessionFeedback = sessionFeedback != null ? sessionFeedback : "";
        this.keyStrengths = keyStrengths;
        this.priorityImprovements = priorityImprovements;
    }

    /**
     * Creates a placeholder result indicating AI evaluation is not yet available.
     * Use this for every session created in the current prototype build.
     */
    public static SessionEvaluationResult notAvailable() {
        return new SessionEvaluationResult(
                EvaluationStatus.NOT_AVAILABLE,
                0, "", "", null, null
        );
    }

    public EvaluationStatus getStatus()                { return status; }
    public int getAggregatedScore()                    { return aggregatedScore; }
    public String getReadinessLabel()                  { return readinessLabel; }
    public String getSessionFeedback()                 { return sessionFeedback; }
    public List<String> getKeyStrengths()              { return keyStrengths; }
    public List<String> getPriorityImprovements()      { return priorityImprovements; }

    public boolean isCompleted() {
        return status == EvaluationStatus.COMPLETED;
    }
}
