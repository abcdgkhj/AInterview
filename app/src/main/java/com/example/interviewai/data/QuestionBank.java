package com.example.interviewai.data;

import com.example.interviewai.model.InterviewQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bank Pertanyaan Lokal untuk Simulasi Wawancara InterviewAI.
 * Menyediakan bank soal terstruktur berdasarkan Kategori, Tingkat Kesulitan, dan Bahasa.
 */
public class QuestionBank {

    // 2 Kategori Wawancara Utama
    public static final String CATEGORY_UMUM = "Interview Umum";
    public static final String CATEGORY_TEKNIS = "Interview Teknis";
    public static final String CATEGORY_TEKNIS_IT = "Interview Teknis - IT";
    public static final String CATEGORY_TEKNIS_MARKETING = "Interview Teknis - Marketing";
    public static final String CATEGORY_TEKNIS_ACCOUNTING = "Interview Teknis - Accounting";

    // Legacy Kategori untuk backward compatibility
    public static final String CATEGORY_GENERAL = "General Interview";
    public static final String CATEGORY_SOFTWARE = "Software Engineering";
    public static final String CATEGORY_WEB = "Web Development";
    public static final String CATEGORY_DATABASE = "Database & SQL";
    public static final String CATEGORY_BEHAVIORAL = "Behavioral Interview";

    // Legacy role aliases untuk backward compatibility
    public static final String ROLE_GENERAL = "General Interview";
    public static final String ROLE_ANDROID = "Software Engineering";
    public static final String ROLE_BACKEND = "Database & SQL";
    public static final String ROLE_FRONTEND = "Web Development";
    public static final String ROLE_PRODUCT = "Behavioral Interview";

    // Tingkat Kesulitan
    public static final String DIFFICULTY_BEGINNER = "Beginner";
    public static final String DIFFICULTY_INTERMEDIATE = "Intermediate";
    public static final String DIFFICULTY_ADVANCED = "Advanced";

    // Bahasa Wawancara
    public static final String LANG_ID = "Indonesian";
    public static final String LANG_EN = "English";

    public static String getTechnicalCategoryForField(String field) {
        if ("Marketing".equalsIgnoreCase(field)) {
            return CATEGORY_TEKNIS_MARKETING;
        } else if ("Accounting".equalsIgnoreCase(field)) {
            return CATEGORY_TEKNIS_ACCOUNTING;
        } else {
            return CATEGORY_TEKNIS_IT;
        }
    }

    public static List<String> getAvailableCategories() {
        return Arrays.asList(
                CATEGORY_UMUM,
                CATEGORY_TEKNIS_IT,
                CATEGORY_TEKNIS_MARKETING,
                CATEGORY_TEKNIS_ACCOUNTING,
                CATEGORY_GENERAL,
                CATEGORY_SOFTWARE,
                CATEGORY_WEB,
                CATEGORY_DATABASE,
                CATEGORY_BEHAVIORAL
        );
    }

    public static List<String> getAvailableDifficulties() {
        return Arrays.asList(
                DIFFICULTY_BEGINNER,
                DIFFICULTY_INTERMEDIATE,
                DIFFICULTY_ADVANCED
        );
    }

    public static List<String> getAvailableLanguages() {
        return Arrays.asList(
                LANG_ID,
                LANG_EN
        );
    }

    public static List<String> getAvailableRoles() {
        return getAvailableCategories();
    }

    public static List<InterviewQuestion> getQuestionsForRole(String role) {
        return getFilteredQuestions(role, DIFFICULTY_INTERMEDIATE, LANG_ID, 5);
    }

    /**
     * Mengambil daftar pertanyaan lokal yang disaring sesuai preferensi user.
     * Menerapkan fallback hierarkis jika jumlah pertanyaan pada kesulitan tertentu kurang dari yang diminta.
     */
    public static List<InterviewQuestion> getFilteredQuestions(String category, String difficulty, String language, int count) {
        List<InterviewQuestion> masterList = getAllQuestions();
        List<InterviewQuestion> filtered = new ArrayList<>();

        String targetCategory = (category != null && !category.isEmpty()) ? category : CATEGORY_GENERAL;
        String targetDifficulty = (difficulty != null && !difficulty.isEmpty()) ? difficulty : DIFFICULTY_INTERMEDIATE;
        String targetLanguage = (language != null && !language.isEmpty()) ? language : LANG_ID;

        // 1. Prioritas 1: Kategori sama, Bahasa sama, Kesulitan sama persis
        for (InterviewQuestion q : masterList) {
            if (matchesCategory(q.getCategory(), targetCategory)
                    && q.getLanguage().equalsIgnoreCase(targetLanguage)
                    && q.getDifficulty().equalsIgnoreCase(targetDifficulty)) {
                filtered.add(q);
            }
        }

        // 2. Prioritas 2: Kategori sama, Bahasa sama, tingkat kesulitan lain
        if (filtered.size() < count) {
            for (InterviewQuestion q : masterList) {
                if (matchesCategory(q.getCategory(), targetCategory)
                        && q.getLanguage().equalsIgnoreCase(targetLanguage)
                        && !containsQuestion(filtered, q.getId())) {
                    filtered.add(q);
                }
            }
        }

        // 3. Prioritas 3: Kategori sama, bahasa berbeda (jika masih kurang)
        if (filtered.size() < count) {
            for (InterviewQuestion q : masterList) {
                if (matchesCategory(q.getCategory(), targetCategory)
                        && !containsQuestion(filtered, q.getId())) {
                    filtered.add(q);
                }
            }
        }

        // 4. Fallback Terakhir: Pertanyaan umum lain dari master list
        if (filtered.size() < count) {
            for (InterviewQuestion q : masterList) {
                if (!containsQuestion(filtered, q.getId())) {
                    filtered.add(q);
                }
            }
        }

        // Potong sesuai batas yang diminta
        int max = Math.min(count, filtered.size());
        List<InterviewQuestion> sliced = new ArrayList<>(filtered.subList(0, max));

        // Format ulang ID berurutan 1..N untuk kemudahan penomoran sesi
        List<InterviewQuestion> sessionQuestions = new ArrayList<>();
        for (int i = 0; i < sliced.size(); i++) {
            InterviewQuestion q = sliced.get(i);
            sessionQuestions.add(new InterviewQuestion(
                    i + 1,
                    q.getCategory(),
                    q.getDifficulty(),
                    q.getLanguage(),
                    q.getQuestionText(),
                    q.getTip()
            ));
        }

        return sessionQuestions;
    }

