package esprit.tn.jungleevents.controllers;

import esprit.tn.jungleevents.DTO.ParticipationDTO;
import esprit.tn.jungleevents.services.ParticipationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participations")


public class ParticipationRestController {

    private final ParticipationService participationService;

    public ParticipationRestController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/add")
    public ParticipationDTO addParticipation(@RequestBody ParticipationDTO dto) {
        return participationService.addParticipation(dto);
    }

    @GetMapping("/getAll")
    public List<ParticipationDTO> getAllParticipations() {
        return participationService.getAllParticipations();
    }

    @GetMapping("/session/{sessionId}")
    public List<ParticipationDTO> getBySession(@PathVariable Long sessionId) {
        return participationService.getBySession(sessionId);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteParticipation(@PathVariable Long id) {
        participationService.deleteParticipation(id);
    }

    @PutMapping("/update/{id}")
    public ParticipationDTO updateParticipation(
            @PathVariable Long id,
            @RequestBody ParticipationDTO dto) {

        return participationService.updateParticipation(id, dto);
    }

    @GetMapping("/getById/{id}")
    public ParticipationDTO getParticipationById(@PathVariable Long id) {
        return participationService.getParticipationById(id);
    }
    @GetMapping("/by-email-and-event")
    public ParticipationDTO getByEmailAndEvent(
            @RequestParam String email,
            @RequestParam Long eventId) {
        return participationService.getByEmailAndEventId(email, eventId);
    }
}