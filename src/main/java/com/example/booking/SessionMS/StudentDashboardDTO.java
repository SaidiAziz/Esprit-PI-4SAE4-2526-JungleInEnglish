package com.example.booking.SessionMS;

import java.util.List;

public class StudentDashboardDTO {
    private long totalSessions;
    private long upcomingSessions;
    private int totalMinutesLearned;
    private List<Session> nextSessions;
    private List<Session> sessionHistory;

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long v) { this.totalSessions = v; }

    public long getUpcomingSessions() { return upcomingSessions; }
    public void setUpcomingSessions(long v) { this.upcomingSessions = v; }

    public int getTotalMinutesLearned() { return totalMinutesLearned; }
    public void setTotalMinutesLearned(int v) { this.totalMinutesLearned = v; }

    public List<Session> getNextSessions() { return nextSessions; }
    public void setNextSessions(List<Session> v) { this.nextSessions = v; }

    public List<Session> getSessionHistory() { return sessionHistory; }
    public void setSessionHistory(List<Session> v) { this.sessionHistory = v; }
}