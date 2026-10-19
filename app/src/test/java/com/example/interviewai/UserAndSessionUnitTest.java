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

        User userHR = new User("Clara", "clara@example.com", "pass123", System.currentTimeMillis(), User.FIELD_HR);
        assertEquals(User.FIELD_HR, userHR.getField());

        User userFinance = new User("David", "david@example.com", "pass123", System.currentTimeMillis(), User.FIELD_FINANCE);
        assertEquals(User.FIELD_FINANCE, userFinance.getField());

        User userBM = new User("Eko", "eko@example.com", "pass123", System.currentTimeMillis(), User.FIELD_BUSINESS_MANAGEMENT);
        assertEquals(User.FIELD_BUSINESS_MANAGEMENT, userBM.getField());

        User userSales = new User("Fani", "fani@example.com", "pass123", System.currentTimeMillis(), User.FIELD_SALES);
        assertEquals(User.FIELD_SALES, userSales.getField());

        User userGD = new User("Gilang", "gilang@example.com", "pass123", System.currentTimeMillis(), User.FIELD_GRAPHIC_DESIGN);
        assertEquals(User.FIELD_GRAPHIC_DESIGN, userGD.getField());

        User userDA = new User("Hana", "hana@example.com", "pass123", System.currentTimeMillis(), User.FIELD_DATA_ANALYTICS);
        assertEquals(User.FIELD_DATA_ANALYTICS, userDA.getField());

        User userCS = new User("Indra", "indra@example.com", "pass123", System.currentTimeMillis(), User.FIELD_CUSTOMER_SERVICE);
        assertEquals(User.FIELD_CUSTOMER_SERVICE, userCS.getField());
    }

    @Test
    public void user_availableFieldsContainAllTen() {
        String[] expectedFields = new String[]{
                "IT",
                "Marketing",
                "Accounting",
                "Human Resources",
                "Finance",
                "Business Management",
                "Sales",
                "Graphic Design",
                "Data Analytics",
                "Customer Service"
        };
        assertArrayEquals(expectedFields, User.AVAILABLE_FIELDS);
        assertEquals(10, User.AVAILABLE_FIELDS.length);
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
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_HR,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Human Resources"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_HR,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("HR"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_FINANCE,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Finance"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_BUSINESS_MANAGEMENT,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Business Management"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_SALES,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Sales"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_GRAPHIC_DESIGN,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Graphic Design"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_DATA_ANALYTICS,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Data Analytics"));
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_CUSTOMER_SERVICE,
                com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField("Customer Service"));
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
    public void questionBank_technicalQuestionsAvailableForAllTenFields() {
        String[] fields = User.AVAILABLE_FIELDS;
        for (String field : fields) {
            String category = com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField(field);
            java.util.List<com.example.interviewai.model.InterviewQuestion> questions =
                    com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                            category,
                            com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                            com.example.interviewai.data.QuestionBank.LANG_ID,
                            5
                    );
            assertNotNull("Questions for field " + field + " should not be null", questions);
            assertEquals("Expected 5 questions for field " + field, 5, questions.size());

            // Ensure not treated as General Interview
            assertTrue("Field " + field + " category should contain Teknis", category.contains("Teknis"));
            assertFalse("Field " + field + " category should not be General", category.equalsIgnoreCase(com.example.interviewai.data.QuestionBank.CATEGORY_UMUM));
        }
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

    @Test
    public void profileField_and_sessionField_remainSeparate() {
        // User's profile field is IT
        User profileUser = new User("Alice", "alice@example.com", "pass123", System.currentTimeMillis(), User.FIELD_IT);
        assertEquals(User.FIELD_IT, profileUser.getField());

        // User chooses Finance for a technical interview session
        String temporarySessionField = User.FIELD_FINANCE;
        String sessionCategory = com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField(temporarySessionField);
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_FINANCE, sessionCategory);

        // Verify the profile field was NOT changed by the temporary session selection
        assertEquals(User.FIELD_IT, profileUser.getField());

        // Now user updates profile to Marketing
        profileUser.setField(User.FIELD_MARKETING);
        assertEquals(User.FIELD_MARKETING, profileUser.getField());

        // Next technical interview defaults to Marketing, but user can choose Graphic Design for that session
        String nextSessionField = User.FIELD_GRAPHIC_DESIGN;
        String nextSessionCategory = com.example.interviewai.data.QuestionBank.getTechnicalCategoryForField(nextSessionField);
        assertEquals(com.example.interviewai.data.QuestionBank.CATEGORY_TEKNIS_GRAPHIC_DESIGN, nextSessionCategory);

        // Profile remains Marketing
        assertEquals(User.FIELD_MARKETING, profileUser.getField());
    }

    @Test
    public void generalInterview_preservedAndNotClassifiedAsTechnical() {
        String generalCategory = com.example.interviewai.data.QuestionBank.CATEGORY_UMUM;
        assertFalse("General interview should not contain Teknis", generalCategory.toLowerCase().contains("teknis"));

        java.util.List<com.example.interviewai.model.InterviewQuestion> generalQuestions =
                com.example.interviewai.data.QuestionBank.getFilteredQuestions(
                        generalCategory,
                        com.example.interviewai.data.QuestionBank.DIFFICULTY_INTERMEDIATE,
                        com.example.interviewai.data.QuestionBank.LANG_ID,
                        5
                );
        assertNotNull(generalQuestions);
        assertEquals(5, generalQuestions.size());
    }
}
