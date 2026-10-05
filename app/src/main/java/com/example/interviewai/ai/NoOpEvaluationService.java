package com.example.interviewai.ai;

import java.util.List;

/**
 * A no-operation implementation of {@link AnswerEvaluationService} used throughout
 * the current prototype build.
 *
 * <p><b>This class does NOT perform any AI evaluation.</b> It immediately returns
 * {@link EvaluationStatus#NOT_AVAILABLE} for every request, which the UI renders
 * as a clear "AI evaluation not yet available" placeholder.</p>
 *
 * <p>This class exists solely to ensure the abstraction layer is wired end-to-end
 * so that no Interview screen, Result screen, or data class needs to be rewritten
 * when real AI integration is added.</p>
 *
 * <h3>How to replace this with real AI evaluation:</h3>
 * <ol>
 *   <li>Create {@code GeminiEvaluationService implements AnswerEvaluationService}
 *       (or any other provider class).</li>
 *   <li>In {@link com.example.interviewai.workflow.InterviewWorkflowManager},
 *       replace the {@code new NoOpEvaluationService()} instantiation with your
 *       new service class.</li>
 *   <li>No other file needs to change.</li>
 * </ol>
 */
public class NoOpEvaluationService implements AnswerEvaluationService {

    @Override
    public void evaluate(EvaluationRequest request, EvaluationCallback callback) {
        // Immediately notify the caller that AI evaluation is not available.
        // When integrating a real AI service, replace this body with an async API call.
        callback.onSuccess(EvaluationResponse.notAvailable());
    }

    @Override
    public void evaluateSession(List<EvaluationRequest> requests, SessionEvaluationCallback callback) {
        // Session-level AI evaluation is not available in this build.
        // When integrating, call the AI's batch evaluation endpoint here.
        callback.onSuccess(SessionEvaluationResult.notAvailable());
    }

    @Override
    public boolean isAvailable() {
        // Always false ? this implementation has no real AI backend.
        return false;
    }
}
