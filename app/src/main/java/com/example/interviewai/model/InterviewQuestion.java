package com.example.interviewai.model;

import java.io.Serializable;

public class InterviewQuestion implements Serializable {
    private final int id;
    private final String category;
    private final String difficulty;
    private final String language;
    private final String questionText;
    private final String tip;

    public InterviewQuestion(int id, String category, String questionText, String tip) {
        this(id, category, "Intermediate", "Indonesian", questionText, tip);
    }

    public InterviewQuestion(int id, String category, String difficulty, String language, String questionText, String tip) {
        this.id = id;
        this.category = category;
        this.difficulty = difficulty;
        this.language = language;
        this.questionText = questionText;
        this.tip = tip;
    }

    public int getId() {
        return id;
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

    public String getQuestionText() {
        return questionText;
    }

    public String getTip() {
        return tip;
    }
}
