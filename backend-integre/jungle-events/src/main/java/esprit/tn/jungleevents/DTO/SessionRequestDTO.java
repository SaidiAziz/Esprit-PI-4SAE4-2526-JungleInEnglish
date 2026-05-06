package esprit.tn.jungleevents.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionRequestDTO {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String type;
    private String room;
    private String meetingLink;
    private Long eventId;
    private Integer availableSeats; // ✅ champ manquant ajouté

    // getters & setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }


    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
}