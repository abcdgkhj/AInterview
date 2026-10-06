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
}
