package com.example.eventguard.models;

public class Event {
    public String id;
    public String title;
    public String date; // Format: "Oct 24, 2026" or similar for display
    public String time; // Format: "09:00 AM"
    public String category;
    public String location;
    public String description;
    public String imageUrl;
    public int currentParticipants;
    public int maxParticipants;
    public String status; // Tentative, Joined, Full
    public long eventTimestamp; // For expiration logic

    public Event() {
        // Required for Firebase
    }

    public Event(String id, String title, String date, String time, String category, String location, String description, String imageUrl, int currentParticipants, int maxParticipants, String status, long eventTimestamp) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.category = category;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
        this.currentParticipants = currentParticipants;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
    }

    public void setExpired(boolean expired) {
        if (expired) {
            this.status = "Expired";
        }
    }
}
