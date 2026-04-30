package tn.esprit.examen.nomPrenomClasseExamen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ForumCommentNotFoundException extends RuntimeException {
    public ForumCommentNotFoundException(Long id) {
        super("Forum comment with id " + id + " not found");
    }
}

