package tn.esprit.examen.nomPrenomClasseExamen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ForumPostNotFoundException extends RuntimeException {
    public ForumPostNotFoundException(Long id) {
        super("Forum post with id " + id + " not found");
    }
}

