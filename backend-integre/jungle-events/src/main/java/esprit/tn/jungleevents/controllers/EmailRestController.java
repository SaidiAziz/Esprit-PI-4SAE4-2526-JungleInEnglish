package esprit.tn.jungleevents.controllers;

import esprit.tn.jungleevents.DTO.CertificateEmailRequestDTO;
import esprit.tn.jungleevents.DTO.RibEmailRequestDTO;
import esprit.tn.jungleevents.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")

public class EmailRestController {

    @Autowired
    private EmailService emailService;  // ✅ injection via interface

    @PostMapping("/send-rib")
    public ResponseEntity<String> sendRib(@RequestBody RibEmailRequestDTO request) {
        emailService.sendRibEmail(
                request.getEmail(),
                request.getEventTitle(),
                request.getSessionDate(),
                request.getAmount()
        );
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Email sent successfully\"}");

    }
    @PostMapping("/send-certificate")
    public ResponseEntity<String> sendCertificate(@RequestBody CertificateEmailRequestDTO request) {
        emailService.sendCertificateEmail(
                request.getEmail(),
                request.getParticipantName(),
                request.getEventTitle(),
                request.getSessionDate()
        );
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Certificate sent successfully\"}");
    }
}