package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ForumDtos;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Course;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumCategory;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReactionType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumTag;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.CourseRepository;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumPostRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForumRecommendationService {

    private final ForumPostRepository postRepository;
    private final CourseRepository courseRepository;

    public ForumDtos.RecommendationsResponse recommendForPost(ForumPost post) {
        Set<String> tagSlugs = post.getTags() == null ? Set.of() :
                post.getTags().stream().map(ForumTag::getSlug).collect(Collectors.toSet());

        var similarPage = postRepository.findSimilar(
                post.getId(),
                post.getCategory() == null ? ForumCategory.GENERAL : post.getCategory(),
                tagSlugs.isEmpty() ? List.of("__none__") : tagSlugs,
                PageRequest.of(0, 5, Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("updatedAt")))
        );

        List<ForumDtos.PostListItemResponse> similar = similarPage.getContent().stream().map(p -> {
            long commentsCount = p.getComments() == null ? 0 : p.getComments().size();
            long likes = p.getReactions() == null ? 0 : p.getReactions().stream().filter(r -> r.getType() == ForumReactionType.LIKE).count();
            long dislikes = p.getReactions() == null ? 0 : p.getReactions().stream().filter(r -> r.getType() == ForumReactionType.DISLIKE).count();

            return new ForumDtos.PostListItemResponse(
                    p.getId(),
                    p.getTitle(),
                    p.getAuthorName(),
                    p.getCategory() == null ? null : p.getCategory().name(),
                    p.getCourseId(),
                    p.getTags() == null ? Set.of() : p.getTags().stream().map(ForumTag::getSlug).collect(Collectors.toSet()),
                    Boolean.TRUE.equals(p.getPinned()),
                    p.getCreatedAt(),
                    p.getUpdatedAt(),
                    commentsCount,
                    likes,
                    dislikes
            );
        }).toList();

        List<ForumDtos.CourseSuggestion> courses = suggestCourses(post);

        return new ForumDtos.RecommendationsResponse(similar, courses);
    }

    private List<ForumDtos.CourseSuggestion> suggestCourses(ForumPost post) {
        // simple mapping category -> course.level
        String level = switch (post.getCategory() == null ? ForumCategory.GENERAL : post.getCategory()) {
            case IELTS, BUSINESS_ENGLISH, WRITING -> "advanced";
            case SPEAKING, LISTENING, PRONUNCIATION -> "intermediate";
            case GRAMMAR, VOCABULARY -> "beginner";
            default -> "beginner";
        };

        List<Course> candidates = courseRepository.findByLevelIgnoreCase(level);
        return candidates.stream()
                .limit(5)
                .map(c -> new ForumDtos.CourseSuggestion(
                        c.getId(),
                        c.getTitle(),
                        c.getLevel(),
                        c.getDurationHours(),
                        c.getPrice()
                ))
                .toList();
    }
}

