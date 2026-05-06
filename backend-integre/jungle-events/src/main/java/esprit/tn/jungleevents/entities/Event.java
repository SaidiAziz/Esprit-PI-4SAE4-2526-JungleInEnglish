package esprit.tn.jungleevents.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000)
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    //@NotBlank(message = "Le lieu est obligatoire")
    private String location;

    private String meetLink;

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    @NotBlank(message = "Le statut online est obligatoire")
    private String format;

    @Enumerated(EnumType.STRING)
    private EventStatus status;


    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    private Double price;

    @Positive(message = "Le nombre max de participants doit être positif")
    private Integer maxParticipants;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    private String mediaFileName;

    private Double latitude;

    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Session> sessions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public String getMediaFileName() {return mediaFileName;}

    public void setMediaFileName(String mediaFileName) {this.mediaFileName = mediaFileName;}
}
