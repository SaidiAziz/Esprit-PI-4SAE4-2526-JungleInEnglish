package tn.esprit.administrationservice.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.UUID;

@Service
public class JitsiMeetingService {

  private static final String JITSI_BASE_URL = "https://meet.jit.si";

  public String generateRoomName(Long sessionId, String courseName, Long tutorId) {
    String safeCourse = sanitize(courseName);
    String uniquePart = UUID.randomUUID().toString().substring(0, 8);

    return "Accent-" + safeCourse + "-T" + tutorId + "-S" + sessionId + "-" + uniquePart;
  }

  public String buildMeetingLink(String roomName) {
    return JITSI_BASE_URL + "/" + roomName;
  }

  private String sanitize(String input) {
    if (input == null || input.isBlank()) {
      return "Session";
    }

    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "");

    return normalized
      .replaceAll("[^a-zA-Z0-9]", "")
      .trim();
  }
}
