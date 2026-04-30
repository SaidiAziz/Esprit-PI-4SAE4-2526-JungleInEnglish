package tn.esprit.administrationservice.dto;


public class MonthlySessionStatsDTO {

  private int month;
  private long sessionCount;

  public MonthlySessionStatsDTO(int month, long sessionCount) {
    this.month = month;
    this.sessionCount = sessionCount;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public long getSessionCount() {
    return sessionCount;
  }

  public void setSessionCount(long sessionCount) {
    this.sessionCount = sessionCount;
  }
}
