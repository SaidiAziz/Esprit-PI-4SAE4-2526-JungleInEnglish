package tn.esprit.administrationservice.exception;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ 400 - erreurs métier (ex: conflit, règles cancel/complete, etc.)
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(BadRequestException ex) {
        return Map.of("error", ex.getMessage());
    }

    // ✅ 404 - ressource introuvable
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(ChangeSetPersister.NotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    // ✅ 400 - contrôle de saisie (@Valid)
    // Retourne les erreurs par champ : { "tutorId": "...", "endTime": "..." }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        // Si tu veux aussi inclure les erreurs globales (@AssertTrue, etc.)
        ex.getBindingResult().getGlobalErrors().forEach(err ->
                fieldErrors.put(err.getObjectName(), err.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation error");
        body.put("fields", fieldErrors);

        return body;
    }

    // ✅ 500 - fallback (utile en dev pour ne pas avoir un 500 vide)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAny(Exception ex) {
        return Map.of("error", "Internal server error", "details", ex.getMessage());
    }
}