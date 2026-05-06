package tn.esprit.administrationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.administrationservice.dto.UserDTO;
import tn.esprit.administrationservice.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class UserValidationService {

  private final RestTemplate plainRestTemplate;

  private static final String USER_SERVICE_URL = "http://localhost:8081/users/getUserById/";

  public void validateTutor(Long tutorId) {
    if (tutorId == null) {
      throw new BadRequestException("tutorId est obligatoire");
    }

    try {
      String url = USER_SERVICE_URL + tutorId;
      System.out.println("Calling user-service URL = " + url);

      UserDTO user = plainRestTemplate.getForObject(url, UserDTO.class);

      if (user == null) {
        throw new BadRequestException("Aucun utilisateur trouvé avec l'id " + tutorId);
      }

      if (!"TUTOR".equalsIgnoreCase(user.getRole())) {
        throw new BadRequestException("L'utilisateur " + tutorId + " n'a pas le rôle TUTOR");
      }

    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new BadRequestException("Impossible de valider le tutorId " + tutorId);
    }
  }
}
