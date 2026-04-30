package tn.esprit.administrationservice.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdministrationController {
    @GetMapping("/api/admin/health")
    public String health() {
        return "Administration Service is running";
    }




}
