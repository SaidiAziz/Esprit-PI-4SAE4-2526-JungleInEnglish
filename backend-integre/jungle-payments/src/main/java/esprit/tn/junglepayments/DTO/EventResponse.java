// dto/EventResponse.java
package esprit.tn.junglepayments.DTO;

import lombok.Data;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private Double price;
    private String location;
    private String status;
}