package esprit.tn.jungleevents.controllers;

import esprit.tn.jungleevents.DTO.EventResponse;
import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    @Autowired
    private EventService eventService;

    @PostMapping(
            value = "/addEvent",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> addEvent(
            @Valid @ModelAttribute Event event,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (event.getEndDate().isBefore(event.getStartDate())) {
            return ResponseEntity.badRequest().body("La date de fin doit être après la date de début");
        }

        // ✅ Gestion upload fichier
        if (file != null && !file.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);

            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs(); // IMPORTANT
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            File destinationFile = new File(uploadDir + fileName);
            file.transferTo(destinationFile.getAbsoluteFile());

            event.setMediaFileName(fileName);
        }

        return ResponseEntity.ok(eventService.createEvent(event));
    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("getEventById/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PutMapping(
            value = "updateEventById/{id}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute Event event,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (file != null && !file.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);

            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs(); // IMPORTANT
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            File destinationFile = new File(uploadDir + fileName);
            file.transferTo(destinationFile);

            event.setMediaFileName(fileName);
        }

        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }



    @DeleteMapping("deleteEventById/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
    @GetMapping("/internal/{id}")
    public ResponseEntity<EventResponse> getEventForPayment(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setPrice(event.getPrice());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        return ResponseEntity.ok(dto);
    }
}
