package exp.ailearningassistantmicroservice.integration;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CourseCatalogItem {

    private Long id;
    private String title;
    private String level;
    private String description;
    private Integer durationHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private Integer maxStudents;
    private Boolean active;
}
