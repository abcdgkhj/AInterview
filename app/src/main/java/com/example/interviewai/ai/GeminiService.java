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
    public static final String MODEL_NAME = "gemini-flash-lite-latest";

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
        final String safeDifficulty = (difficulty != null && !difficulty.isEmpty()) ? QuestionBank.normalizeDifficulty(difficulty) : QuestionBank.DIFFICULTY_SEDANG;
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
                        || safeCategory.toLowerCase().contains("human resources")
                        || safeCategory.toLowerCase().contains("hr")
                        || safeCategory.toLowerCase().contains("finance")
                        || safeCategory.toLowerCase().contains("business management")
                        || safeCategory.toLowerCase().contains("sales")
                        || safeCategory.toLowerCase().contains("graphic design")
                        || safeCategory.toLowerCase().contains("data analytics")
                        || safeCategory.toLowerCase().contains("customer service")
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
                promptBuilder.append("- Tingkat Kesulitan: ").append(safeDifficulty);
                if (QuestionBank.DIFFICULTY_MUDAH.equalsIgnoreCase(safeDifficulty)) {
                    promptBuilder.append(" (Tingkat Mudah / Dasar: Buat pertanyaan tingkat dasar, fundamental, pengenalan konsep, tidak berbelit-belit, dan ramah untuk level pemula/fresh graduate)\n");
                } else if (QuestionBank.DIFFICULTY_SULIT.equalsIgnoreCase(safeDifficulty)) {
                    promptBuilder.append(" (Tingkat Sulit / Lanjut: Buat pertanyaan mendalam, kompleks, studi kasus pemecahan masalah kritis, penanganan skenario sulit/edge cases, arsitektur, dan problem solving tingkat senior)\n");
                } else {
                    promptBuilder.append(" (Tingkat Sedang / Menengah: Buat pertanyaan tingkat menengah, penerapan praktis di lingkungan kerja nyata, studi kasus realistis standar industri)\n");
                }
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

                // Error jika parsing menghasilkan daftar kosong
                throw new Exception("Hasil parsing pertanyaan dari AInterview kosong.");
            } catch (Exception e) {
                Log.e(TAG, "Gagal meng-generate pertanyaan via AInterview: " + e.getMessage(), e);
                // Gemini AI adalah satu-satunya sumber pertanyaan; jangan gunakan bank pertanyaan lokal sebagai fallback
                mainHandler.post(() -> callback.onError("Gagal menghasilkan pertanyaan dari AI: " + e.getMessage(), null));
            }
        });
    }

    /**
     * Evaluasi jawaban user menggunakan AInterview AI:
     * - Evaluasi relevansi terlebih dahulu sebelum memberikan skor
     * - Bedakan jawaban singkat relevan vs karakter acak / tidak bermakna
     * - Skor 0 untuk jawaban kosong atau karakter acak
     * - Skor rendah (10-25) untuk tidak tahu atau tidak relevan
     * - Skor 70-100 untuk jawaban baik dan relevan
     * - Tidak mengarang kelebihan jika tidak ditemukan (tampilkan "Belum ada kelebihan yang dapat dinilai")
     * - Kekurangan spesifik dan saran aplikatif
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

        // 1. Validasi di sisi aplikasi sebelum memanggil AI
        AnswerValidator.ValidationResult validation = AnswerValidator.validateAnswer(answer);

        // Kasus 1: Jawaban kosong
        if (validation.isEmpty()) {
            final String noEvalMessage = "Tidak ada data evaluasi karena pertanyaan tidak dijawab.";
            EvaluationResponse emptyResponse = new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                    .overallScore(0)
                    .strengths("Belum ada kelebihan yang dapat dinilai.")
                    .weaknesses("Pertanyaan tidak dijawab oleh kandidat.")
                    .improvements("Berikan jawaban yang relevan dan terstruktur sesuai dengan pertanyaan wawancara yang diajukan.")
                    .modelAnswer(noEvalMessage)
                    .scoreRationale("Skor 0: Pertanyaan tidak dijawab.")
                    .build();
            mainHandler.post(() -> callback.onSuccess(emptyResponse));
            return;
        }

        // Kasus 2: Jawaban karakter acak / tanpa makna (misal "Kk", "Jwnri2nwoq")
        if (validation.isGibberish()) {
            executor.execute(() -> {
                String modelAnswer = generateIdealAnswerFallback(question, category, difficulty, language);
                EvaluationResponse gibberishResponse = new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                        .overallScore(0)
                        .strengths("Belum ada kelebihan yang dapat dinilai.")
                        .weaknesses("Jawaban berupa rangkaian karakter acak atau tidak bermakna ('" + answer.trim() + "') sehingga tidak ada substansi materi yang dapat dinilai.")
                        .improvements("Jawablah pertanyaan secara jelas dan terstruktur dengan menyampaikan konsep, pengalaman nyata, atau solusi konkret yang relevan.")
                        .modelAnswer(modelAnswer)
                        .scoreRationale("Skor 0: Karakter acak atau tanpa makna.")
                        .build();
                mainHandler.post(() -> callback.onSuccess(gibberishResponse));
            });
            return;
        }

        // Kasus 3: Jawaban teks valid -> Panggil AI Gemini dengan standar evaluasi ketat
        executor.execute(() -> {
            try {
                int wordCount = answer.trim().split("\\s+").length;

                StringBuilder prompt = new StringBuilder();
                prompt.append("Anda adalah seorang penilai wawancara kerja profesional dan Hiring Manager ahli berbasis AI.\n");
                prompt.append("Tugas Anda: Evaluasi jawaban kandidat wawancara kerja berikut secara objektif, ketat, dan adil.\n\n");
                prompt.append("Pertanyaan: ").append(question).append("\n");
                prompt.append("Kategori / Bidang: ").append(category).append("\n");
                String safeEvalDifficulty = QuestionBank.normalizeDifficulty(difficulty);
                prompt.append("Tingkat Kesulitan: ").append(safeEvalDifficulty).append("\n");
                prompt.append("Bahasa: ").append(language).append("\n");
                prompt.append("Jawaban Kandidat: \"").append(answer).append("\"\n");
                prompt.append("Jumlah Kata Jawaban: ").append(wordCount).append(" kata\n\n");

                prompt.append("STANDAR DAN ATURAN EVALUASI WAJIB (SANGAT KETAT):\n");
                prompt.append("1. EVALUASI RELEVANSI TERLEBIH DAHULU SEBELUM MEMBERIKAN SKOR:\n");
                prompt.append("   - Periksa apakah jawaban benar-benar menjawab esensi dari pertanyaan yang diajukan.\n");
                prompt.append("   - Jangan menganggap jawaban yang panjang selalu bagus. Jika jawaban panjang tetapi keluar dari topik pertanyaan / tidak relevan, WAJIB berikan skor rendah (10–25).\n");
                prompt.append("   - Jika jawaban berupa pengakuan tidak tahu atau mengelak (misal 'Saya tidak tahu', 'Belum pernah belajar', 'Gak tau'), berikan skor rendah (10–20).\n");
                prompt.append("   - Jika jawaban singkat TETAPI relevan dan tepat sasaran (misal menjawab konsep inti dengan benar secara to-the-point), BERIKAN NILAI SECARA OBJEKTIF berdasarkan kualitas substansinya (misal 70–85), BUKAN otomatis diberi skor 0.\n");
                prompt.append("   - Jika jawaban berupa karakter acak, spam, atau tanpa makna, berikan skor 0.\n\n");

                prompt.append("2. SKALA STANDAR SKOR (0 - 100):\n");
                prompt.append("   - 90–100: Sangat baik, sangat relevan, lengkap, terstruktur, dan menunjukkan pemahaman mendalam.\n");
                prompt.append("   - 70–89: Baik dan relevan, menjawab inti pertanyaan dengan tepat, namun masih memiliki sedikit ruang elaborasi.\n");
                prompt.append("   - 50–69: Cukup relevan, memahami konsep dasar namun penjelasan kurang mendalam atau masih terlalu umum.\n");
                prompt.append("   - 30–49: Sedikit relevan, tetapi penjelasan sangat minim, dangkal, dan tidak memadai.\n");
                prompt.append("   - 10–25: Hampir tidak menjawab inti pertanyaan, tidak relevan, atau menyatakan tidak tahu.\n");
                prompt.append("   - 0: Jawaban kosong, karakter acak, atau sama sekali tidak memiliki makna.\n\n");

                prompt.append("3. ATURAN KELEBIHAN (STRENGTHS) - DILARANG MENGARANG:\n");
                prompt.append("   - DILARANG KERAS memuji kandidat hanya karena dia mengisi jawaban, merespons, atau mengikuti wawancara!\n");
                prompt.append("   - DILARANG mengarang kelebihan yang tidak benar-benar terlihat dari isi jawaban.\n");
                prompt.append("   - Jika jawaban tidak relevan, menyatakan tidak tahu, atau bernilai rendah (< 40), bagian 'strengths' WAJIB diisi:\n");
                prompt.append("     \"Belum ada kelebihan yang dapat dinilai.\"\n");
                prompt.append("   - Hanya tuliskan kelebihan jika jawaban benar-benar memiliki substansi pengetahuan, metodologi, atau sikap kerja yang positif.\n\n");

                prompt.append("4. ATURAN KEKURANGAN (WEAKNESSES):\n");
                prompt.append("   - Jelaskan secara spesifik mengapa jawaban tidak memenuhi kriteria atau aspek apa yang belum terjawab.\n\n");

                prompt.append("5. ATURAN SARAN PERBAIKAN (IMPROVEMENT):\n");
                prompt.append("   - Berikan perbaikan konkret yang sesuai dengan pertanyaan dan kekurangan jawaban kandidat (misal metode STAR, penambahan metrik, atau pemahaman konsep teknis terkait).\n\n");

                prompt.append("6. REKOMENDASI JAWABAN IDEAL (IDEAL_ANSWER):\n");
                prompt.append("   - Berikan contoh kalimat jawaban yang ideal, terstruktur, dan profesional untuk pertanyaan tersebut.\n\n");

                prompt.append("7. ABAIKAN TYPO KECIL:\n");
                prompt.append("   - Abaikan salah eja kecil atau typo ringan jika maknanya tetap dapat dipahami secara substansi.\n\n");

                prompt.append("FORMAT OUTPUT WAJIB: Hanya keluarkan format JSON Object murni tanpa tanda kutip markdown:\n");
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
                mainHandler.post(() -> callback.onFailure("Koneksi AInterview terputus atau gagal: " + e.getMessage()));
            }
        });
    }

    /**
     * Menghasilkan rekomendasi jawaban ideal jika jawaban pengguna tidak valid / gibberish.
     */
    private static String generateIdealAnswerFallback(String question, String category, String difficulty, String language) {
        try {
            String prompt = "Anda adalah pewawancara profesional. Berikan SATU contoh rekomendasi jawaban ideal, terstruktur, dan profesional untuk pertanyaan wawancara berikut:\n"
                    + "Pertanyaan: " + question + "\n"
                    + "Bidang: " + category + "\n"
                    + "Tingkat Kesulitan: " + difficulty + "\n"
                    + "Bahasa: " + language + "\n"
                    + "Berikan langsung teks jawaban ideal tanpa pembuka atau penutup.";
            String res = callGeminiApi(prompt, false);
            if (res != null && !res.trim().isEmpty()) {
                return res.trim();
            }
        } catch (Exception ignored) {
        }
        return "Contoh jawaban ideal: Berikan jawaban yang terstruktur dengan metode STAR (Situation, Task, Action, Result) serta jelaskan langkah konkret yang Anda ambil secara profesional.";
    }

    /**
     * Memanggil REST API Gemini via HttpURLConnection dengan auto-failover / key rotation
     */
    private static String callGeminiApi(String promptText, boolean jsonMimeType) throws Exception {
        int maxAttempts = Math.max(1, GeminiApiKey.getKeyCount());
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String activeKey = GeminiApiKey.getActiveKey();
            int keyIndex = GeminiApiKey.getCurrentKeyIndex();
            String endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL_NAME + ":generateContent?key=" + activeKey;

            HttpURLConnection conn = null;
            try {
                URL url = new URL(endpointUrl);
                conn = (HttpURLConnection) url.openConnection();
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

                // Deteksi Rate Limit (HTTP 429) atau Quota Exceeded (HTTP 403)
                if (responseCode == 429 || (responseCode == 403 && sb.toString().contains("RESOURCE_EXHAUSTED"))) {
                    Log.w(TAG, "API Key #" + keyIndex + " terkena kuota limit (HTTP " + responseCode + "). Merotasi ke API key berikutnya...");
                    GeminiApiKey.rotateToNextKey();
                    lastException = new Exception("API Key #" + keyIndex + " habis kuota: " + sb.toString());
                    continue; // Coba lagi dengan key berikutnya
                }

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

                throw new Exception("Format respons AI tidak memiliki teks candidate: " + sb.toString());
            } catch (Exception e) {
                lastException = e;
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    Log.w(TAG, "Mendeteksi error 429 pada key #" + keyIndex + ". Merotasi key...");
                    GeminiApiKey.rotateToNextKey();
                } else if (attempt < maxAttempts && (e instanceof java.io.IOException)) {
                    Log.w(TAG, "Koneksi gagal pada key #" + keyIndex + ". Mencoba key lain...");
                    GeminiApiKey.rotateToNextKey();
                } else {
                    throw e;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        throw (lastException != null) ? lastException : new Exception("Semua API key telah mencapai kuota atau gagal dihubungi.");
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

    static EvaluationResponse parseEvaluationFromJson(String rawJson) {
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
            if (score < 0) score = 0;
            if (score > 100) score = 100;

            String strengths = obj.optString("strengths", "");
            String weaknesses = obj.optString("weaknesses", "");
            String improvement = obj.optString("improvement", "");
            String idealAnswer = obj.optString("ideal_answer", "");

            // Sanitasi kelebihan jika skor rendah (< 35) atau memuji partisipasi/usaha menjawab
            String lowerStrengths = strengths.toLowerCase(java.util.Locale.ROOT);
            if (score < 35 || strengths.trim().isEmpty() || strengths.trim().equals("-")
                    || lowerStrengths.contains("berpartisipasi")
                    || lowerStrengths.contains("merespons")
                    || lowerStrengths.contains("mencoba menjawab")
                    || lowerStrengths.contains("mengisi jawaban")
                    || lowerStrengths.contains("meluangkan waktu")
                    || lowerStrengths.contains("mengikuti wawancara")
                    || lowerStrengths.contains("berusaha memberikan")) {
                strengths = "Belum ada kelebihan yang dapat dinilai.";
            }

            // Sanitasi kekurangan jika kosong
            if (weaknesses.trim().isEmpty() || weaknesses.trim().equals("-")) {
                if (score == 0) {
                    weaknesses = "Jawaban tidak memiliki substansi atau makna yang dapat dinilai.";
                } else if (score < 50) {
                    weaknesses = "Jawaban belum menjawab inti pertanyaan secara memadai.";
                } else {
                    weaknesses = "Jawaban dapat lebih diperdalam dengan rincian contoh praktis.";
                }
            }

            // Sanitasi saran perbaikan jika kosong
            if (improvement.trim().isEmpty() || improvement.trim().equals("-")) {
                improvement = "Pelajari konsep terkait dan gunakan struktur STAR untuk memaparkan jawaban secara komprehensif.";
            }

            return new EvaluationResponse.Builder(EvaluationStatus.COMPLETED)
                    .overallScore(score)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .improvements(improvement)
                    .modelAnswer(idealAnswer)
                    .scoreRationale("Skor dievaluasi berdasarkan relevansi, kedalaman substansi, dan kelengkapan materi oleh AInterview.")
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Gagal mengurai JSON evaluasi: " + e.getMessage(), e);
            return null;
        }
    }
}
