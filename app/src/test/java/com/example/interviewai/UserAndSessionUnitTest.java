package com.example.interviewai;

import com.example.interviewai.model.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserAndSessionUnitTest {

    @Test
    public void user_defaultFieldIsIT() {
        User user = new User("John Doe", "john@example.com", "pass123", System.currentTimeMillis());
        assertEquals("IT", user.getField());
    }

    @Test
    public void user_customFieldWorks() {
        User userMarketing = new User("Jane Doe", "jane@example.com", "pass123", System.currentTimeMillis(), User.FIELD_MARKETING);
        assertEquals(User.FIELD_MARKETING, userMarketing.getField());

        User userAccounting = new User("Bob", "bob@example.com", "pass123", System.currentTimeMillis(), User.FIELD_ACCOUNTING);
        assertEquals(User.FIELD_ACCOUNTING, userAccounting.getField());
    }

    @Test
    public void user_availableFieldsContainAllThree() {
        assertArrayEquals(new String[]{"IT", "Marketing", "Accounting"}, User.AVAILABLE_FIELDS);
    }

    @Test
    public void user_passwordUpdateWorksForReset() {
        User user = new User("Alice", "alice@example.com", "oldPass", System.currentTimeMillis(), User.FIELD_IT);
        assertEquals("oldPass", user.getPassword());
        user.setPassword("reset123");
        assertEquals("reset123", user.getPassword());
    }

    @Test
    public void questionBank_technicalCategoryMapping() {
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_IT,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("IT"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_MARKETING,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Marketing"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_ACCOUNTING,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Accounting"));
    }

    @Test
    public void questionBank_interviewUmumQuestionsAvailable() {
        java.util.List<com.example.interviewai.model.InterviewQuestion> questions =
                com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                        com.example.interviewai.data.QuestionBank.CATEGORY_UMUM,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertNotNull(questions);
        assertEquals(5, questions.size());
    }

    @Test
    public void questionBank_technicalQuestionsAvailableForAllFields() {
        // IT
        java.util.List<com.example.interviewai.model.InterviewQuestion> itQuestions =
                com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                        com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_IT,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertNotNull(itQuestions);
        assertEquals(5, itQuestions.size());

        // Marketing
        java.util.List<com.example.interviewai.model.InterviewQuestion> mktQuestions =
                com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                        com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_MARKETING,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertNotNull(mktQuestions);
        assertEquals(5, mktQuestions.size());

        // Accounting
        java.util.List<com.example.interviewai.model.InterviewQuestion> accQuestions =
                com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                        com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_ACCOUNTING,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertNotNull(accQuestions);
        assertEquals(5, accQuestions.size());
    }

    @Test
    public void user_emailRemainsReadOnlyWhenNameUpdated() {
        User user = new User("Old Name", "user@example.com", "pass123", System.currentTimeMillis(), "IT");
        // Emulating DataDiriActivity update: user updates name to "New Name", email is read-only
        User updatedUser = new User("New Name", user.getEmail(), user.getPassword(), user.getRegisteredDate(), user.getField());
        assertEquals("New Name", updatedUser.getName());
        assertEquals("user@example.com", updatedUser.getEmail());
    }

    @Test
    public void workflowManager_preservesAll5QuestionsAndIndividualData() {
        com.example.interviewai.workflow.InterviewWorkflowManager workflow =
                new com.example.interviewai.workflow.InterviewWorkflowManager(
                        "Budi",
                        com.example.interviewai.data.QuestionBank.CATEGORY_UMUM,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertEquals(5, workflow.getTotalQuestions());

        // Answer each of the 5 questions with different answers
        String[] sampleAnswers = new String[]{
                "Jawaban pertama saya adalah mengenai kepemimpinan dalam tim.",
                "Jawaban kedua saya adalah terkait penyelesaian konflik antar rekan kerja.",
                "Jawaban ketiga adalah strategi manajemen waktu dan prioritas proyek.",
                "Jawaban keempat adalah pengalaman beradaptasi dengan perubahan teknologi secara cepat.",
                "Jawaban kelima adalah kontribusi nyata terhadap target perusahaan dan pencapaian target."
        };

        for (int i = 0; i < 5; i++) {
            workflow.saveCurrentAnswer(sampleAnswers[i]);
            workflow.evaluateCurrentAnswer();
            if (i < 4) {
                workflow.goToNext();
            }
        }

        com.example.interviewai.model.InterviewSession session = workflow.createCompletedSession();
        assertNotNull(session);
        assertEquals(5, session.getTotalQuestions());
        assertEquals(5, session.getItems().size());

        // Check that each question has its own question text, answer, score, and feedback
        for (int i = 0; i < 5; i++) {
            com.example.interviewai.model.InterviewItem item = session.getItems().get(i);
            assertNotNull(item.getQuestion());
            assertFalse(item.getQuestion().trim().isEmpty());
            assertEquals(sampleAnswers[i], item.getUserAnswer());
            assertTrue(item.getScore() > 0);
            assertNotNull(item.getStrength());
            assertFalse(item.getStrength().trim().isEmpty());
            assertNotNull(item.getImprovement());
            assertFalse(item.getImprovement().trim().isEmpty());
        }

        // Questions must not be duplicates of Question 1
        assertNotEquals(session.getItems().get(0).getQuestion(), session.getItems().get(1).getQuestion());
        assertNotEquals(session.getItems().get(1).getQuestion(), session.getItems().get(2).getQuestion());
    }

    @Test
    public void workflowManager_partialCompletionStillGenerates5Items() {
        com.example.interviewai.workflow.InterviewWorkflowManager workflow =
                new com.example.interviewai.workflow.InterviewWorkflowManager(
                        "Siti",
                        com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_IT,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        // Only answer question 1
        workflow.saveCurrentAnswer("Pengalaman saya menggunakan database SQL dan query optimasi.");
        workflow.evaluateCurrentAnswer();

        com.example.interviewai.model.InterviewSession session = workflow.createCompletedSession();
        assertNotNull(session);
        assertEquals(5, session.getItems().size());

        // Question 1 answered
        assertTrue(session.getItems().get(0).getUserAnswer().contains("database SQL"));
        // Remaining questions still present with distinct questions
        for (int i = 0; i < 5; i++) {
            assertNotNull(session.getItems().get(i).getQuestion());
            assertFalse(session.getItems().get(i).getQuestion().trim().isEmpty());
        }
        assertNotEquals(session.getItems().get(0).getQuestion(), session.getItems().get(1).getQuestion());
    }

    @Test
    public void forgotPassword_defaultOtpValidation() {
        assertEquals("123456", com.example.interviewai.ForgotPasswordActivity.DEFAULT_OTP);
        assertTrue(com.example.interviewai.ForgotPasswordActivity.DEFAULT_OTP.equals("123456"));
        assertFalse(com.example.interviewai.ForgotPasswordActivity.DEFAULT_OTP.equals("000000"));
        assertFalse(com.example.interviewai.ForgotPasswordActivity.DEFAULT_OTP.equals(""));
    }

    @Test
    public void forgotPassword_passwordMatchingValidation() {
        String newPass = "securePassword123";
        String confirmPassMatching = "securePassword123";
        String confirmPassDifferent = "differentPassword";

        assertTrue(newPass.equals(confirmPassMatching));
        assertFalse(newPass.equals(confirmPassDifferent));
    }
}
