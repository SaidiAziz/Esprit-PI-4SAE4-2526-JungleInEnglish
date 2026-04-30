package com.example.booking.SessionMS;

import java.util.List;

public class TutorDashboardDTO {
    private long totalSessions;
    private long upcomingSessions;
    private double averageRating;
    private List<Session> plannedSessions;

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long v) { this.totalSessions = v; }

    public long getUpcomingSessions() { return upcomingSessions; }
    public void setUpcomingSessions(long v) { this.upcomingSessions = v; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double v) { this.averageRating = v; }

    public List<Session> getPlannedSessions() { return plannedSessions; }
    public void setPlannedSessions(List<Session> v) { this.plannedSessions = v; }
}