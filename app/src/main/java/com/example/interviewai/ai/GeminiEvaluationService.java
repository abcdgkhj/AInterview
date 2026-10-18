package com.example.interviewai.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementasi {@link AnswerEvaluationService} yang terhubung langsung
 * ke Google Gemini AI (model gemini-3.8-flash).
 */
public class GeminiEvaluationService implements AnswerEvaluationService {

    @Override
    public void evaluate(EvaluationRequest request, EvaluationCallback callback) {
        GeminiService.evaluateAnswer(request, callback);
    }

    @Override
    public void evaluateSession(List<EvaluationRequest> requests, SessionEvaluationCallback callback) {
        if (requests == null || requests.isEmpty()) {
            callback.onSuccess(SessionEvaluationResult.notAvailable());
            return;
        }

        final int total = requests.size();
        final List<EvaluationResponse> responses = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger completedCount = new AtomicInteger(0);

        for (int i = 0; i < total; i++) {
            final EvaluationRequest req = requests.get(i);
            evaluate(req, new EvaluationCallback() {
                @Override
                public void onSuccess(EvaluationResponse response) {
                    responses.add(response);
                    checkFinish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    responses.add(EvaluationResponse.failed(errorMessage));
                    checkFinish();
                }

                private void checkFinish() {
                    if (completedCount.incrementAndGet() == total) {
                        int sumScore = 0;
                        int validCount = 0;
                        List<String> keyStrengths = new ArrayList<>();
                        List<String> priorityImprovements = new ArrayList<>();
                        for (EvaluationResponse res : responses) {
                            if (res.isCompleted()) {
                                sumScore += res.getOverallScore();
                                validCount++;
                                if (res.getStrengths() != null && !res.getStrengths().isEmpty()) {
                                    keyStrengths.add(res.getStrengths());
                                }
                                if (res.getImprovements() != null && !res.getImprovements().isEmpty()) {
                                    priorityImprovements.add(res.getImprovements());
                                }
                            }
                        }
                        int avgScore = validCount > 0 ? Math.round((float) sumScore / validCount) : 70;
                        String readiness = avgScore >= 80 ? "Sangat Siap" : (avgScore >= 60 ? "Siap" : "Perlu Latihan");
                        SessionEvaluationResult result = new SessionEvaluationResult(
                                EvaluationStatus.COMPLETED,
                                avgScore,
                                readiness,
                                "Semua jawaban telah dievaluasi oleh AInterview.",
                                keyStrengths,
                                priorityImprovements
                        );
                        callback.onSuccess(result);
                    }
                }
            });
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
