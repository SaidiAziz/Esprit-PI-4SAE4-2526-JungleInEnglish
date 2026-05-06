package tn.esprit.administrationservice.dto;

public class SessionStatsDTO {

  private long totalSessions;
  private long plannedSessions;
  private long completedSessions;
  private long canceledSessions;
  private double completionRate;

  public SessionStatsDTO() {
  }

  public SessionStatsDTO(long totalSessions, long plannedSessions, long completedSessions, long canceledSessions, double completionRate) {
    this.totalSessions = totalSessions;
    this.plannedSessions = plannedSessions;
    this.completedSessions = completedSessions;
    this.canceledSessions = canceledSessions;
    this.completionRate = completionRate;
  }

  public long getTotalSessions() {
    return totalSessions;
  }

  public void setTotalSessions(long totalSessions) {
    this.totalSessions = totalSessions;
  }

  public long getPlannedSessions() {
    return plannedSessions;
  }

  public void setPlannedSessions(long plannedSessions) {
    this.plannedSessions = plannedSessions;
  }

  public long getCompletedSessions() {
    return completedSessions;
  }

  public void setCompletedSessions(long completedSessions) {
    this.completedSessions = completedSessions;
  }

  public long getCanceledSessions() {
    return canceledSessions;
  }

  public void setCanceledSessions(long canceledSessions) {
    this.canceledSessions = canceledSessions;
  }

  public double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(double completionRate) {
    this.completionRate = completionRate;
  }
}
