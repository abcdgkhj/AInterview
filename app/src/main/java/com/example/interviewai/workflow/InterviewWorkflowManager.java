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
 * validasi, penyimpanan jawaban sementara, dan evaluasi template di memori
 * tanpa bergantung pada AI eksternal.
 */
public class InterviewWorkflowManager implements Serializable {

    private final String candidateName;
    private final String category;
    private final String difficulty;
    private final String language;
    private final int requestedCount;

    private List<InterviewQuestion> questions;
    private List<String> answers;
    private List<InterviewItem> evaluatedItems;
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
        this.evaluatedItems = new ArrayList<>();
        for (int i = 0; i < this.questions.size(); i++) {
            this.answers.add("");
            this.evaluatedItems.add(null);
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
     * Returns whether the current question has already been evaluated (feedback shown).
     */
    public boolean isCurrentQuestionEvaluated() {
        if (evaluatedItems != null && currentIndex >= 0 && currentIndex < evaluatedItems.size()) {
            return evaluatedItems.get(currentIndex) != null;
        }
        return false;
    }

    /**
     * Returns the evaluated InterviewItem for the current question, or null if not yet evaluated.
     */
    public InterviewItem getCurrentEvaluatedItem() {
        if (evaluatedItems != null && currentIndex >= 0 && currentIndex < evaluatedItems.size()) {
            return evaluatedItems.get(currentIndex);
        }
        return null;
    }

    /**
     * Evaluates the current answer using template/heuristic feedback.
     * This method generates a score and feedback based on word count and answer content.
     * When real AI is integrated, replace this method's body with an API call.
     *
     * @return the evaluated InterviewItem with score, strength, improvement, and suggestion
     */
    public InterviewItem evaluateCurrentAnswer() {
        InterviewQuestion question = getCurrentQuestion();
        String answer = getCurrentAnswer();
        if (question == null) return null;

        InterviewItem item = evaluateAnswer(question, answer, language);

        // Store the evaluated item
        if (evaluatedItems != null && currentIndex >= 0 && currentIndex < evaluatedItems.size()) {
            evaluatedItems.set(currentIndex, item);
        }

        return item;
    }

    /**
     * Template-based answer evaluation using word count heuristics.
     * Produces realistic score, strengths, weaknesses, and suggestions.
     * Ready to be replaced with real AI evaluation in the future.
     */
    private static InterviewItem evaluateAnswer(InterviewQuestion question, String answer, String language) {
        String trimmed = answer != null ? answer.trim() : "";
        int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        boolean isEnglish = "English".equalsIgnoreCase(language);

        int score;
        String strength;
        String improvement;
        String sampleAnswer;

        if (trimmed.isEmpty()) {
            score = 30;
            if (isEnglish) {
                strength = "Question skipped or left unanswered.";
                improvement = "Practice tackling unfamiliar questions by breaking them down systematically rather than leaving them blank.";
                sampleAnswer = "Introduce context, outline your key methodology, and illustrate with an actionable past experience.";
            } else {
                strength = "Pertanyaan ini dilewati / belum dijawab.";
                improvement = "Berlatihlah mengutarakan kerangka pemikiran awal meskipun belum menguasai topik sepenuhnya, hindari mengosongkan jawaban.";
                sampleAnswer = "Mulai dengan konteks peran, jelaskan tantangan yang dihadapi, langkah sistematis yang Anda ambil, dan sebutkan hasil positif yang terukur.";
            }
        } else if (wordCount < 10) {
            score = 55 + (wordCount * 2);
            if (isEnglish) {
                strength = "Direct answer addressing the core question prompt.";
                improvement = "The explanation is brief. Expand with context, actions taken, and final outcomes.";
                sampleAnswer = "Start with the problem definition, describe your hands-on intervention, and highlight key results.";
            } else {
                strength = "Anda telah menjawab pertanyaan secara langsung.";
                improvement = "Jawaban terlalu singkat. Jelaskan konteks, aksi yang diambil, dan hasil yang diperoleh agar pewawancara memperoleh gambaran utuh.";
                sampleAnswer = "Mulai dengan konteks peran, jelaskan tantangan yang dihadapi, langkah sistematis yang Anda ambil, dan sebutkan hasil positif yang terukur.";
            }
        } else if (wordCount < 30) {
            score = 72 + Math.min(10, (wordCount - 10) / 2);
            if (isEnglish) {
                strength = "Core principles were communicated clearly and concisely.";
                improvement = "Include real-world metrics or specific technical decisions to enhance persuasiveness.";
                sampleAnswer = "Detail your specific role, technical tools utilized, and metrics achieved (e.g. reduced latency by 30%).";
            } else {
                strength = "Poin utama sudah tersampaikan dengan bahasa yang jelas.";
                improvement = "Tambahkan contoh kasus nyata atau metrik konkret dari pengalaman Anda sebelumnya untuk memperkuat kredibilitas.";
                sampleAnswer = "Berikan ilustrasi kasus nyata dari proyek sebelumnya, peran spesifik Anda, dan metrik dampak yang dicapai.";
            }
        } else if (wordCount < 60) {
            score = 84 + Math.min(8, (wordCount - 30) / 5);
            if (isEnglish) {
                strength = "Well-structured answer demonstrating solid technical depth.";
                improvement = "Tie the learning back to how it provides immediate business or organizational value.";
                sampleAnswer = "Highlight strategic trade-offs considered and post-implementation reflections.";
            } else {
                strength = "Jawaban terstruktur baik dengan kedalaman penjelasan yang memadai.";
                improvement = "Pertajam bagian kesimpulan atau bagaimana pengalaman tersebut dapat memberikan nilai tambah langsung bagi perusahaan.";
                sampleAnswer = "Perjelas analisis trade-off yang Anda pertimbangkan dan dampak berkelanjutan yang dirasakan tim.";
            }
        } else {
            score = 92 + Math.min(6, (wordCount - 60) / 10);
            if (isEnglish) {
                strength = "Exceptional depth, structured articulation, and comprehensive coverage.";
                improvement = "Keep up this high standard of communication and ensure pacing remains crisp.";
                sampleAnswer = "Your articulated response matches industry best practices.";
            } else {
                strength = "Pemaparan sangat detail, matang, dan mencerminkan penguasaan topik yang mendalam.";
                improvement = "Pertahankan gaya penyampaian ini dan pastikan ritme berbicara tetap fokus pada inti terpenting.";
                sampleAnswer = "Respon yang Anda susun telah memenuhi standar profesional industri.";
            }
        }

        return new InterviewItem(
                question.getQuestionText(),
                trimmed,
                score,
                strength,
                improvement,
                sampleAnswer
        );
    }

    /**
     * Menyelesaikan sesi latihan dan membentuk model InterviewSession resmi.
     * Uses the evaluated items stored during the interview for proper feedback scores.
     */
    public InterviewSession createCompletedSession() {
        List<InterviewItem> items = new ArrayList<>();
        int totalScore = 0;

        for (int i = 0; i < questions.size(); i++) {
            InterviewItem evaluated = (evaluatedItems != null && i < evaluatedItems.size())
                    ? evaluatedItems.get(i) : null;

            if (evaluated != null) {
                items.add(evaluated);
                totalScore += evaluated.getScore();
            } else {
                // Question was not submitted/evaluated — create a placeholder item
                InterviewQuestion q = questions.get(i);
                String ans = (i < answers.size()) ? answers.get(i) : "";
                InterviewItem placeholder = evaluateAnswer(q, ans, language);
                items.add(placeholder);
                totalScore += placeholder.getScore();
            }
        }

        int overallScore = items.isEmpty() ? 0 : Math.round((float) totalScore / items.size());

        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        boolean isEnglish = "English".equalsIgnoreCase(language);
        String summaryStatus;
        String summaryFeedback;

        if (isEnglish) {
            if (overallScore >= 88) {
                summaryStatus = "Outstanding (Highly Prepared)";
                summaryFeedback = "Your answers are thorough, structured, and backed by solid context. You demonstrated clear problem-solving methodology.";
            } else if (overallScore >= 75) {
                summaryStatus = "Ready (Good Performance)";
                summaryFeedback = "Good domain foundation and relevant examples. Incorporate quantifiable impacts and standard frameworks (e.g. STAR) to stand out.";
            } else if (overallScore >= 60) {
                summaryStatus = "Good Progress (Needs Polish)";
                summaryFeedback = "You grasp core concepts well, though some answers were concise or general. Provide deeper practical context.";
            } else {
                summaryStatus = "Needs More Practice";
                summaryFeedback = "Answers were brief or partially incomplete. Take time to structure thoughts and detail past learnings.";
            }
        } else {
            if (overallScore >= 88) {
                summaryStatus = "Sangat Siap (Outstanding)";
                summaryFeedback = "Jawaban Anda sangat komprehensif, menunjukkan pemikiran terstruktur dan contoh konkret. Anda memiliki potensi tinggi untuk lolos ke tahap berikutnya.";
            } else if (overallScore >= 75) {
                summaryStatus = "Siap (Ready)";
                summaryFeedback = "Pemaparan Anda sudah baik dan relevan. Tingkatkan lagi penyampaian dampak kuantitatif dan gunakan formula STAR agar jawaban lebih menonjol.";
            } else if (overallScore >= 60) {
                summaryStatus = "Cukup Baik (Good Progress)";
                summaryFeedback = "Anda memahami pokok pertanyaan, namun beberapa jawaban masih terlalu umum. Berikan rincian pengalaman nyata dan alasan lebih spesifik.";
            } else {
                summaryStatus = "Perlu Latihan Tambahan";
                summaryFeedback = "Jawaban Anda masih singkat atau belum terjawab lengkap. Luangkan waktu untuk menguraikan proses berpikir, solusi yang Anda tawarkan, dan pencapaian nyata.";
            }
        }

        return new InterviewSession(
                sessionId,
                candidateName,
                category,
                difficulty,
                language,
                System.currentTimeMillis(),
                overallScore,
                summaryStatus,
                summaryFeedback,
                items
        );
    }
}
