package com.example.interviewai;

import com.example.interviewai.ai.AnswerValidator;
import com.example.interviewai.model.InterviewItem;
import com.example.interviewai.model.InterviewQuestion;
import com.example.interviewai.workflow.InterviewWorkflowManager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test untuk validasi dan evaluasi jawaban wawancara:
 * Menguji deteksi gibberish ("Kk", "Jwnri2nwoq"), penanganan jawaban kosong,
 * jawaban "tidak tahu", jawaban relevan substantif, dan peniadaan kelebihan palsu.
 */
public class EvaluationValidationUnitTest {

    @Test
    public void testGibberish_shortNonWord_Kk() {
        AnswerValidator.ValidationResult result = AnswerValidator.validateAnswer("Kk");
        assertTrue("Kk harus terdeteksi sebagai gibberish", result.isGibberish());
        assertFalse("Kk bukan jawaban kosong", result.isEmpty());
    }

    @Test
    public void testGibberish_mixedAlphanumeric_Jwnri2nwoq() {
        AnswerValidator.ValidationResult result = AnswerValidator.validateAnswer("Jwnri2nwoq");
        assertTrue("Jwnri2nwoq harus terdeteksi sebagai gibberish", result.isGibberish());
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGibberish_repeatedCharacters() {
        AnswerValidator.ValidationResult result1 = AnswerValidator.validateAnswer(".....");
        assertTrue("..... harus terdeteksi sebagai gibberish", result1.isGibberish());

        AnswerValidator.ValidationResult result2 = AnswerValidator.validateAnswer("aaaaaa");
        assertTrue("aaaaaa harus terdeteksi sebagai gibberish", result2.isGibberish());

        AnswerValidator.ValidationResult result3 = AnswerValidator.validateAnswer("????");
        assertTrue("???? harus terdeteksi sebagai gibberish", result3.isGibberish());
    }

    @Test
    public void testGibberish_keyboardSmash() {
        AnswerValidator.ValidationResult result1 = AnswerValidator.validateAnswer("asdfghjkl");
        assertTrue("asdfghjkl harus terdeteksi sebagai gibberish", result1.isGibberish());

        AnswerValidator.ValidationResult result2 = AnswerValidator.validateAnswer("qwertyuiop");
        assertTrue("qwertyuiop harus terdeteksi sebagai gibberish", result2.isGibberish());
    }

    @Test
    public void testGibberish_symbolsOnly() {
        AnswerValidator.ValidationResult result = AnswerValidator.validateAnswer("!@#$%^&*()");
        assertTrue("Karakter simbol harus terdeteksi sebagai gibberish", result.isGibberish());
    }

    @Test
    public void testEmptyAnswer() {
        AnswerValidator.ValidationResult resultEmpty = AnswerValidator.validateAnswer("");
        assertTrue("String kosong harus terdeteksi isEmpty", resultEmpty.isEmpty());
        assertFalse(resultEmpty.isGibberish());

        AnswerValidator.ValidationResult resultSpaces = AnswerValidator.validateAnswer("   ");
        assertTrue("Spasi kosong harus terdeteksi isEmpty", resultSpaces.isEmpty());

        AnswerValidator.ValidationResult resultNull = AnswerValidator.validateAnswer(null);
        assertTrue("Null harus terdeteksi isEmpty", resultNull.isEmpty());
    }

    @Test
    public void testValidShortWords_notGibberish() {
        // Kata-kata sah pendek seperti "ya", "ok", "siap", "bisa", "tidak" tidak boleh dideteksi sebagai gibberish
        assertFalse(AnswerValidator.validateAnswer("ya").isGibberish());
        assertFalse(AnswerValidator.validateAnswer("ok").isGibberish());
        assertFalse(AnswerValidator.validateAnswer("siap").isGibberish());
        assertFalse(AnswerValidator.validateAnswer("bisa").isGibberish());
        assertFalse(AnswerValidator.validateAnswer("tidak").isGibberish());
    }

    @Test
    public void testMeaningfulAnswer_notGibberish() {
        String answer = "Saya akan mendengarkan pendapat rekan kerja terlebih dahulu, kemudian mendiskusikan perbedaan tersebut untuk mencari solusi bersama.";
        AnswerValidator.ValidationResult result = AnswerValidator.validateAnswer(answer);
        assertFalse("Jawaban bermakna tidak boleh terdeteksi sebagai gibberish", result.isGibberish());
        assertFalse("Jawaban bermakna tidak boleh kosong", result.isEmpty());
    }

    @Test
    public void testWorkflowEvaluation_Kk_getsScoreZeroAndNoFalseStrengths() {
        InterviewQuestion q = new InterviewQuestion(1, "IT", "Medium", "Indonesian",
                "Bagaimana Anda menyelesaikan konflik dalam tim kerja?", "Gunakan metode STAR.");
        InterviewItem item = InterviewWorkflowManager.evaluateAnswer(q, "Kk", "Indonesian");

        assertEquals("Jawaban 'Kk' harus mendapatkan skor 0", 0, item.getScore());
        assertEquals("Strengths harus 'Belum ada kelebihan yang dapat dinilai.'",
                "Belum ada kelebihan yang dapat dinilai.", item.getStrength());
        assertTrue("Weakness harus menjelaskan bahwa jawaban tidak bermakna atau karakter acak",
                item.getWeakness().toLowerCase().contains("karakter acak") ||
                item.getWeakness().toLowerCase().contains("tidak bermakna"));
    }

    @Test
    public void testWorkflowEvaluation_Jwnri2nwoq_getsScoreZeroAndNoFalseStrengths() {
        InterviewQuestion q = new InterviewQuestion(1, "IT", "Medium", "Indonesian",
                "Ceritakan pengalaman Anda memimpin proyek.", "Fokus pada kepemimpinan.");
        InterviewItem item = InterviewWorkflowManager.evaluateAnswer(q, "Jwnri2nwoq", "Indonesian");

        assertEquals("Jawaban 'Jwnri2nwoq' harus mendapatkan skor 0", 0, item.getScore());
        assertEquals("Strengths harus 'Belum ada kelebihan yang dapat dinilai.'",
                "Belum ada kelebihan yang dapat dinilai.", item.getStrength());
    }

    @Test
    public void testWorkflowEvaluation_emptyAnswer_getsScoreZero() {
        InterviewQuestion q = new InterviewQuestion(1, "IT", "Medium", "Indonesian",
                "Apa kelebihan terbesar Anda?", "Berikan contoh konkret.");
        InterviewItem item = InterviewWorkflowManager.evaluateAnswer(q, "", "Indonesian");

        assertEquals("Jawaban kosong harus mendapatkan skor 0", 0, item.getScore());
        assertEquals("Belum ada kelebihan yang dapat dinilai.", item.getStrength());
    }

    @Test
    public void testWorkflowEvaluation_tidakTahu_getsLowScoreAndExplainsWeakness() {
        InterviewQuestion q = new InterviewQuestion(1, "IT", "Medium", "Indonesian",
                "Bagaimana cara Anda mengoptimalkan query database?", "Sebutkan indexing.");
        InterviewItem item = InterviewWorkflowManager.evaluateAnswer(q, "Saya tidak tahu", "Indonesian");

        assertTrue("Skor 'Saya tidak tahu' harus rendah (skala 0-20)", item.getScore() <= 20);
        assertEquals("Belum ada kelebihan yang dapat dinilai.", item.getStrength());
        assertTrue("Weakness harus menjelaskan bahwa kandidat tidak mengetahui topik",
                item.getWeakness().toLowerCase().contains("belum mengetahui") ||
                item.getWeakness().toLowerCase().contains("tidak tahu"));
    }

    @Test
    public void testWorkflowEvaluation_substantiveAnswer_getsGoodScore() {
        InterviewQuestion q = new InterviewQuestion(1, "IT", "Medium", "Indonesian",
                "Bagaimana Anda menyelesaikan konflik dalam tim kerja?", "Gunakan metode STAR.");
        String answer = "Saya akan mendengarkan pendapat rekan kerja terlebih dahulu, kemudian mendiskusikan perbedaan tersebut untuk mencari solusi bersama.";
        InterviewItem item = InterviewWorkflowManager.evaluateAnswer(q, answer, "Indonesian");

        assertTrue("Jawaban terstruktur harus mendapatkan skor yang baik (>= 70)", item.getScore() >= 70);
        assertNotEquals("Jawaban bagus harus memiliki kelebihan yang nyata",
                "Belum ada kelebihan yang dapat dinilai.", item.getStrength());
    }
}
