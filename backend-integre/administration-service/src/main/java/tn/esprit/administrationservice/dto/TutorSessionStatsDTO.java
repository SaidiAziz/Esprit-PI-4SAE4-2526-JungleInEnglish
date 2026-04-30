package tn.esprit.administrationservice.dto;

public class TutorSessionStatsDTO {

  private Long tutorId;
  private Long sessionCount;

  public TutorSessionStatsDTO(Long tutorId, Long sessionCount) {
    this.tutorId = tutorId;
    this.sessionCount = sessionCount;
  }

  public Long getTutorId() {
    return tutorId;
  }

  public void setTutorId(Long tutorId) {
    this.tutorId = tutorId;
  }

  public Long getSessionCount() {
    return sessionCount;
  }

  public void setSessionCount(Long sessionCount) {
    this.sessionCount = sessionCount;
  }
}
