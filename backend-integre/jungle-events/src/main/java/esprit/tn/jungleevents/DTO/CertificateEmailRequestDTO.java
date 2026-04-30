package esprit.tn.jungleevents.DTO;

public class CertificateEmailRequestDTO {
    private String email;
    private String participantName;
    private String eventTitle;
    private String sessionDate;
    private Long sessionId;

    public String getEmail()           { return email; }
    public void setEmail(String e)     { this.email = e; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String n) { this.participantName = n; }
    public String getEventTitle()      { return eventTitle; }
    public void setEventTitle(String t){ this.eventTitle = t; }
    public String getSessionDate()     { return sessionDate; }
    public void setSessionDate(String d){ this.sessionDate = d; }
    public Long getSessionId()         { return sessionId; }
    public void setSessionId(Long id)  { this.sessionId = id; }
}