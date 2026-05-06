package esprit.tn.jungleevents.DTO;
import lombok.Data;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private Double price;
    private String location;
    private String status;
}