    private static boolean containsQuestion(List<InterviewQuestion> list, int id) {
        for (InterviewQuestion item : list) {
            if (item.getId() == id) return true;
        }
        return false;
    }

    private static boolean matchesCategory(String qCategory, String targetCategory) {
        if (qCategory.equalsIgnoreCase(targetCategory)) return true;

        // Interview Umum: General Interview + Behavioral (STAR)
        if (targetCategory.equalsIgnoreCase(CATEGORY_UMUM) || targetCategory.contains("Umum")) {
            return qCategory.equalsIgnoreCase(CATEGORY_UMUM)
                    || qCategory.contains("General")
                    || qCategory.contains("Behavioral");
        }

        // Interview Teknis - IT: Software Engineering, Web Development, Database & SQL, IT
        if (targetCategory.equalsIgnoreCase(CATEGORY_TEKNIS_IT)
                || targetCategory.contains("- IT")
                || targetCategory.equals("IT")) {
            return qCategory.equalsIgnoreCase(CATEGORY_TEKNIS_IT)
                    || qCategory.contains("Software")
                    || qCategory.contains("Web")
                    || qCategory.contains("Database")
                    || qCategory.contains("IT");
        }

        // Interview Teknis - Marketing
        if (targetCategory.equalsIgnoreCase(CATEGORY_TEKNIS_MARKETING)
                || targetCategory.contains("Marketing")) {
            return qCategory.equalsIgnoreCase(CATEGORY_TEKNIS_MARKETING)
                    || qCategory.contains("Marketing");
        }

        // Interview Teknis - Accounting
        if (targetCategory.equalsIgnoreCase(CATEGORY_TEKNIS_ACCOUNTING)
                || targetCategory.contains("Accounting")) {
            return qCategory.equalsIgnoreCase(CATEGORY_TEKNIS_ACCOUNTING)
                    || qCategory.contains("Accounting");
        }

        if (targetCategory.contains("General") && qCategory.contains("General")) return true;
        if (targetCategory.contains("Software") && (qCategory.contains("Software") || qCategory.contains("Android"))) return true;
        if (targetCategory.contains("Web") && (qCategory.contains("Web") || qCategory.contains("Frontend"))) return true;
        if (targetCategory.contains("Database") && (qCategory.contains("Database") || qCategory.contains("Backend"))) return true;
        if (targetCategory.contains("Behavioral") && (qCategory.contains("Behavioral") || qCategory.contains("Product"))) return true;
        return false;
    }

