package com.example.interviewai.ai;

/**
 * Async callback interface for receiving an AI evaluation result.
 *
 * <p>Implement this interface wherever you need to react to an evaluation completing
 * (e.g. in ResultActivity or a background worker). Both methods are always called
 * on the main (UI) thread.</p>
 *
 * <h3>Future integration note:</h3>
 * <p>When wiring a real AI API call (e.g. via Retrofit or OkHttp), post the result
 * back to the main thread using {@code Activity.runOnUiThread()} or a Handler before
 * calling these callbacks.</p>
 */
public interface EvaluationCallback {

    /**
     * Called when evaluation has completed successfully.
     * @param response A fully populated {@link EvaluationResponse} with
     *                 {@link EvaluationStatus#COMPLETED} status.
     */
    void onSuccess(EvaluationResponse response);

    /**
     * Called when evaluation failed (API error, network failure, timeout, etc.).
     * @param errorMessage A human-readable description of the failure cause.
     *                     Display this in the UI to inform the user gracefully.
     */
    void onFailure(String errorMessage);
}
