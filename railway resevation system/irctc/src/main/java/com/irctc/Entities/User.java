package com.irctc.Entities;

import java.util.List;

public class User {

    private String name;
    private String email;
    private String passwordHash;
    private String userId;
    private List<String> bookedTickets;

    public User() {
    }

    public User(String name, String email, String passwordHash, String userId, List<String> bookedTickets) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userId = userId;
        this.bookedTickets = bookedTickets;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getBookedTickets() {
        return bookedTickets;
    }

    public void setBookedTickets(List<String> bookedTickets) {
        this.bookedTickets = bookedTickets;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userId='" + userId + '\'' +
                ", bookedTickets=" + bookedTickets +
                '}';
    }
}
