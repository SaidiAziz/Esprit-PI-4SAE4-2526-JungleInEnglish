package esprit.tn.jungleevents.controllers;

import esprit.tn.jungleevents.DTO.SessionRequestDTO;
import esprit.tn.jungleevents.entities.Session;
import esprit.tn.jungleevents.services.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event/sessions")

public class SessionRestController {

    private final SessionService sessionService;

    public SessionRestController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // CREATE
    @PostMapping("/add")
    public Session addSession(@Valid @RequestBody SessionRequestDTO dto) {
        return sessionService.createSession(dto);
    }

    // READ ALL
    @GetMapping("/getAll")
    public List<Session> getAllSessions() {
        return sessionService.getAllSessions();
    }

    // READ BY ID
    @GetMapping("/getById/{id}")
    public Session getSessionById(@PathVariable Long id) {
        return sessionService.getAllSessions()
                .stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
    }

    // READ BY EVENT
    @GetMapping("/event/{eventId}")
    public List<Session> getSessionsByEvent(@PathVariable Long eventId) {
        return sessionService.getSessionsByEvent(eventId);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSession(
            @PathVariable Long id,
            @RequestBody SessionRequestDTO dto
    ) {
        try {
            Session updated = sessionService.updateSession(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // DELETE
    @DeleteMapping("/delete/{id}")
    public void deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
    }
}
