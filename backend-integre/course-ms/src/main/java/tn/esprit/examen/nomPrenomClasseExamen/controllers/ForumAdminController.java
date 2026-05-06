package tn.esprit.examen.nomPrenomClasseExamen.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumBadWord;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumBadWordRepository;

import java.util.List;

@RestController
@RequestMapping("/api/forum/admin")
@RequiredArgsConstructor
public class ForumAdminController {

    private final ForumBadWordRepository badWordRepository;

    @GetMapping("/bad-words")
    public ResponseEntity<List<ForumBadWord>> listBadWords() {
        return ResponseEntity.ok(badWordRepository.findAll());
    }

    @PostMapping("/bad-words")
    public ResponseEntity<ForumBadWord> addBadWord(@Valid @RequestBody ForumBadWord badWord) {
        badWord.setWord(badWord.getWord() == null ? null : badWord.getWord().trim().toLowerCase());
        ForumBadWord saved = badWordRepository.save(badWord);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/bad-words/{id}")
    public ResponseEntity<Void> deleteBadWord(@PathVariable Long id) {
        badWordRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

