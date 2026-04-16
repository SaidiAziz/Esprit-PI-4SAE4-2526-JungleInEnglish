package tn.esprit.examen.nomPrenomClasseExamen.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ForumDtos;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumCategory;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumComment;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReactionType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReport;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportTargetType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumTag;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumTagRepository;
import tn.esprit.examen.nomPrenomClasseExamen.services.ForumService;
import tn.esprit.examen.nomPrenomClasseExamen.services.ForumRecommendationService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final ForumTagRepository tagRepository;
    private final ForumRecommendationService recommendationService;

    @PostMapping("/posts")
    public ResponseEntity<ForumDtos.PostDetailsResponse> createPost(@Valid @RequestBody ForumDtos.CreatePostRequest req) {
        ForumCategory cat = parseCategory(req.category());
        Set<ForumTag> tags = resolveTags(req.tags());

        ForumPost created = forumService.createPost(ForumPost.builder()
                .title(req.title())
                .content(req.content())
                .authorName(req.authorName())
                .category(cat)
                .courseId(req.courseId())
                .tags(tags)
                .pinned(false)
                .deleted(false)
                .build());
        ForumPost loaded = forumService.getPostById(created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetails(loaded));
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<ForumDtos.PostListItemResponse>> listPosts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 50),
                Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("updatedAt"))
        );

        Page<ForumPost> posts = forumService.searchPosts(q, pageable);
        Page<ForumDtos.PostListItemResponse> mapped = posts.map(this::toListItem);
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ForumDtos.PostDetailsResponse> getPost(@PathVariable Long id) {
        ForumPost post = forumService.getPostById(id);
        return ResponseEntity.ok(toDetails(post));
    }

    @GetMapping("/posts/{id}/recommendations")
    public ResponseEntity<ForumDtos.RecommendationsResponse> recommendations(@PathVariable Long id) {
        ForumPost post = forumService.getPostById(id);
        return ResponseEntity.ok(recommendationService.recommendForPost(post));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<ForumDtos.PostDetailsResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody ForumDtos.UpdatePostRequest req
    ) {
        ForumCategory cat = parseCategory(req.category());
        Set<ForumTag> tags = resolveTags(req.tags());

        forumService.updatePost(id, ForumPost.builder()
                .title(req.title())
                .content(req.content())
                .authorName(req.authorName())
                .category(cat)
                .courseId(req.courseId())
                .tags(tags)
                .build());
        ForumPost updated = forumService.getPostById(id);
        return ResponseEntity.ok(toDetails(updated));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> softDeletePost(@PathVariable Long id) {
        forumService.softDeletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/posts/{id}/pin")
    public ResponseEntity<ForumDtos.PostDetailsResponse> setPinned(@PathVariable Long id, @RequestParam boolean pinned) {
        forumService.setPinned(id, pinned);
        ForumPost updated = forumService.getPostById(id);
        return ResponseEntity.ok(toDetails(updated));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ForumDtos.CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody ForumDtos.CreateCommentRequest req
    ) {
        ForumComment created = forumService.addComment(postId, ForumComment.builder()
                .content(req.content())
                .authorName(req.authorName())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(toComment(created));
    }

    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<ForumDtos.PostDetailsResponse> reactToPost(
            @PathVariable Long postId,
            @Valid @RequestBody ForumDtos.ReactToPostRequest req
    ) {
        ForumReactionType type = req.type() == ForumDtos.ReactionType.DISLIKE
                ? ForumReactionType.DISLIKE
                : ForumReactionType.LIKE;

        ForumPost updated = forumService.reactToPost(postId, req.voterName(), type);
        ForumPost loaded = forumService.getPostById(updated.getId());
        return ResponseEntity.ok(toDetails(loaded));
    }

    @PostMapping("/posts/{postId}/accept")
    public ResponseEntity<ForumDtos.PostDetailsResponse> acceptAnswer(
            @PathVariable Long postId,
            @RequestBody ForumDtos.AcceptAnswerRequest req
    ) {
        forumService.acceptAnswer(postId, req.commentId());
        return ResponseEntity.ok(toDetails(forumService.getPostById(postId)));
    }

    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ForumDtos.ReportResponse> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ForumDtos.ReportRequest req
    ) {
        ForumReport report = forumService.report(ForumReportTargetType.POST, postId, req.reporterName(), req.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(toReport(report));
    }

    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<ForumDtos.ReportResponse> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ForumDtos.ReportRequest req
    ) {
        ForumReport report = forumService.report(ForumReportTargetType.COMMENT, commentId, req.reporterName(), req.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(toReport(report));
    }

    // Admin moderation (simple, no auth)
    @GetMapping("/admin/reports")
    public ResponseEntity<Page<ForumDtos.ReportResponse>> listReports(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ForumReportStatus st = ForumReportStatus.valueOf(status.toUpperCase(Locale.ROOT));
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 50), Sort.by(Sort.Order.desc("createdAt")));
        Page<ForumReport> reports = forumService.listReports(st, pageable);
        return ResponseEntity.ok(reports.map(this::toReport));
    }

    @PostMapping("/admin/reports/{id}/review")
    public ResponseEntity<ForumDtos.ReportResponse> reviewReport(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(defaultValue = "admin") String reviewedBy
    ) {
        ForumReportStatus st = ForumReportStatus.valueOf(status.toUpperCase(Locale.ROOT));
        ForumReport updated = forumService.reviewReport(id, st, reviewedBy);
        return ResponseEntity.ok(toReport(updated));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        forumService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    private ForumDtos.PostListItemResponse toListItem(ForumPost p) {
        long count = p.getComments() == null ? 0 : p.getComments().size();
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
                count,
                likes,
                dislikes
        );
    }

    private ForumDtos.PostDetailsResponse toDetails(ForumPost p) {
        List<ForumDtos.CommentResponse> comments = (p.getComments() == null ? List.<ForumComment>of() : p.getComments())
                .stream()
                .sorted(Comparator.comparing(ForumComment::getCreatedAt))
                .map(this::toComment)
                .toList();

        long likes = p.getReactions() == null ? 0 : p.getReactions().stream().filter(r -> r.getType() == ForumReactionType.LIKE).count();
        long dislikes = p.getReactions() == null ? 0 : p.getReactions().stream().filter(r -> r.getType() == ForumReactionType.DISLIKE).count();

        return new ForumDtos.PostDetailsResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getAuthorName(),
                p.getCategory() == null ? null : p.getCategory().name(),
                p.getCourseId(),
                p.getTags() == null ? Set.of() : p.getTags().stream().map(ForumTag::getSlug).collect(Collectors.toSet()),
                Boolean.TRUE.equals(p.getPinned()),
                p.getAcceptedCommentId(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                comments,
                likes,
                dislikes
        );
    }

    private ForumCategory parseCategory(String category) {
        if (category == null || category.isBlank()) return ForumCategory.GENERAL;
        return ForumCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
    }

    private Set<ForumTag> resolveTags(Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return Set.of();
        return slugs.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .limit(8)
                .map(slug -> tagRepository.findBySlug(slug).orElseGet(() ->
                        tagRepository.save(ForumTag.builder().slug(slug).label(slug.replace('-', ' ')).build())
                ))
                .collect(Collectors.toSet());
    }

    private ForumDtos.ReportResponse toReport(ForumReport r) {
        return new ForumDtos.ReportResponse(
                r.getId(),
                r.getTargetType() == null ? null : r.getTargetType().name(),
                r.getTargetId(),
                r.getReporterName(),
                r.getReason(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getCreatedAt(),
                r.getReviewedAt(),
                r.getReviewedBy()
        );
    }

    private ForumDtos.CommentResponse toComment(ForumComment c) {
        return new ForumDtos.CommentResponse(
                c.getId(),
                c.getContent(),
                c.getAuthorName(),
                c.getCreatedAt()
        );
    }
}

