package com.example.eventguard.models;

public class User {
    public String name, email, role, bio, phone, country, profilePic;
    public long joinedDate;

    public User() {
        // Required for Firebase
    }

    // Constructor for registration (3 arguments)
    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.bio = "";
        this.phone = "";
        this.country = "";
        this.profilePic = "user_profile"; // Default avatar
        this.joinedDate = System.currentTimeMillis();
    }

    // Constructor for full profile
    public User(String name, String email, String role, String bio, String phone, String country, String profilePic, long joinedDate) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.bio = bio;
        this.phone = phone;
        this.country = country;
        this.profilePic = profilePic;
        this.joinedDate = joinedDate;
    }
}
