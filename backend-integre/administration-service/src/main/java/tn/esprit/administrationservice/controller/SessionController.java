package tn.esprit.administrationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.administrationservice.entity.Session;
import tn.esprit.administrationservice.service.SessionService;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public Session create(@Valid @RequestBody Session session) {
        return sessionService.create(session);
    }

    @PutMapping("/{id}")
    public Session update(@PathVariable Long id,@Valid @RequestBody Session session) {
        return sessionService.update(id, session);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }

    @GetMapping("/{id}")
    public Session getById(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    @GetMapping
    public List<Session> getAll() {
        return sessionService.getAll();
    }

    @GetMapping("/by-tutor")
    public List<Session> getByTutor(@RequestParam Long tutorId) {
        return sessionService.getByTutor(tutorId);
    }

    // ✅ ENDPOINT MÉTIER : CANCEL
    @PutMapping("/{id}/cancel")
    public Session cancel(@PathVariable Long id) {
        return sessionService.cancel(id);
    }

    // ✅ ENDPOINT MÉTIER : COMPLETE
    @PutMapping("/{id}/complete")
    public Session complete(@PathVariable Long id) {
        return sessionService.complete(id);
    }
}