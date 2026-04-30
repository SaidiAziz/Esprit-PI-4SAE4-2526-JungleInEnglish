package esprit.tn.junglepayments.repositories;

import esprit.tn.junglepayments.DTO.EventResponse;
import esprit.tn.junglepayments.DTO.ParticipationRequest;
import esprit.tn.junglepayments.DTO.RegistrationResponse;
import esprit.tn.junglepayments.DTO.EventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "jungle-events")
public interface EventFeignClient {

    @GetMapping("/api/events/internal/{id}")
    EventResponse getEventById(@PathVariable Long id);

    @GetMapping("/api/participations/by-email-and-event")
    RegistrationResponse getRegistration(
            @RequestParam("eventId") Long eventId,
            @RequestParam("email") String email
    );
    @PostMapping("/api/participations/add")
    RegistrationResponse createParticipation(@RequestBody ParticipationRequest request);
}
