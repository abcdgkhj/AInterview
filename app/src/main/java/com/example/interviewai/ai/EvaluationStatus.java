package com.example.interviewai.ai;

/**
 * Represents the current state of AI evaluation for a single answer or session.
 *
 * <p>This enum is the single source of truth for whether AI evaluation has been
 * performed, is pending, or is unavailable. The UI layer should check this status
 * before rendering any evaluation-specific content.</p>
 *
 * <h3>Future integration note:</h3>
 * <p>When an AI service is connected, the workflow should transition statuses as follows:
 * <pre>
 *   NOT_REQUESTED  -> (user submits answer) -> PENDING
 *   PENDING        -> (API call succeeds)   -> COMPLETED
 *   PENDING        -> (API call fails)      -> FAILED
 *   COMPLETED      -> (never changes again)
 * </pre>
 * </p>
 */
public enum EvaluationStatus {

    /**
     * No AI evaluation has been requested yet. Default state for all items in this build.
     */
    NOT_REQUESTED("not_requested"),

    /**
     * AI evaluation requested and currently in progress (async).
     * Reserved for future use when a real API call is in flight.
     */
    PENDING("pending"),

    /**
     * AI evaluation completed successfully and results are available.
     * Reserved for future use after a successful API response is parsed.
     */
    COMPLETED("completed"),

    /**
     * AI evaluation was attempted but failed (network error, timeout, quota exceeded, etc.).
     * Reserved for future error-handling when a real API is integrated.
     */
    FAILED("failed"),

    /**
     * AI integration is explicitly not available in this build.
     * All items in the current prototype use this status.
     * The UI must display a clear, non-misleading placeholder when this status is set.
     */
    NOT_AVAILABLE("not_available");

    private final String key;

    EvaluationStatus(String key) {
        this.key = key;
    }

    /** Returns the serialization key for this status (used in JSON persistence). */
    public String getKey() {
        return key;
    }

    /** Parses a serialization key back into an EvaluationStatus. Defaults to NOT_AVAILABLE. */
    public static EvaluationStatus fromKey(String key) {
        if (key == null) return NOT_AVAILABLE;
        for (EvaluationStatus s : values()) {
            if (s.key.equals(key)) return s;
        }
        return NOT_AVAILABLE;
    }
}
