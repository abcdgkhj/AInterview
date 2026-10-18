package com.example.interviewai.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.data.QuestionBank;
import com.example.interviewai.model.InterviewItem;
import com.example.interviewai.model.InterviewQuestion;
import com.example.interviewai.model.InterviewSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service integrasi Google Gemini AI untuk:
 * 1. Menghasilkan pertanyaan wawancara kerja yang unik (tidak mengulang dari database riwayat).
 * 2. Mengevaluasi jawaban user (Skor 1-100, Kelebihan, Kekurangan, Saran Perbaikan, dan Rekomendasi Jawaban Ideal).
 */
public class GeminiService {

    private static final String TAG = "GeminiService";
    public static final String API_KEY = GeminiApiKey.API_KEY;
    public static final String MODEL_NAME = "gemini-flash-lite-latest";
    private static final String ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL_NAME + ":generateContent?key=" + API_KEY;

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface QuestionGenerationCallback {
        void onSuccess(List<InterviewQuestion> questions);
        void onError(String errorMessage, List<InterviewQuestion> fallbackQuestions);
    }

    /**
     * Generate pertanyaan interview unik menggunakan AInterview AI.
     * AI mengetahui pertanyaan yang sudah pernah ditanyakan sebelumnya dari history session.
     */
    public static void generateQuestions(Context context,
                                          String category,
                                          String difficulty,
                                          String language,
                                          int count,
                                          QuestionGenerationCallback callback) {
        final int targetCount = count > 0 ? count : 5;
        final String safeCategory = (category != null && !category.isEmpty()) ? category : QuestionBank.CATEGORY_UMUM;
        final String safeDifficulty = (difficulty != null && !difficulty.isEmpty()) ? difficulty : QuestionBank.DIFFICULTY_INTERMEDIATE;
        final String safeLanguage = (language != null && !language.isEmpty()) ? language : QuestionBank.LANG_ID;

        // Ambil riwayat pertanyaan sebelumnya dari database penyimpanan sesi
        List<String> pastQuestions = new ArrayList<>();
        if (context != null) {
            List<InterviewSession> sessions = InterviewStorage.getSessions(context);
            if (sessions != null) {
                for (InterviewSession s : sessions) {
                    if (s.getItems() != null) {
                        for (InterviewItem item : s.getItems()) {
                            if (item.getQuestion() != null && !item.getQuestion().trim().isEmpty()) {
                                String q = item.getQuestion().trim();
                                if (!pastQuestions.contains(q)) {
                                    pastQuestions.add(q);
                                }
                            }
                        }
                    }
                }
            }
        }

        executor.execute(() -> {
            try {
                // Tentukan konteks Umum vs Teknis
                boolean isTeknis = safeCategory.toLowerCase().contains("teknis")
                        || safeCategory.toLowerCase().contains("it")
                        || safeCategory.toLowerCase().contains("marketing")
                        || safeCategory.toLowerCase().contains("accounting")
                        || safeCategory.toLowerCase().contains("software");

                StringBuilder promptBuilder = new StringBuilder();
                promptBuilder.append("Anda adalah seorang pewawancara profesional dan HR/Technical Hiring Manager ahli.\n");
                promptBuilder.append("Tugas Anda: Buatlah ").append(targetCount).append(" pertanyaan simulasi interview kerja yang unik, berkualitas tinggi, dan relevan.\n\n");

                promptBuilder.append("KRITERIA WAWANCARA:\n");
                if (isTeknis) {
                    promptBuilder.append("- Jenis Wawancara: Interview Teknis\n");
                    promptBuilder.append("- Kategori / Bidang Teknis yang dipilih user: ").append(safeCategory).append("\n");
                    promptBuilder.append("  (Pertanyaan WAJIB berfokus pada keahlian, konsep, metodologi, dan studi kasus praktis di bidang ").append(safeCategory).append(")\n");
                } else {
                    promptBuilder.append("- Jenis Wawancara: Interview Umum / Behavioral\n");
                    promptBuilder.append("- Kategori: Pertanyaan Umum (pengenalan diri, kerja tim, resolusi konflik, motivasi, kepemimpinan, dan etika kerja)\n");
                }
                promptBuilder.append("- Tingkat Kesulitan: ").append(safeDifficulty).append("\n");
                promptBuilder.append("- Bahasa Pertanyaan: ").append(safeLanguage).append("\n\n");

                if (!pastQuestions.isEmpty()) {
                    promptBuilder.append("DATABASE RIWAYAT PERTANYAAN SEBELUMNYA (Dilarang mengulang atau membuat pertanyaan yang mirip dengan daftar berikut):\n");
                    int maxHistory = Math.min(30, pastQuestions.size());
                    for (int i = 0; i < maxHistory; i++) {
                        promptBuilder.append(i + 1).append(". ").append(pastQuestions.get(i)).append("\n");
                    }
                    promptBuilder.append("Semua pertanyaan yang dihasilkan HARUS BARU dan UNIK dari riwayat di atas.\n\n");
                }

                promptBuilder.append("FORMAT OUTPUT WAJIB: Hanya keluarkan format JSON Array murni tanpa markdown/teks pembuka/penutup:\n");
                promptBuilder.append("[\n");
                promptBuilder.append("  {\n");
                promptBuilder.append("    \"id\": 1,\n");
                promptBuilder.append("    \"question\": \"teks pertanyaan...\"\n");
                promptBuilder.append("  }\n");
                promptBuilder.append("]");

                String responseJson = callGeminiApi(promptBuilder.toString(), true);
                List<InterviewQuestion> parsedQuestions = parseQuestionsFromJson(responseJson, safeCategory, safeDifficulty, safeLanguage);

                if (parsedQuestions != null && !parsedQuestions.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(parsedQuestions));
                    return;
                }

                // Fallback jika parsing menghasilkan daftar kosong
                throw new Exception("Hasil parsing pertanyaan dari AInterview kosong.");
            } catch (Exception e) {
                Log.e(TAG, "Gagal meng-generate pertanyaan via AInterview: " + e.getMessage(), e);
                List<InterviewQuestion> fallback = QuestionBank.getFilteredQuestions(safeCategory, safeDifficulty, safeLanguage, targetCount);
                mainHandler.post(() -> callback.onError("Koneksi AInterview bermasalah, menggunakan bank soal lokal: " + e.getMessage(), fallback));
            }
        });
    }

    /**
     * Evaluasi jawaban user menggunakan AInterview AI:
     * - Skor 1-100 (memperhatikan relevansi, panjang jawaban, substansi)
     * - Kelebihan (format bullet points)
     * - Kekurangan (format bullet points, mengabaikan typo)
     * - Saran perbaikan (format bullet points)
     * - Rekomendasi jawaban ideal
     */
    public static void evaluateAnswer(EvaluationRequest request, EvaluationCallback callback) {
        if (request == null) {
            mainHandler.post(() -> callback.onFailure("Permintaan evaluasi kosong."));
            return;
        }

        final String question = request.getQuestionText();
        final String answer = request.getCandidateAnswer();
        final String category = request.getCategory();
        final String difficulty = request.getDifficulty();
        final String language = request.getLanguage();

        // Jika user tidak menjawab atau kosong
        if (answer == null || answer.trim().isEmpty()) {
            boolean isEn = "English".equalsIgnoreCase(language);
            EvaluationResponse emptyResponse = new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                    .overallScore(30)
                    .strengths(isEn ? "• No response provided." : "• Tidak ada jawaban yang diberikan.")
                    .weaknesses(isEn ? "• Question was left blank." : "• Pertanyaan dilewati / kosong tanpa jawaban.")
                    .improvements(isEn ? "• Always attempt to provide key framework points rather than skipping."
                            : "• Cobalah untuk selalu memberikan poin kerangka pemikiran awal meskipun belum menguasai topik sepenuhnya.")
                    .modelAnswer(isEn ? "Introduce context, outline your key methodology, and illustrate with an actionable past experience."
                            : "Mulai dengan konteks peran, jelaskan tantangan yang dihadapi, langkah sistematis yang Anda ambil, dan sebutkan hasil positif yang terukur.")
                    .scoreRationale(isEn ? "Score 30: Blank answer." : "Skor 30: Jawaban kosong.")
                    .build();
            mainHandler.post(() -> callback.onSuccess(emptyResponse));
            return;
        }

        executor.execute(() -> {
            try {
                int wordCount = answer.trim().split("\\s+").length;

                StringBuilder prompt = new StringBuilder();
                prompt.append("Anda adalah seorang penilai wawancara kerja profesional berbasis AI.\n");
                prompt.append("Evaluasi jawaban kandidat wawancara berikut secara objektif dan mendalam.\n\n");
                prompt.append("Pertanyaan: ").append(question).append("\n");
                prompt.append("Jawaban User: ").append(answer).append("\n");
                prompt.append("Jumlah Kata Jawaban: ").append(wordCount).append(" kata\n");
                prompt.append("Kategori / Bidang: ").append(category).append("\n");
                prompt.append("Tingkat Kesulitan: ").append(difficulty).append("\n");
                prompt.append("Bahasa: ").append(language).append("\n\n");

                prompt.append("KRITERIA EVALUASI DAN PENILAIAN WAJIB:\n");
                prompt.append("1. Skor (1 - 100): Berikan skor realistis 1-100 berdasarkan faktor relevansi jawaban terhadap pertanyaan, panjang jawaban (apakah terlalu singkat < 15 kata, pas 30-100 kata, atau bertele-tele), kelengkapan, dan kedalaman isi.\n");
                prompt.append("2. Kelebihan: Analisis apa yang sudah baik, tepat, dan positif dari jawaban user.\n");
                prompt.append("3. Kekurangan: Analisis apa yang kurang lengkap, kelemahan jawaban, atau hal yang masih meleset dari jawaban user.\n");
                prompt.append("4. Saran Perbaikan: Berikan perbaikan konkret dan langkah praktis berdasarkan analisis kekurangan di atas (misal gunakan metode STAR, elaborasi metrik, atau perjelas peran).\n");
                prompt.append("5. Rekomendasi Jawaban Ideal: Berikan contoh kalimat jawaban yang ideal, terstruktur, dan profesional yang bisa langsung dipelajari oleh user untuk pertanyaan tersebut.\n");
                prompt.append("6. ABAIKAN TYPO: Abaikan segala bentuk kesalahan ketik (typo), salah eja kecil, atau slip jari yang dibuat oleh user. JANGAN mengurangi skor dan JANGAN menyebutkan typo pada kelemahan maupun saran perbaikan. Evaluasi murni berdasarkan substansi dan pemahaman konsep user.\n");
                prompt.append("7. FORMAT BULLET POINT: Tuliskan bagian 'strengths' (Kelebihan), 'weaknesses' (Kekurangan), dan 'improvement' (Saran Perbaikan) dalam bentuk poin-poin bullet (gunakan format '• ' di setiap poin baris baru, masing-masing 2-3 poin ringkas dan padat) agar rapi dan mudah dibaca.\n\n");

                prompt.append("FORMAT OUTPUT WAJIB: Keluarkan hanya JSON Object murni tanpa tanda kutip markdown:\n");
                prompt.append("{\n");
                prompt.append("  \"score\": 80,\n");
                prompt.append("  \"strengths\": \"• Poin kelebihan 1\\n• Poin kelebihan 2\",\n");
                prompt.append("  \"weaknesses\": \"• Poin kekurangan 1\\n• Poin kekurangan 2\",\n");
                prompt.append("  \"improvement\": \"• Langkah saran perbaikan 1\\n• Langkah saran perbaikan 2\",\n");
                prompt.append("  \"ideal_answer\": \"...\"\n");
                prompt.append("}");

                String responseJson = callGeminiApi(prompt.toString(), true);
                EvaluationResponse parsedResponse = parseEvaluationFromJson(responseJson);

                if (parsedResponse != null) {
                    mainHandler.post(() -> callback.onSuccess(parsedResponse));
                    return;
                }

                throw new Exception("Gagal mengurai respons evaluasi AInterview.");
            } catch (Exception e) {
                Log.e(TAG, "Gagal mengevaluasi via AInterview: " + e.getMessage(), e);
                // Fallback cerdas jika koneksi gagal
                InterviewQuestion fallbackQ = new InterviewQuestion(1, category, difficulty, language, question, "");
                InterviewItem heuristic = com.example.interviewai.workflow.InterviewWorkflowManager.evaluateAnswer(fallbackQ, answer, language);

                EvaluationResponse fallbackResponse = new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                        .overallScore(heuristic.getScore())
                        .strengths("• " + heuristic.getStrength())
                        .weaknesses("• " + heuristic.getImprovement())
                        .improvements("• Perjelas konteks proyek dan peran spesifik Anda.\n• Sebutkan hasil kuantitatif atau dampak yang dicapai.")
                        .modelAnswer(heuristic.getSampleAnswer())
                        .scoreRationale("Skor estimasi heuristik (Offline/Fallback).")
                        .build();

                mainHandler.post(() -> callback.onSuccess(fallbackResponse));
            }
        });
    }

    /**
     * Memanggil REST API Gemini via HttpURLConnection
     */
    private static String callGeminiApi(String promptText, boolean jsonMimeType) throws Exception {
        URL url = new URL(ENDPOINT_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(25000);
        conn.setReadTimeout(35000);
        conn.setDoOutput(true);

        JSONObject root = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", promptText);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        root.put("contents", contents);

        if (jsonMimeType) {
            JSONObject genConfig = new JSONObject();
            genConfig.put("responseMimeType", "application/json");
            root.put("generationConfig", genConfig);
        }

        String payload = root.toString();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("HTTP error code " + responseCode + ": " + sb.toString());
        }

        JSONObject resObj = new JSONObject(sb.toString());
        JSONArray candidates = resObj.optJSONArray("candidates");
        if (candidates != null && candidates.length() > 0) {
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject candContent = candidate.optJSONObject("content");
            if (candContent != null) {
                JSONArray candParts = candContent.optJSONArray("parts");
                if (candParts != null && candParts.length() > 0) {
                    return candParts.getJSONObject(0).optString("text", "");
                }
            }
        }

        throw new Exception("Format respons Gemini tidak memiliki teks candidate: " + sb.toString());
    }

    private static List<InterviewQuestion> parseQuestionsFromJson(String rawJson, String category, String difficulty, String language) {
        List<InterviewQuestion> list = new ArrayList<>();
        if (rawJson == null || rawJson.trim().isEmpty()) return list;

        String cleanJson = rawJson.trim();
        // Bersihkan formatting markdown jika AI masih menyertakan ```json
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        try {
            JSONArray arr = new JSONArray(cleanJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id = obj.optInt("id", i + 1);
                String qText = obj.optString("question", "");
                String tip = obj.optString("tip", "");
                if (!qText.isEmpty()) {
                    list.add(new InterviewQuestion(id, category, difficulty, language, qText, tip));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Gagal mengurai JSON array pertanyaan: " + e.getMessage(), e);
        }
        return list;
    }

    private static EvaluationResponse parseEvaluationFromJson(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) return null;

        String cleanJson = rawJson.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        try {
            JSONObject obj = new JSONObject(cleanJson);
            int score = obj.optInt("score", 70);
            if (score < 1) score = 1;
            if (score > 100) score = 100;

            String strengths = obj.optString("strengths", "");
            String weaknesses = obj.optString("weaknesses", "");
            String improvement = obj.optString("improvement", "");
            String idealAnswer = obj.optString("ideal_answer", "");

            return new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                    .overallScore(score)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .improvements(improvement)
                    .modelAnswer(idealAnswer)
                    .scoreRationale("Skor dievaluasi berdasarkan relevansi, panjang jawaban, dan kedalaman materi oleh Gemini AI.")
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Gagal mengurai JSON evaluasi: " + e.getMessage(), e);
            return null;
        }
    }
}
