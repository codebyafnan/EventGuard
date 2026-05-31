package com.example.eventguard.models;

public class Attendance {
    public String attendanceId;
    public String userId;
    public String userName;
    public String userEmail;
    public String eventId;
    public String eventName;
    public String status;
    public long timestamp;
    public String organizerId;

    public Attendance() {
        // Required for Firebase
    }

    public Attendance(String attendanceId, String userId, String userName, String userEmail, String eventId, String eventName, String status, long timestamp, String organizerId) {
        this.attendanceId = attendanceId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.timestamp = timestamp;
        this.organizerId = organizerId;
    }
}
