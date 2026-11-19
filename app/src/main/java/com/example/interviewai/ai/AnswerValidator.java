package com.example.interviewai.ai;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator kualitas jawaban sebelum diproses oleh AI.
 * Mendeteksi jawaban kosong, karakter acak (gibberish), spam, dan ketikan tanpa makna.
 */
public class AnswerValidator {

    // Kumpulan kata sah sangat pendek dalam Bahasa Indonesia & Inggris
    private static final Set<String> VALID_SHORT_WORDS = new HashSet<>(Arrays.asList(
            "ya", "ok", "oke", "ada", "mau", "iya", "siap", "bisa", "tahu", "tau", "baik", "yes", "no"
    ));

    // Pola angka bercampur di tengah huruf acak (misal "jwnri2nwoq", "abc123xyz")
    private static final Pattern MIXED_ALPHANUMERIC = Pattern.compile("^[a-zA-Z]+[0-9]+[a-zA-Z0-9]*|[0-9]+[a-zA-Z]+[a-zA-Z0-9]*$");

    // Pola karakter berulang (misal "aaaaaa", "kkkkkk", "zzzzzz", "......")
    private static final Pattern REPEATED_CHARS = Pattern.compile("^(.)\\1{2,}$");

    // Pola keyboard smash umum
    private static final Set<String> KEYBOARD_SMASH = new HashSet<>(Arrays.asList(
            "asdf", "asdfgh", "asdfghjkl", "qwerty", "qwertyuiop", "zxcv", "zxcvbn", "zxcvbnm",
            "qwer", "hjkl", "yuio", "ghjk", "fghj", "dfgh"
    ));

    public static class ValidationResult {
        private final boolean isGibberish;
        private final boolean isEmpty;
        private final String reason;

        public ValidationResult(boolean isGibberish, boolean isEmpty, String reason) {
            this.isGibberish = isGibberish;
            this.isEmpty = isEmpty;
            this.reason = reason;
        }

        public boolean isGibberish() {
            return isGibberish;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * Memeriksa apakah teks jawaban merupakan karakter acak, spam, atau tanpa makna.
     */
    public static ValidationResult validateAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return new ValidationResult(false, true, "Pertanyaan tidak dijawab.");
        }

        String trimmed = answer.trim();

        // 1. Periksa karakter berulang mendominasi (misal "...", "???", "aaaa", "kkkk")
        if (REPEATED_CHARS.matcher(trimmed.toLowerCase(Locale.ROOT)).matches()) {
            return new ValidationResult(true, false, "Karakter berulang tanpa makna.");
        }

        // 2. Bersihkan tanda baca untuk analisa kata
        String cleaned = trimmed.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
        if (cleaned.isEmpty()) {
            // Hanya berisi simbol / tanda baca acak (misal "@#$%^&*")
            return new ValidationResult(true, false, "Hanya berisi simbol atau tanda baca.");
        }

        String[] words = cleaned.split("\\s+");

        // 3. Jika hanya terdiri dari 1 kata:
        if (words.length == 1) {
            String word = words[0].toLowerCase(Locale.ROOT);

            // Periksa jika mengandung angka bercampur huruf (misal "jwnri2nwoq")
            if (MIXED_ALPHANUMERIC.matcher(word).matches()) {
                return new ValidationResult(true, false, "Kombinasi huruf dan angka acak.");
            }

            // Periksa keyboard smash
            if (KEYBOARD_SMASH.contains(word)) {
                return new ValidationResult(true, false, "Rangkaian karakter keyboard acak.");
            }

            // Periksa apakah tidak ada huruf vokal sama sekali (misal "kk", "zxcvb", "bcdfgh")
            boolean hasVowel = word.matches(".*[aeiouy].*");
            if (!hasVowel && word.length() >= 2) {
                return new ValidationResult(true, false, "Rangkaian konsonan acak tanpa vokal.");
            }

            // Kata sangat pendek (<= 2 karakter) yang bukan kata sah (misal "kk", "aa", "gg", "zz")
            if (word.length() <= 2 && !VALID_SHORT_WORDS.contains(word)) {
                return new ValidationResult(true, false, "Karakter acak terlalu singkat.");
            }

            // Kata dengan rasio vokal sangat rendah pada kata panjang (misal "jwnrnwq" -> 7 huruf tanpa vokal bermakna)
            if (word.length() >= 5) {
                int vowels = 0;
                for (char c : word.toCharArray()) {
                    if ("aeiouy".indexOf(c) >= 0) vowels++;
                }
                if (vowels <= 1 && word.length() >= 6) {
                    return new ValidationResult(true, false, "Rangkaian karakter tidak bermakna.");
                }
            }
        } else {
            // 4. Jika ada beberapa kata, periksa apakah SEMUA kata berupa gibberish (misal "asdf qwer zxcv")
            int gibberishWords = 0;
            for (String w : words) {
                String lw = w.toLowerCase(Locale.ROOT);
                boolean hasVowel = lw.matches(".*[aeiouy].*");
                if (!hasVowel && lw.length() >= 2) {
                    gibberishWords++;
                } else if (KEYBOARD_SMASH.contains(lw)) {
                    gibberishWords++;
                } else if (MIXED_ALPHANUMERIC.matcher(lw).matches()) {
                    gibberishWords++;
                } else if (REPEATED_CHARS.matcher(lw).matches()) {
                    gibberishWords++;
                }
            }
            if (gibberishWords == words.length) {
                return new ValidationResult(true, false, "Seluruh kata berupa karakter acak.");
            }
        }

        return new ValidationResult(false, false, "Valid");
    }
}
