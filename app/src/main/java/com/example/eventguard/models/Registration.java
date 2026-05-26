package com.example.eventguard.models;

public class Registration {
    public String registrationId;
    public String userId;
    public String eventId;
    public String eventTitle;
    public String eventDate;
    public String eventLocation;
    public String eventTime;
    public long eventTimestamp;
    public boolean isAttendanceMarked;
    public long registrationTime;

    public Registration() {
        // Required for Firebase
    }

    public Registration(String registrationId, String userId, String eventId, String eventTitle, String eventDate, String eventLocation, String eventTime, long eventTimestamp) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
        this.eventTimestamp = eventTimestamp;
        this.isAttendanceMarked = false;
        this.registrationTime = System.currentTimeMillis();
    }
}
