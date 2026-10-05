package com.example.interviewai.model;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String password;
    private long registeredDate;

    public User(String name, String email, String password, long registeredDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registeredDate = registeredDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }
}
