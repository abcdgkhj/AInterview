package com.example.interviewai.ai;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a request to evaluate a single question-answer pair via an AI service.
 *
 * <p>This model encapsulates everything an AI evaluator needs to assess one answer.
 * It is intentionally decoupled from {@link com.example.interviewai.model.InterviewItem}
 * so that the AI layer can evolve independently of the storage layer.</p>
 *
 * <h3>Future integration note:</h3>
 * <p>When implementing a real AI service, serialize this object into the API request body
 * (e.g. as JSON). The {@code criteria} list maps directly to the evaluation dimensions
 * the AI should address in its response.</p>
 *
 * <p>Integration point: {@link AnswerEvaluationService#evaluate(EvaluationRequest, EvaluationCallback)}</p>
 */
public class EvaluationRequest implements Serializable {

    /**
     * The original question text shown to the candidate.
     * Should be the exact string from {@link com.example.interviewai.model.InterviewQuestion#getQuestionText()}.
     */
    private final String questionText;

    /**
     * The candidate's typed answer, trimmed of leading/trailing whitespace.
     * An empty string indicates the question was skipped.
     */
    private final String candidateAnswer;

    /**
     * The interview category context (e.g. "Software Engineering", "Behavioral (STAR)").
     * Helps the AI tailor its evaluation to the domain.
     */
    private final String category;

    /**
     * The difficulty level of the question ("Beginner", "Intermediate", "Advanced").
     * The AI should calibrate its scoring expectations accordingly.
     */
    private final String difficulty;

    /**
     * The language in which the candidate answered ("Indonesian" or "English").
     * The AI must respond in the same language.
     */
    private final String language;

    /**
     * The evaluation criteria dimensions the AI should score.
     * Suggested default: {@link EvaluationRequest#DEFAULT_CRITERIA}
     */
    private final List<String> criteria;

    /**
     * Default evaluation criteria. Use these unless the AI service requires different dimensions.
     * <ul>
     *   <li>relevance     ? How directly the answer addresses the question</li>
     *   <li>clarity       ? How clearly and logically the answer is expressed</li>
     *   <li>completeness  ? Whether the answer covers all required aspects</li>
     *   <li>communication ? Professionalism, tone, and structure of the response</li>
     *   <li>technical     ? Accuracy of technical claims (weighted by category)</li>
     * </ul>
     */
    public static final List<String> DEFAULT_CRITERIA = java.util.Arrays.asList(
            "relevance", "clarity", "completeness", "communication", "technical"
    );

    public EvaluationRequest(String questionText, String candidateAnswer,
                             String category, String difficulty, String language,
                             List<String> criteria) {
        this.questionText = questionText != null ? questionText : "";
        this.candidateAnswer = candidateAnswer != null ? candidateAnswer.trim() : "";
        this.category = category != null ? category : "General";
        this.difficulty = difficulty != null ? difficulty : "Intermediate";
        this.language = language != null ? language : "Indonesian";
        this.criteria = criteria != null ? criteria : DEFAULT_CRITERIA;
    }

    /** Convenience constructor using default evaluation criteria. */
    public static EvaluationRequest withDefaults(String questionText, String candidateAnswer,
                                                 String category, String difficulty, String language) {
        return new EvaluationRequest(questionText, candidateAnswer,
                category, difficulty, language, DEFAULT_CRITERIA);
    }

    public String getQuestionText()     { return questionText; }
    public String getCandidateAnswer()  { return candidateAnswer; }
    public String getCategory()         { return category; }
    public String getDifficulty()       { return difficulty; }
    public String getLanguage()         { return language; }
    public List<String> getCriteria()   { return criteria; }

    /** Returns true if the candidate did not provide any answer. */
    public boolean isAnswerEmpty() {
        return candidateAnswer.isEmpty();
    }
}