    public static List<InterviewQuestion> getAllQuestions() {
        List<InterviewQuestion> list = new ArrayList<>();

        // ================= 1. GENERAL INTERVIEW (INDONESIAN) =================
        list.add(new InterviewQuestion(101, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Ceritakan tentang diri Anda, latar belakang pendidikan, dan minat profesional Anda.",
                "Fokus pada ringkasan perjalanan akademis/karir, proyek yang pernah Anda kerjakan, dan nilai yang ingin Anda bawa."));
        list.add(new InterviewQuestion(102, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa yang menjadi motivasi terbesar Anda melamar di posisi dan organisasi ini?",
                "Tunjukkan pemahaman terhadap visi misi perusahaan serta bagaimana posisi ini sejalan dengan target karir Anda."));
        list.add(new InterviewQuestion(103, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa saja kekuatan utama dan kelebihan pribadi yang paling mendukung produktivitas kerja Anda?",
                "Sebutkan 2-3 kekuatan spesifik dengan contoh nyata bagaimana kelebihan tersebut membantu menyelesaikan pekerjaan."));
        list.add(new InterviewQuestion(104, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Bagaimana cara Anda membagi waktu dan mengatur prioritas ketika memiliki beberapa tugas bersamaan?",
                "Jelaskan teknik manajemen waktu Anda, seperti matriks Eisenhower, to-do list terstruktur, atau kalender kerja."));
        list.add(new InterviewQuestion(105, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Sebutkan satu kelemahan terbesar Anda dan bagaimana langkah nyata Anda untuk mengatasinya.",
                "Jawab dengan jujur dan tunjukkan langkah perbaikan mandiri atau strategi yang sedang Anda terapkan."));
        list.add(new InterviewQuestion(106, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Di mana Anda melihat diri Anda secara profesional dalam jangka waktu 3 hingga 5 tahun ke depan?",
                "Tunjukkan aspirasi realistis mengenai peningkatan skill, kepemimpinan, dan kontribusi jangka panjang."));
        list.add(new InterviewQuestion(107, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana Anda menyikapi kritik atau masukan yang kurang menyenangkan terhadap hasil kerja Anda?",
                "Tekankan keterbukaan terhadap feedback konstruktif dan pemisahan antara urusan profesional dengan emosi pribadi."));
        list.add(new InterviewQuestion(108, CATEGORY_GENERAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Mengapa perusahaan kami harus memilih Anda dibandingkan kandidat-kandidat berkualitas lainnya?",
                "Tekankan kombinasi unik antara etos kerja, kecepatan adaptasi, dan kesiapan memberikan solusi nyata."));
        list.add(new InterviewQuestion(109, CATEGORY_GENERAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana pendekatan Anda dalam membangun lingkungan kerja yang inklusif dan produktif bersama tim?",
                "Ulas pentingnya komunikasi transparan, rasa saling menghargai, dan berbagi pengetahuan antar anggota tim."));
        list.add(new InterviewQuestion(110, CATEGORY_GENERAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Apa arti kesuksesan profesional bagi Anda dalam konteks peran yang Anda lamar ini?",
                "Hubungkan kesuksesan pribadi dengan tercapainya target tim, kepuasan pengguna, dan kualitas produk yang andal."));

        // ================= 1. GENERAL INTERVIEW (ENGLISH) =================
        list.add(new InterviewQuestion(151, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_EN,
                "Tell me about yourself, your educational background, and your key technical interests.",
                "Provide a structured overview covering your background, relevant projects, and career direction."));
        list.add(new InterviewQuestion(152, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_EN,
                "What motivated you to apply for this specific role and our organization?",
                "Demonstrate knowledge of company products and align your personal values with company mission."));
        list.add(new InterviewQuestion(153, CATEGORY_GENERAL, DIFFICULTY_BEGINNER, LANG_EN,
                "How do you manage deadlines when working on multiple concurrent tasks?",
                "Discuss prioritization frameworks, time-blocking, and clear communication with stakeholders."));
        list.add(new InterviewQuestion(154, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "What do you consider your greatest professional strength and how have you applied it?",
                "Give concrete examples of past achievements supported by this core strength."));
        list.add(new InterviewQuestion(155, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Describe an area where you feel you need improvement, and the steps you take to address it.",
                "Be authentic and discuss actionable habits or ongoing learning initiatives."));
        list.add(new InterviewQuestion(156, CATEGORY_GENERAL, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Where do you see yourself professionally in the next 3 to 5 years?",
                "Highlight continuous skill acquisition, ownership, and measurable organizational contributions."));
        list.add(new InterviewQuestion(157, CATEGORY_GENERAL, DIFFICULTY_ADVANCED, LANG_EN,
                "Why should our company hire you over other qualified candidates?",
                "Summarize your unique intersection of technical competencies, teamwork, and problem-solving drive."));
        list.add(new InterviewQuestion(158, CATEGORY_GENERAL, DIFFICULTY_ADVANCED, LANG_EN,
                "How do you approach receiving tough or critical feedback on your work?",
                "Highlight emotional maturity, active listening, and turning feedback into concrete action points."));

        // ================= 2. SOFTWARE ENGINEERING (INDONESIAN) =================
        list.add(new InterviewQuestion(201, CATEGORY_SOFTWARE, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan 4 prinsip dasar Object-Oriented Programming (OOP) dan contoh penerapannya dalam kode Anda.",
                "Ulas Encapsulation, Inheritance, Polymorphism, dan Abstraction dengan contoh fungsi nyata."));
        list.add(new InterviewQuestion(202, CATEGORY_SOFTWARE, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa perbedaan antara Synchronous dan Asynchronous programming, serta kapan masing-masing digunakan?",
                "Bahas blocking vs non-blocking I/O, threads, dan penanganan respon jaringan tanpa membekukan antarmuka."));
        list.add(new InterviewQuestion(203, CATEGORY_SOFTWARE, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan perbedaan mendasar antara Array, List, dan Map/Dictionary serta efisiensi pencariannya.",
                "Bahas kompleksitas waktu O(1) vs O(N), indexing, key-value lookup, dan penggunaan memori."));
        list.add(new InterviewQuestion(204, CATEGORY_SOFTWARE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana pendekatan Anda dalam menerapkan prinsip SOLID dan clean code pada proyek perangkat lunak?",
                "Jelaskan bagaimana Single Responsibility dan Dependency Inversion membuat kode mudah di-maintain dan di-test."));
        list.add(new InterviewQuestion(205, CATEGORY_SOFTWARE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Ceritakan pengalaman Anda saat mendebug bug kritis atau memory leak. Tools apa yang Anda gunakan?",
                "Jelaskan alur identifikasi: reproduksi masalah, analisa stacktrace/profiler, isolasi akar masalah, dan regression test."));
        list.add(new InterviewQuestion(206, CATEGORY_SOFTWARE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Apa manfaat penulisan Unit Test dan bagaimana strategi Anda dalam menentukan test case yang efektif?",
                "Bahas piramida pengujian, boundary conditions, edge cases, serta mock dependency."));
        list.add(new InterviewQuestion(207, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana Anda merancang sistem perangkat lunak agar scalable, resilient, dan memiliki maintainability tinggi?",
                "Diskusikan pola arsitektur (modular/microservices), decoupling, caching, circuit breakers, dan automated testing."));
        list.add(new InterviewQuestion(208, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana strategi Anda dalam melakukan Code Review untuk menjaga kualitas kode tim tanpa memperlambat deliverable?",
                "Fokus pada checklist readability, keamanan, coverage unit test, dan komunikasi feedback yang konstruktif."));
        list.add(new InterviewQuestion(209, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_ID,
                "Jelaskan konsep Concurrency, Race Condition, dan Deadlock, serta bagaimana Anda mencegahnya.",
                "Ulas sinkronisasi, mutex/locks, thread safety, atomic operations, dan immutability."));
        list.add(new InterviewQuestion(210, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana pendekatan Anda dalam menangani Technical Debt pada basis kode yang sudah berjalan?",
                "Jelaskan cara mengidentifikasi debt, mengukur dampak risiko, dan melakukan refactoring bertahap."));

        // ================= 2. SOFTWARE ENGINEERING (ENGLISH) =================
        list.add(new InterviewQuestion(251, CATEGORY_SOFTWARE, DIFFICULTY_BEGINNER, LANG_EN,
                "Explain the four foundational principles of Object-Oriented Programming (OOP) with real examples.",
                "Detail Abstraction, Encapsulation, Inheritance, and Polymorphism clearly with concise code context."));
        list.add(new InterviewQuestion(252, CATEGORY_SOFTWARE, DIFFICULTY_BEGINNER, LANG_EN,
                "What is the difference between Synchronous and Asynchronous execution in modern applications?",
                "Discuss main-thread responsiveness, background worker threads, promises, and non-blocking I/O."));
        list.add(new InterviewQuestion(253, CATEGORY_SOFTWARE, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do you apply SOLID design principles when refactoring tightly-coupled software components?",
                "Emphasize loose coupling, dependency injection, and modular unit testability benefits."));
        list.add(new InterviewQuestion(254, CATEGORY_SOFTWARE, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Describe your step-by-step strategy for tracking down an intermittent production bug or memory leak.",
                "Detail telemetry inspection, log parsing, heap dump analysis, and root cause reproduction."));
        list.add(new InterviewQuestion(255, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_EN,
                "How do you design distributed systems to ensure fault tolerance, eventual consistency, and low latency?",
                "Discuss CAP theorem trade-offs, message queues, circuit breaker patterns, and read replica topologies."));
        list.add(new InterviewQuestion(256, CATEGORY_SOFTWARE, DIFFICULTY_ADVANCED, LANG_EN,
                "Explain Race Conditions and Deadlocks in multithreaded environments and how you prevent them.",
                "Highlight locks, semaphores, thread-safe data structures, and immutability principles."));

        // ================= 3. WEB DEVELOPMENT (INDONESIAN) =================
        list.add(new InterviewQuestion(301, CATEGORY_WEB, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan siklus HTTP request-response dan bagaimana browser merender sebuah halaman web.",
                "Ulas DNS lookup, TCP/TLS handshake, parsing HTML/CSS/JS, DOM tree, dan critical rendering path."));
        list.add(new InterviewQuestion(302, CATEGORY_WEB, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa perbedaan antara LocalStorage, SessionStorage, dan Cookies dalam aplikasi web?",
                "Bahas batasan kapasitas, masa berlaku data, serta pertimbangan keamanan (HttpOnly, Secure flag)."));
        list.add(new InterviewQuestion(303, CATEGORY_WEB, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan konsep Responsive Web Design dan bagaimana CSS media queries serta Flexbox/Grid bekerja.",
                "Bahas mobile-first approach, fluid layouts, breakpoints, dan adaptasi layout antar perangkat."));
        list.add(new InterviewQuestion(304, CATEGORY_WEB, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana Anda mengoptimalkan Core Web Vitals (LCP, FID/INP, CLS) pada aplikasi web modern?",
                "Jelaskan kompresi aset, code splitting, lazy loading media, serta pencegahan layout shifts."));
        list.add(new InterviewQuestion(305, CATEGORY_WEB, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana strategi Anda dalam mengamankan aplikasi web dari serangan XSS, CSRF, dan SQL Injection?",
                "Ulas input sanitization, CSP (Content Security Policy), anti-CSRF tokens, dan parameterized queries."));
        list.add(new InterviewQuestion(306, CATEGORY_WEB, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Jelaskan arsitektur RESTful API yang baik, termasuk status code HTTP standar yang wajib dipahami.",
                "Bahas statelessness, penamaan endpoint berbasis resource noun, serta status 200, 201, 400, 401, 403, 404, 500."));
        list.add(new InterviewQuestion(307, CATEGORY_WEB, DIFFICULTY_ADVANCED, LANG_ID,
                "Bandingkan kelebihan dan kekurangan Server-Side Rendering (SSR), Client-Side (CSR), dan Static Site Generation (SSG).",
                "Diskusikan SEO, time-to-first-byte (TTFB), performa runtime, dan kompleksitas deployment server."));
        list.add(new InterviewQuestion(308, CATEGORY_WEB, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana Anda mendesain sistem caching web berlapis dari browser cache, CDN, reverse proxy, hingga server state?",
                "Ulas Cache-Control headers, ETag, invalidasi cache, dan stale-while-revalidate pattern."));
        list.add(new InterviewQuestion(309, CATEGORY_WEB, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana arsitektur WebSocket atau Server-Sent Events (SSE) digunakan untuk komunikasi real-time pada web?",
                "Bahas handshake protocol, full-duplex TCP connection, fallback mechanism, dan connection pooling."));
        list.add(new InterviewQuestion(310, CATEGORY_WEB, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana Anda menangani SEO dan web accessibility (a11y) sesuai standar WCAG?",
                "Ulas semantic tags HTML5, atribut ARIA, navigasi keyboard, dan contrast ratio teks."));

        // ================= 3. WEB DEVELOPMENT (ENGLISH) =================
        list.add(new InterviewQuestion(351, CATEGORY_WEB, DIFFICULTY_BEGINNER, LANG_EN,
                "Explain the Critical Rendering Path of a web browser from URL entry to pixels on screen.",
                "Detail DNS, DOM/CSSOM creation, render tree, layout calculation, and paint cycles."));
        list.add(new InterviewQuestion(352, CATEGORY_WEB, DIFFICULTY_BEGINNER, LANG_EN,
                "What are the differences between client storage options: Cookies, LocalStorage, and SessionStorage?",
                "Compare storage limits, transmission over HTTP requests, security scopes, and lifecycles."));
        list.add(new InterviewQuestion(353, CATEGORY_WEB, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do you protect web applications against Cross-Site Scripting (XSS) and Cross-Site Request Forgery (CSRF)?",
                "Discuss contextual output encoding, Content Security Policy (CSP), SameSite cookies, and CSRF tokens."));
        list.add(new InterviewQuestion(354, CATEGORY_WEB, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do you optimize Core Web Vitals (LCP, INP, CLS) in modern performance-critical web applications?",
                "Focus on resource prioritization, deferring unused JavaScript, dynamic imports, and stable layout dimensions."));
        list.add(new InterviewQuestion(355, CATEGORY_WEB, DIFFICULTY_ADVANCED, LANG_EN,
                "Compare SSR, SSG, and CSR paradigms in terms of initial load performance, SEO, and operational costs.",
                "Evaluate server processing overhead, TTFB, hydration costs, and build time scaling."));

        // ================= 4. DATABASE & SQL (INDONESIAN) =================
        list.add(new InterviewQuestion(401, CATEGORY_DATABASE, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa perbedaan mendasar antara Database Relasional (SQL) dan Non-Relasional (NoSQL)?",
                "Bahas struktur skema, integritas referensial (ACID) vs skalabilitas horizontal (BASE/CAP theorem)."));
        list.add(new InterviewQuestion(402, CATEGORY_DATABASE, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan perbedaan antara INNER JOIN, LEFT JOIN, RIGHT JOIN, dan FULL OUTER JOIN dalam query SQL.",
                "Berikan gambaran data yang dihasilkan oleh masing-masing jenis penggabungan tabel."));
        list.add(new InterviewQuestion(403, CATEGORY_DATABASE, DIFFICULTY_BEGINNER, LANG_ID,
                "Apa fungsi Primary Key dan Foreign Key dalam skema basis data relasional?",
                "Jelaskan keunikan identifikasi baris serta penegakan integritas referensial antar tabel."));
        list.add(new InterviewQuestion(404, CATEGORY_DATABASE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana cara kerja Database Indexing (B-Tree/Hash) dan kapan indexing justru dapat menurunkan performa?",
                "Jelaskan percepatan query SELECT vs overhead penulisan (INSERT/UPDATE/DELETE) serta pemilihan index yang efektif."));
        list.add(new InterviewQuestion(405, CATEGORY_DATABASE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Jelaskan prinsip ACID (Atomicity, Consistency, Isolation, Durability) dan mengapa ini krusial dalam transaksi.",
                "Gunakan contoh transfer saldo rekening bank untuk mengilustrasikan rollback dan lock isolation."));
        list.add(new InterviewQuestion(406, CATEGORY_DATABASE, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Jelaskan konsep Normalisasi Database (1NF, 2NF, 3NF) dan kapan denormalisasi sengaja dilakukan.",
                "Bahas pengurangan redundansi data, pencegahan anomali data, serta pertimbangan performa read-heavy."));
        list.add(new InterviewQuestion(407, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana Anda mendiagnosis dan mengoptimalkan slow query yang membebani CPU database di production?",
                "Ulas penggunaan EXPLAIN / EXPLAIN ANALYZE, composite indexes, query refactoring, dan partitioning."));
        list.add(new InterviewQuestion(408, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_ID,
                "Jelaskan 4 level isolasi transaksi (Read Uncommitted, Read Committed, Repeatable Read, Serializable).",
                "Bahas fenomena Dirty Read, Non-repeatable Read, dan Phantom Read serta dampaknya terhadap konkurensi."));
        list.add(new InterviewQuestion(409, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana strategi Anda dalam menerapkan Database Sharding, Replikasi Master-Replica, dan Read/Write Splitting?",
                "Diskusikan replikasi lag, penentuan sharding key, distributed transactions, dan strategi failover."));
        list.add(new InterviewQuestion(410, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana pendekatan Anda dalam melakukan migrasi skema database besar tanpa menimbulkan downtime (Zero Downtime Migration)?",
                "Jelaskan pola expand and contract, backward compatibility kode, dan dual writing."));

        // ================= 4. DATABASE & SQL (ENGLISH) =================
        list.add(new InterviewQuestion(451, CATEGORY_DATABASE, DIFFICULTY_BEGINNER, LANG_EN,
                "What is database normalization and what are the key differences between 1NF, 2NF, and 3NF?",
                "Discuss anomaly prevention, reducing data redundancy, and relational schema integrity."));
        list.add(new InterviewQuestion(452, CATEGORY_DATABASE, DIFFICULTY_BEGINNER, LANG_EN,
                "Explain the operational distinctions between INNER JOIN, LEFT JOIN, and FULL JOIN.",
                "Illustrate matching rows, null padding for unmatched rows, and common reporting use cases."));
        list.add(new InterviewQuestion(453, CATEGORY_DATABASE, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Explain the ACID properties in RDBMS transactions and why they matter in mission-critical applications.",
                "Detail Atomicity, Consistency, Isolation, and Durability using financial transaction examples."));
        list.add(new InterviewQuestion(454, CATEGORY_DATABASE, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do B-Tree indexes work and what trade-offs exist between read acceleration and write overhead?",
                "Explain index tree traversals, cardinality considerations, composite index order, and page fragmentation."));
        list.add(new InterviewQuestion(455, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_EN,
                "How do you inspect and optimize an unindexed slow SQL query using EXPLAIN plans?",
                "Discuss table scans vs index seeks, temporary tables, join algorithms, and execution statistics."));
        list.add(new InterviewQuestion(456, CATEGORY_DATABASE, DIFFICULTY_ADVANCED, LANG_EN,
                "Compare Transaction Isolation Levels and describe how Phantom Reads and Dirty Reads occur.",
                "Detail Read Uncommitted through Serializable, row locks, MVCC (Multi-Version Concurrency Control), and deadlocks."));

        // ================= 5. BEHAVIORAL INTERVIEW (INDONESIAN) =================
        list.add(new InterviewQuestion(501, CATEGORY_BEHAVIORAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Ceritakan pengalaman Anda saat harus beradaptasi dengan perubahan kebutuhan proyek atau deadline yang mendadak.",
                "Gunakan format STAR: jelaskan Situasi, Tugas yang harus diselesaikan, Aksi adaptif Anda, dan Hasil akhirnya."));
        list.add(new InterviewQuestion(502, CATEGORY_BEHAVIORAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Ceritakan situasi ketika Anda harus belajar teknologi atau framework baru dalam waktu singkat untuk menyelesaikan tugas.",
                "Tunjukkan rasa ingin tahu teknis, metode belajar efisien, dan bagaimana Anda mengimplementasikannya secara cepat."));
        list.add(new InterviewQuestion(503, CATEGORY_BEHAVIORAL, DIFFICULTY_BEGINNER, LANG_ID,
                "Ceritakan momen ketika Anda bekerja dalam tim dengan latar belakang anggota yang berbeda-beda.",
                "Fokus pada kolaborasi, saling mendengarkan, pembagian tugas yang adil, dan komunikasi proaktif."));
        list.add(new InterviewQuestion(504, CATEGORY_BEHAVIORAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Ceritakan pengalaman saat Anda menghadapi perbedaan pendapat atau konflik teknis dengan rekan satu tim.",
                "Fokus pada komunikasi terbuka, mendengarkan argumen objektif, mencari konsensus berbasis data, dan menjaga keharmonisan tim."));
        list.add(new InterviewQuestion(505, CATEGORY_BEHAVIORAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Ceritakan momen ketika proyek yang Anda kerjakan mengalami kendala berat atau kegagalan. Apa pelajaran terpenting Anda?",
                "Tunjukkan akuntabilitas, kejujuran dalam evaluasi diri, serta mitigasi preventif yang Anda pelajari untuk masa depan."));
        list.add(new InterviewQuestion(506, CATEGORY_BEHAVIORAL, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Ceritakan situasi ketika Anda harus menyampaikan kabar buruk atau keterlambatan deliverable kepada manajer atau klien.",
                "Jelaskan transparansi, komunikasi lebih awal, alternatif mitigasi solusi yang Anda bawa, dan tanggung jawab penuh."));
        list.add(new InterviewQuestion(507, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Ceritakan pengalaman ketika Anda berinisiatif memimpin atau membantu rekan kerja yang sedang kesulitan mencapai target.",
                "Perlihatkan empati, mentorship teknis, kepemimpinan kolaboratif, serta dampak positif pada produktivitas tim."));
        list.add(new InterviewQuestion(508, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Ceritakan situasi ketika Anda harus membuat keputusan teknis sulit di bawah keterbatasan waktu dan data yang belum lengkap.",
                "Bahas penilaian risiko, trade-off arsitektur, konsultasi dengan pihak terkait, dan evaluasi hasil pasca rilis."));
        list.add(new InterviewQuestion(509, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Ceritakan bagaimana Anda menjaga motivasi dan fokus tim ketika menghadapi proyek yang panjang dan penuh tekanan.",
                "Jelaskan pembagian milestone kecil, perayaan pencapaian tim, serta menjaga keseimbangan beban kerja."));
        list.add(new InterviewQuestion(510, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_ID,
                "Ceritakan pengalaman Anda saat berhasil meyakinkan pimpinan atau stakeholder untuk mengadopsi ide inovatif Anda.",
                "Tekankan riset data pendukung, demonstrasi proof-of-concept, kalkulasi nilai bisnis, dan presentasi yang persuasif."));

        // ================= 5. BEHAVIORAL INTERVIEW (ENGLISH) =================
        list.add(new InterviewQuestion(551, CATEGORY_BEHAVIORAL, DIFFICULTY_BEGINNER, LANG_EN,
                "Describe a situation where you had to adapt quickly to changing project requirements or tight deadlines.",
                "Structure using STAR framework: Situation, Task, your adaptive Action, and the final measurable Result."));
        list.add(new InterviewQuestion(552, CATEGORY_BEHAVIORAL, DIFFICULTY_BEGINNER, LANG_EN,
                "Tell me about a time you had to master a new technology or tool within a tight timeframe.",
                "Showcase fast learning methodology, deliberate practice, resourcefulness, and effective implementation."));
        list.add(new InterviewQuestion(553, CATEGORY_BEHAVIORAL, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Describe a disagreement on technical direction you had with a team member and how you resolved it constructively.",
                "Highlight active listening, data-driven trade-off analysis, empathy, and prioritizing overarching project goals."));
        list.add(new InterviewQuestion(554, CATEGORY_BEHAVIORAL, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Share an instance where an initiative you worked on didn't go as planned. What was your takeaway?",
                "Demonstrate self-awareness, accountability, constructive post-mortem thinking, and preventative changes."));
        list.add(new InterviewQuestion(555, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_EN,
                "Give an example of a tough architectural or engineering trade-off you had to make with incomplete information.",
                "Detail your risk assessment, stakeholder consensus building, fallback plan, and post-launch outcome."));
        list.add(new InterviewQuestion(556, CATEGORY_BEHAVIORAL, DIFFICULTY_ADVANCED, LANG_EN,
                "Describe how you mentored or supported a peer who was struggling to hit a critical milestone.",
                "Highlight collaborative guidance, pair-programming or coaching, empathy, and positive team delivery impact."));

        // ================= 6. TECHNICAL INTERVIEW - MARKETING (INDONESIAN) =================
        list.add(new InterviewQuestion(601, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_BEGINNER, LANG_ID,
                "Bagaimana pendekatan Anda dalam merancang strategi pemasaran (Marketing Mix 4P/7P) untuk meluncurkan produk baru?",
                "Jelaskan analisis Product, Price, Place, Promotion serta penentuan target audiens yang spesifik."));
        list.add(new InterviewQuestion(602, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Jelaskan strategi Digital Marketing yang efektif menggunakan kombinasi SEO, Social Media Ads, dan Content Marketing.",
                "Ulas funnel pemasaran (Awareness, Consideration, Conversion), optimasi kata kunci, dan targeting audiens."));
        list.add(new InterviewQuestion(603, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana strategi Anda dalam membangun dan menjaga Brand Awareness agar produk memiliki daya saing yang unik di pasar?",
                "Fokus pada Unique Selling Proposition (USP), identitas visual, tone of voice, dan konsistensi pesan brand."));
        list.add(new InterviewQuestion(604, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana metode Anda dalam melakukan analisis pasar dan riset kompetitor untuk memetakan target audiens secara tepat?",
                "Ulas riset data demografis, analisis SWOT kompetitor, pain points konsumen, dan pembentukan buyer persona."));
        list.add(new InterviewQuestion(605, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_ADVANCED, LANG_ID,
                "Metrik utama apa saja (seperti CAC, LTV, ROAS, dan Conversion Rate) yang Anda gunakan untuk mengukur efektivitas kampanye pemasaran?",
                "Jelaskan rumus perhitungan Customer Acquisition Cost, Lifetime Value, dan optimasi alokasi anggaran iklan."));

        // ================= 6. TECHNICAL INTERVIEW - MARKETING (ENGLISH) =================
        list.add(new InterviewQuestion(651, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_BEGINNER, LANG_EN,
                "How do you design an end-to-end marketing strategy (using the 4P/7P framework) when launching a new product?",
                "Detail product positioning, pricing models, distribution channels, and targeted promotional strategies."));
        list.add(new InterviewQuestion(652, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Describe your approach to digital marketing campaigns across paid media, SEO, and social engagement.",
                "Explain top-to-bottom funnel tactics, keyword targeting, content calendars, and attribution modeling."));
        list.add(new InterviewQuestion(653, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do you define and strengthen brand positioning to differentiate a brand from fierce competitors?",
                "Highlight Brand Identity, Unique Selling Proposition (USP), messaging consistency, and customer loyalty."));
        list.add(new InterviewQuestion(654, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_ADVANCED, LANG_EN,
                "Explain your methodology for market analysis, competitor benchmarking, and customer persona segmentation.",
                "Discuss TAM/SAM/SOM sizing, qualitative customer interviews, competitor matrix, and user behavior analytics."));
        list.add(new InterviewQuestion(655, CATEGORY_TEKNIS_MARKETING, DIFFICULTY_ADVANCED, LANG_EN,
                "Which quantitative growth metrics (CAC, LTV, ROAS, Retention) do you monitor to evaluate marketing efficiency?",
                "Break down unit economics, customer acquisition payback periods, and data-driven budget optimization."));

        // ================= 7. TECHNICAL INTERVIEW - ACCOUNTING (INDONESIAN) =================
        list.add(new InterviewQuestion(701, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan prinsip-prinsip dasar akuntansi (GAAP / PSAK) dan bagaimana persamaan dasar akuntansi (Aset = Liabilitas + Ekuitas) diterapkan.",
                "Ulas prinsip akrual, konservatisme, penandingan beban (matching principle), dan keseimbangan neraca."));
        list.add(new InterviewQuestion(702, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Jelaskan tiga laporan keuangan utama (Laba Rugi, Neraca, dan Arus Kas) serta keterkaitan timbal balik antar ketiganya.",
                "Jelaskan bagaimana laba bersih mengalir ke ekuitas (laba ditahan) dan rekonsiliasi arus kas operasional."));
        list.add(new InterviewQuestion(703, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana tahapan alur pembukuan (bookkeeping) dari pencatatan jurnal umum, posting ke buku besar, hingga neraca saldo?",
                "Ulas double-entry bookkeeping, debit/kredit, verifikasi saldo akun, dan penyusunan adjusted trial balance."));
        list.add(new InterviewQuestion(704, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana prosedur Anda dalam melakukan rekonsiliasi bank dan membuat jurnal penyesuaian (adjusting entries) di akhir periode?",
                "Bahas penanganan setoran dalam perjalanan (deposit in transit), cek beredar (outstanding checks), dan biaya administrasi."));
        list.add(new InterviewQuestion(705, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_ADVANCED, LANG_ID,
                "Bagaimana Anda melakukan analisis rasio keuangan (rasio likuiditas, solvabilitas, dan profitabilitas) untuk menilai kesehatan finansial perusahaan?",
                "Bahas Current Ratio, Debt-to-Equity, Net Profit Margin, serta interpretasi tren kinerja keuangan tahunan."));

        // ================= 7. TECHNICAL INTERVIEW - ACCOUNTING (ENGLISH) =================
        list.add(new InterviewQuestion(751, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_BEGINNER, LANG_EN,
                "Explain foundational accounting principles (GAAP/IFRS) and the core equation: Assets = Liabilities + Equity.",
                "Discuss accrual basis, matching principle, double-entry mechanism, and dual balance verification."));
        list.add(new InterviewQuestion(752, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "How do the three core financial statements (Income Statement, Balance Sheet, Cash Flow) interconnect?",
                "Detail net income transfer to retained earnings, depreciation impact, and working capital adjustments."));
        list.add(new InterviewQuestion(753, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_INTERMEDIATE, LANG_EN,
                "Describe the entire bookkeeping cycle from source transactions and general journals to the trial balance.",
                "Detail posting to ledgers, journalizing debit and credits, closing temporary accounts, and audit checks."));
        list.add(new InterviewQuestion(754, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_ADVANCED, LANG_EN,
                "What is your approach to performing bank reconciliations and month-end adjusting journal entries?",
                "Address deposits in transit, outstanding checks, bank service fees, interest income, and ledger adjustments."));
        list.add(new InterviewQuestion(755, CATEGORY_TEKNIS_ACCOUNTING, DIFFICULTY_ADVANCED, LANG_EN,
                "Which financial ratios (liquidity, debt-to-equity, profit margins) do you evaluate to determine company health?",
                "Examine current and quick ratios, solvency leverage, operating margins, and return on equity (ROE)."));

        // ================= 8. TECHNICAL INTERVIEW - IT & NETWORKING (INDONESIAN & ENGLISH) =================
        list.add(new InterviewQuestion(801, CATEGORY_TEKNIS_IT, DIFFICULTY_BEGINNER, LANG_ID,
                "Jelaskan perbedaan mendasar antara protokol TCP dan UDP serta contoh skenario penggunaan masing-masing dalam aplikasi modern.",
                "Ulas 3-way handshake, reliabilitas transmisi data vs latency rendah pada video streaming atau gaming."));
        list.add(new InterviewQuestion(802, CATEGORY_TEKNIS_IT, DIFFICULTY_INTERMEDIATE, LANG_ID,
                "Bagaimana arsitektur jaringan komputer dan proses DNS lookup bekerja saat pengguna mengakses URL sebuah aplikasi?",
                "Ulas recursive resolver, root server, TLD server, authoritative name server, dan IP routing."));
        list.add(new InterviewQuestion(851, CATEGORY_TEKNIS_IT, DIFFICULTY_BEGINNER, LANG_EN,
                "Explain the networking differences between TCP and UDP protocols and when to utilize each in production systems.",
                "Discuss packet ordering, flow control, handshake overhead, and latency vs reliability trade-offs."));

        return list;
    }
}
