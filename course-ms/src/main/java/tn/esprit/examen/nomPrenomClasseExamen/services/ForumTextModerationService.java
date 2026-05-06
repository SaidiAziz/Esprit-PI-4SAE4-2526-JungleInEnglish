package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumBadWordRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForumTextModerationService {

    private final ForumBadWordRepository badWordRepository;

    public String censor(String input) {
        if (input == null || input.isBlank()) return input;

        String out = input;
        List<String> badWords = badWordRepository.findAll().stream()
                .map(w -> w.getWord() == null ? "" : w.getWord().trim())
                .filter(s -> !s.isBlank())
                .toList();

        for (String w : badWords) {
            // word boundary-ish, case-insensitive
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(w) + "\\b");
            out = p.matcher(out).replaceAll(mask(w.length()));
        }
        return out;
    }

    private String mask(int n) {
        int len = Math.max(3, n);
        return "*".repeat(Math.min(len, 12));
    }
}

