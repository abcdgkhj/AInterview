package com.example.interviewai.ai;

import java.util.List;

/**
 * Service interface that defines the contract for AI-based interview answer evaluation.
 *
 * <p>This interface is the primary integration point for connecting a real AI backend.
 * The application currently uses {@link NoOpEvaluationService} as the sole implementation,
 * which explicitly marks all evaluations as {@link EvaluationStatus#NOT_AVAILABLE}.</p>
 *
 * <h2>How to add real AI evaluation (future steps):</h2>
 * <ol>
 *   <li>Create a new class, e.g. {@code GeminiEvaluationService implements AnswerEvaluationService}</li>
 *   <li>In its {@link #evaluate} method, serialize the {@link EvaluationRequest} to JSON
 *       and call your chosen AI API (Gemini, OpenAI, etc.) asynchronously.</li>
 *   <li>On success: parse the response into an {@link EvaluationResponse} with
 *       {@link EvaluationStatus#COMPLETED} and call {@link EvaluationCallback#onSuccess}.</li>
 *   <li>On failure: call {@link EvaluationCallback#onFailure} with a user-friendly error message.</li>
 *   <li>Replace the {@code NoOpEvaluationService} injection site in
 *       {@link com.example.interviewai.workflow.InterviewWorkflowManager} with your new class.</li>
 *   <li>No changes to {@link com.example.interviewai.InterviewActivity},
 *       {@link com.example.interviewai.ResultActivity}, or the database layer are required.</li>
 * </ol>
 *
 * <h2>Evaluation criteria:</h2>
 * <p>The AI should evaluate answers against these five dimensions
 * (defined in {@link EvaluationRequest#DEFAULT_CRITERIA}):</p>
 * <ul>
 *   <li><b>relevance</b>     ? How directly the answer addresses the specific question</li>
 *   <li><b>clarity</b>       ? How clearly and logically the answer is expressed</li>
 *   <li><b>completeness</b>  ? Whether all expected aspects of the answer are covered</li>
 *   <li><b>communication</b> ? Professional tone, structured flow, and appropriate terminology</li>
 *   <li><b>technical</b>     ? Accuracy of any technical claims (weighted by category)</li>
 * </ul>
 */
public interface AnswerEvaluationService {

    /**
     * Evaluates a single question-answer pair asynchronously.
     *
     * <p>Implementations MUST call exactly one of {@link EvaluationCallback#onSuccess}
     * or {@link EvaluationCallback#onFailure} ? never both, never neither.</p>
     *
     * <p>Both callback methods must be invoked on the main (UI) thread.</p>
     *
     * @param request  The evaluation request containing the question, answer, and context.
     * @param callback Receives the result or error notification.
     */
    void evaluate(EvaluationRequest request, EvaluationCallback callback);

    /**
     * Evaluates an entire session's worth of question-answer pairs in one batch call.
     * Implementations may process requests sequentially or in parallel.
     *
     * <p>This is a convenience method. A default implementation may loop over
     * individual {@link #evaluate} calls.</p>
     *
     * @param requests One request per question answered in the session.
     * @param callback Receives the aggregated {@link SessionEvaluationResult} on completion.
     */
    void evaluateSession(List<EvaluationRequest> requests, SessionEvaluationCallback callback);

    /**
     * Returns whether this implementation can perform real AI evaluation.
     * Always returns {@code false} for {@link NoOpEvaluationService}.
     * Returns {@code true} for a real API-backed implementation.
     */
    boolean isAvailable();
}
