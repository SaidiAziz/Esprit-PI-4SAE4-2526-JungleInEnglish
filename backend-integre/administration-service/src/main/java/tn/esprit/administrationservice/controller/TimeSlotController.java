package tn.esprit.administrationservice.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.administrationservice.dto.TimeSlotRequest;
import tn.esprit.administrationservice.entity.OwnerType;
import tn.esprit.administrationservice.entity.TimeSlot;
import tn.esprit.administrationservice.service.TimeSlotService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timeslots")
public class TimeSlotController {
    private final TimeSlotService service;

    public TimeSlotController(TimeSlotService service) {
        this.service = service;
    }

    @GetMapping
    public List<TimeSlot> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TimeSlot getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // Example: /api/admin/timeslots/by-owner?ownerType=TUTOR&ownerId=5
    @GetMapping("/by-owner")
    public List<TimeSlot> getByOwner(@RequestParam OwnerType ownerType,
                                     @RequestParam(required = false) Long ownerId) {
        return service.getByOwner(ownerType, ownerId);
    }

    @PostMapping
    public TimeSlot create(@Valid @RequestBody TimeSlotRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TimeSlot update(@PathVariable Long id, @Valid @RequestBody TimeSlotRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
