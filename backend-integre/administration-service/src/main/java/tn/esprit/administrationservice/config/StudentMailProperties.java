package tn.esprit.administrationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.students")
public class StudentMailProperties {

    private List<String> emails = new ArrayList<>();

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
}