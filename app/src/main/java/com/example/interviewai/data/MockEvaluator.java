package com.example.interviewai.data;

import com.example.interviewai.model.InterviewItem;
import com.example.interviewai.model.InterviewQuestion;
import com.example.interviewai.model.InterviewSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated This class generated heuristic scores framed as AI evaluation output,
 * which could mislead users into believing real AI analysis had been performed.
 * It is retained for reference only and is no longer called by the application.
 *
 * <p>The current application uses {@link com.example.interviewai.workflow.InterviewWorkflowManager}
 * (which calls {@link com.example.interviewai.ai.NoOpEvaluationService}) to complete sessions
 * with honest {@link com.example.interviewai.ai.EvaluationStatus#NOT_AVAILABLE} status on every item.</p>
 *
 * <p>When real AI integration is ready, implement
 * {@link com.example.interviewai.ai.AnswerEvaluationService} instead of reviving this class.</p>
 */
@Deprecated
public class MockEvaluator {

    public static InterviewSession evaluateSession(String candidateName, String roleCategory,
                                                   List<InterviewQuestion> questions, List<String> answers) {
        return evaluateSession(candidateName, roleCategory, "Intermediate", "Indonesian", questions, answers);
    }

    public static InterviewSession evaluateSession(String candidateName, String roleCategory,
                                                   String difficulty, String language,
                                                   List<InterviewQuestion> questions, List<String> answers) {
        List<InterviewItem> evaluatedItems = new ArrayList<>();
        int totalScore = 0;
        int answeredCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestion q = questions.get(i);
            String ans = (answers != null && i < answers.size()) ? answers.get(i) : "";

            InterviewItem evaluatedItem = evaluateAnswer(q, ans, language);
            evaluatedItems.add(evaluatedItem);
            totalScore += evaluatedItem.getScore();
            if (ans != null && !ans.trim().isEmpty()) {
                answeredCount++;
            }
        }

        int overallScore = evaluatedItems.isEmpty() ? 0 : Math.round((float) totalScore / evaluatedItems.size());

        boolean isEnglish = "English".equalsIgnoreCase(language);
        String summaryStatus;
        String summaryFeedback;

        if (isEnglish) {
            if (overallScore >= 88) {
                summaryStatus = "Outstanding (Highly Prepared)";
                summaryFeedback = "Your answers are thorough, structured, and backed by solid context. You demonstrated clear problem-solving methodology.";
            } else if (overallScore >= 75) {
                summaryStatus = "Ready (Good Performance)";
                summaryFeedback = "Good domain foundation and relevant examples. Incorporate quantifiable impacts and standard frameworks (e.g. STAR) to make your profile stand out even more.";
            } else if (overallScore >= 60) {
                summaryStatus = "Good Progress (Needs Polish)";
                summaryFeedback = "You grasp core concepts well, though some answers were concise or general. Provide deeper practical context and specific reasoning.";
            } else {
                summaryStatus = "Needs More Practice";
                summaryFeedback = "Answers were brief or partially incomplete. Take time to structure thoughts, articulate solutions, and detail past learnings.";
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

        String validName = (candidateName == null || candidateName.trim().isEmpty()) ? "Kandidat" : candidateName.trim();
        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        return new InterviewSession(
                sessionId,
                validName,
                roleCategory,
                difficulty,
                language,
                System.currentTimeMillis(),
                overallScore,
                summaryStatus,
                summaryFeedback,
                evaluatedItems
        );
    }

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
                sampleAnswer = "Recommended Answer: Introduce context, outline your key methodology, and illustrate with an actionable past experience.";
            } else {
                strength = "Pertanyaan ini dilewati / belum dijawab.";
                improvement = "Berlatihlah mengutarakan kerangka pemikiran awal meskipun belum menguasai topik sepenuhnya, hindari mengosongkan jawaban.";
                sampleAnswer = "Contoh Jawaban Ideal: Mulai dengan konteks peran, jelaskan tantangan yang dihadapi, langkah sistematis yang Anda ambil, dan sebutkan hasil positif yang terukur.";
            }
        } else if (wordCount < 10) {
            score = 55 + (wordCount * 2);
            if (isEnglish) {
                strength = "Direct answer addressing the core question prompt.";
                improvement = "The explanation is brief. Expand with context, actions taken, and final outcomes.";
                sampleAnswer = "Recommended Answer: Start with the problem definition, describe your hands-on intervention, and highlight key results.";
            } else {
                strength = "Anda telah menjawab pertanyaan secara langsung.";
                improvement = "Jawaban terlalu singkat. Jelaskan konteks, aksi yang diambil, dan hasil yang diperoleh agar pewawancara memperoleh gambaran utuh.";
                sampleAnswer = "Contoh Jawaban Ideal: Mulai dengan konteks peran, jelaskan tantangan yang dihadapi, langkah sistematis yang Anda ambil, dan sebutkan hasil positif yang terukur.";
            }
        } else if (wordCount < 30) {
            score = 72 + Math.min(10, (wordCount - 10) / 2);
            if (isEnglish) {
                strength = "Core principles were communicated clearly and concisely.";
                improvement = "Include real-world metrics or specific technical decisions to enhance persuasiveness.";
                sampleAnswer = "Recommended Answer: Detail your specific role, technical tools utilized, and metrics achieved (e.g. reduced latency by 30%).";
            } else {
                strength = "Poin utama sudah tersampaikan dengan bahasa yang jelas.";
                improvement = "Tambahkan contoh kasus nyata atau metrik konkret dari pengalaman Anda sebelumnya untuk memperkuat kredibilitas.";
                sampleAnswer = "Contoh Jawaban Ideal: Berikan ilustrasi kasus nyata dari proyek sebelumnya, peran spesifik Anda, dan metrik dampak yang dicapai.";
            }
        } else if (wordCount < 60) {
            score = 84 + Math.min(8, (wordCount - 30) / 5);
            if (isEnglish) {
                strength = "Well-structured answer demonstrating solid technical depth.";
                improvement = "Tie the learning back to how it provides immediate business or organizational value.";
                sampleAnswer = "Recommended Answer: Highlight strategic trade-offs considered and post-implementation reflections.";
            } else {
                strength = "Jawaban terstruktur baik dengan kedalaman penjelasan yang memadai.";
                improvement = "Pertajam bagian kesimpulan atau bagaimana pengalaman tersebut dapat memberikan nilai tambah langsung bagi perusahaan.";
                sampleAnswer = "Contoh Jawaban Ideal: Perjelas analisis trade-off yang Anda pertimbangkan dan dampak berkelanjutan yang dirasakan tim.";
            }
        } else {
            score = 92 + Math.min(6, (wordCount - 60) / 10);
            if (isEnglish) {
                strength = "Exceptional depth, structured articulation, and comprehensive coverage.";
                improvement = "Keep up this high standard of communication and ensure pacing remains crisp.";
                sampleAnswer = "Recommended Answer: Your articulated response matches industry best practices.";
            } else {
                strength = "Pemaparan sangat detail, matang, dan mencerminkan penguasaan topik yang mendalam.";
                improvement = "Pertahankan gaya penyampaian ini dan pastikan ritme berbicara tetap fokus pada inti terpenting.";
                sampleAnswer = "Contoh Jawaban Ideal: Respon yang Anda susun telah memenuhi standar profesional industri.";
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
}
