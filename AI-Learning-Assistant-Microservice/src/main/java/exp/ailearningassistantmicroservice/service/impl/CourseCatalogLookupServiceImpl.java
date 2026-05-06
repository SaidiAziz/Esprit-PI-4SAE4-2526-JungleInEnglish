package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.integration.CourseCatalogClient;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;
import exp.ailearningassistantmicroservice.service.CourseCatalogLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCatalogLookupServiceImpl implements CourseCatalogLookupService {

    private final CourseCatalogClient courseCatalogClient;

    @Override
    public List<CourseCatalogItem> findRecommendedCourses(String level, SkillType focusSkill, Long preferredCourseId, int limit) {
        List<CourseCatalogItem> levelCourses = safeGetByLevel(level);
        List<CourseCatalogItem> activeLevelCourses = filterActive(levelCourses);
        List<CourseCatalogItem> candidates = !activeLevelCourses.isEmpty() ? activeLevelCourses : filterActive(safeGetAllCourses());

        if (candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((CourseCatalogItem course) -> recommendationScore(course, focusSkill, preferredCourseId)).reversed()
                        .thenComparing(CourseCatalogItem::getId, Comparator.nullsLast(Long::compareTo)))
                .limit(limit)
                .toList();
    }

    @Override
    public Map<Long, CourseCatalogItem> getCourseMap(Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        Set<Long> requestedIds = courseIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (requestedIds.isEmpty()) {
            return Map.of();
        }

        List<CourseCatalogItem> courses = safeGetAllCourses();
        if (courses.isEmpty()) {
            return Map.of();
        }

        return courses.stream()
                .filter(course -> course.getId() != null && requestedIds.contains(course.getId()))
                .collect(Collectors.toMap(CourseCatalogItem::getId, course -> course, (left, right) -> left, LinkedHashMap::new));
    }

    private List<CourseCatalogItem> safeGetByLevel(String level) {
        try {
            return courseCatalogClient.getCoursesByLevel(level);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private List<CourseCatalogItem> safeGetAllCourses() {
        try {
            return courseCatalogClient.getAllCourses();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private List<CourseCatalogItem> filterActive(List<CourseCatalogItem> courses) {
        return courses.stream()
                .filter(course -> Boolean.TRUE.equals(course.getActive()))
                .toList();
    }

    private int recommendationScore(CourseCatalogItem course, SkillType focusSkill, Long preferredCourseId) {
        int score = 0;
        if (preferredCourseId != null && preferredCourseId.equals(course.getId())) {
            score += 4;
        }

        String searchableText = ((course.getTitle() == null ? "" : course.getTitle()) + " " +
                (course.getDescription() == null ? "" : course.getDescription())).toLowerCase(Locale.ROOT);

        for (String keyword : keywordsFor(focusSkill)) {
            if (searchableText.contains(keyword)) {
                score += 3;
            }
        }

        if (course.getPrice() != null && course.getPrice() == 0) {
            score += 1;
        }
        return score;
    }

    private List<String> keywordsFor(SkillType focusSkill) {
        return switch (focusSkill) {
            case GRAMMAR -> List.of("grammar", "writing", "sentence", "tense", "structure");
            case LISTENING -> List.of("listening", "audio", "comprehension", "podcast", "dictation");
            case SPEAKING -> List.of("speaking", "oral", "conversation", "fluency", "pronunciation");
        };
    }
}
