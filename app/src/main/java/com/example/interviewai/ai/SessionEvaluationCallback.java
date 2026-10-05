package com.example.interviewai.ai;

/**
 * Async callback interface for receiving a session-level AI evaluation result.
 *
 * <h3>Future integration note:</h3>
 * <p>Both methods must be called on the main (UI) thread.
 * Wire this callback in ResultActivity or a ViewModel to trigger a UI refresh
 * once session evaluation completes.</p>
 */
public interface SessionEvaluationCallback {

    /**
     * Called when the full session evaluation has completed successfully.
     * @param result A fully populated {@link SessionEvaluationResult} with
     *               {@link EvaluationStatus#COMPLETED} status.
     */
    void onSuccess(SessionEvaluationResult result);

    /**
     * Called when session evaluation failed.
     * @param errorMessage Human-readable reason for the failure.
     */
    void onFailure(String errorMessage);
}
