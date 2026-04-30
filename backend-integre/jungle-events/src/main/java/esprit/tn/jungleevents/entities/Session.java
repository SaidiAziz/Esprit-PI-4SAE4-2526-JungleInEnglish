package esprit.tn.jungleevents.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;
    @JsonFormat(pattern="HH:mm")
    @Schema(example = "09:00")
    private LocalTime startTime;

    @JsonFormat(pattern="HH:mm")
    @Schema(example = "11:00")
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionType type;


    private String room;

    private String meetingLink;

    @Positive(message = "Le nombre de places doit être positif")
    private Integer availableSeats;

    @JsonIgnore
    @ManyToOne
    @NotNull(message = "L'événement est obligatoire")
    private Event event;

    @JsonIgnore
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Participation> participations;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Participation> getParticipations() {
        return participations;
    }

    public void setParticipations(List<Participation> participations) {
        this.participations = participations;
    }
    public Long getEventId() {
        return event != null ? event.getId() : null;
    }
}



