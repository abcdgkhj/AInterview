package com.example.interviewai.workflow;

import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.model.InterviewItem;
import com.example.interviewai.model.InterviewQuestion;
import com.example.interviewai.model.InterviewSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mengelola state alur wawancara (business logic), navigasi pertanyaan,
 * validasi, dan penyimpanan jawaban sementara di memori tanpa bergantung pada AI eksternal.
 */
public class InterviewWorkflowManager implements Serializable {

    private final String candidateName;
    private final String category;
    private final String difficulty;
    private final String language;
    private final int requestedCount;

    private List<InterviewQuestion> questions;
    private List<String> answers;
    private int currentIndex = 0;
    private boolean isSubmitting = false;

    public InterviewWorkflowManager(String candidateName, String category, String difficulty,
                                    String language, int requestedCount) {
        this.candidateName = (candidateName != null && !candidateName.trim().isEmpty())
                ? candidateName.trim() : "Kandidat";
        this.category = (category != null && !category.trim().isEmpty())
                ? category.trim() : QuestionBank.CATEGORY_GENERAL;
        this.difficulty = (difficulty != null && !difficulty.trim().isEmpty())
                ? difficulty.trim() : QuestionBank.DIFFICULTY_INTERMEDIATE;
        this.language = (language != null && !language.trim().isEmpty())
                ? language.trim() : QuestionBank.LANG_ID;
        this.requestedCount = requestedCount > 0 ? requestedCount : 5;

        loadQuestions();
    }

    private void loadQuestions() {
        this.questions = QuestionBank.getFilteredQuestions(category, difficulty, language, requestedCount);
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }

        this.answers = new ArrayList<>();
        for (int i = 0; i < this.questions.size(); i++) {
            this.answers.add("");
        }
        this.currentIndex = 0;
    }

    public boolean hasQuestions() {
        return questions != null && !questions.isEmpty();
    }

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentQuestionNumber() {
        return currentIndex + 1;
    }

    public int getProgressPercentage() {
        int total = getTotalQuestions();
        if (total == 0) return 0;
        return (int) (((float) (currentIndex + 1) / total) * 100);
    }

    public InterviewQuestion getCurrentQuestion() {
        if (!hasQuestions() || currentIndex < 0 || currentIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentIndex);
    }

    public void saveCurrentAnswer(String answerText) {
        if (answers != null && currentIndex >= 0 && currentIndex < answers.size()) {
            answers.set(currentIndex, answerText != null ? answerText.trim() : "");
        }
    }

    public String getCurrentAnswer() {
        if (answers != null && currentIndex >= 0 && currentIndex < answers.size()) {
            String ans = answers.get(currentIndex);
            return ans != null ? ans : "";
        }
        return "";
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public boolean hasNext() {
        return currentIndex < getTotalQuestions() - 1;
    }

    public boolean isLastQuestion() {
        return currentIndex == getTotalQuestions() - 1;
    }

    public boolean goToPrevious() {
        if (hasPrevious()) {
            currentIndex--;
            return true;
        }
        return false;
    }

    public boolean goToNext() {
        if (hasNext()) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public int getAnsweredCount() {
        if (answers == null) return 0;
        int count = 0;
        for (String ans : answers) {
            if (ans != null && !ans.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getUnansweredCount() {
        return getTotalQuestions() - getAnsweredCount();
    }

    public boolean isAllAnswered() {
        return getUnansweredCount() == 0;
    }

    public boolean isSubmitting() {
        return isSubmitting;
    }

    public void setSubmitting(boolean submitting) {
        this.isSubmitting = submitting;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * Menyelesaikan sesi latihan dan membentuk model InterviewSession resmi.
     * Tidak memfabrikasi penilaian AI tiruan sesuai instruksi persyaratan:
     * Nilai dan ulasan ditandai secara jujur dan transparan sebagai hasil rekaman lokal
     * dengan AI evaluation yang belum diintegrasikan.
     */
    public InterviewSession createCompletedSession() {
        List<InterviewItem> items = new ArrayList<>();

        boolean isEnglish = "English".equalsIgnoreCase(language);

        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestion q = questions.get(i);
            String ans = (i < answers.size()) ? answers.get(i) : "";
            boolean isAnswered = ans != null && !ans.trim().isEmpty();

            String statusNotice;
            String guideline;
            String aiPlaceholderNotice;

            if (isEnglish) {
                statusNotice = isAnswered ? "Recorded locally" : "(Skipped / Unanswered)";
                guideline = "Reference Guide: " + q.getTip();
                aiPlaceholderNotice = "AI Evaluation: Currently unavailable (Pending API integration).";
            } else {
                statusNotice = isAnswered ? "Tercatat di sistem lokal" : "(Dilewati / Tidak dijawab)";
                guideline = "Panduan Jawaban: " + q.getTip();
                aiPlaceholderNotice = "Evaluasi AI: Belum tersedia (Menunggu integrasi layanan AI).";
            }

            items.add(new InterviewItem(
                    q.getQuestionText(),
                    ans != null ? ans.trim() : "",
                    isAnswered ? 100 : 0, // Indikator penyelesaian: 100% terjawab / 0% tidak dijawab
                    statusNotice,
                    guideline,
                    aiPlaceholderNotice
            ));
        }

        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        String summaryStatus;
        String summaryFeedback;

        int answered = getAnsweredCount();
        int total = getTotalQuestions();

        if (isEnglish) {
            summaryStatus = "Practice Session Completed";
            summaryFeedback = "All your responses (" + answered + " of " + total + " questions answered) have been successfully saved to local storage. AI semantic evaluation is currently pending integration.";
        } else {
            summaryStatus = "Sesi Wawancara Selesai";
            summaryFeedback = "Jawaban Anda (" + answered + " dari " + total + " pertanyaan terjawab) telah berhasil disimpan secara lokal. Evaluasi analitik AI saat ini belum diaktifkan (menunggu integrasi API).";
        }

        // Skor keseluruhan mencerminkan tingkat keterisian pertanyaan latihan
        int completionRate = total > 0 ? Math.round(((float) answered / total) * 100) : 0;

        return new InterviewSession(
                sessionId,
                candidateName,
                category,
                difficulty,
                language,
                System.currentTimeMillis(),
                completionRate,
                summaryStatus,
                summaryFeedback,
                items
        );
    }
}
