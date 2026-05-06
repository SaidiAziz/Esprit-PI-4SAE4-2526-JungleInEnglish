package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ForumDtos {

    public record CreatePostRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 4000) String content,
            @NotBlank @Size(max = 80) String authorName,
            String category,
            Long courseId,
            Set<String> tags
    ) {}

    public record UpdatePostRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 4000) String content,
            @NotBlank @Size(max = 80) String authorName,
            String category,
            Long courseId,
            Set<String> tags
    ) {}

    public record CreateCommentRequest(
            @NotBlank @Size(max = 1500) String content,
            @NotBlank @Size(max = 80) String authorName
    ) {}

    public enum ReactionType {
        LIKE,
        DISLIKE
    }

    public record ReactToPostRequest(
            @NotBlank @Size(max = 80) String voterName,
            ReactionType type
    ) {}

    public record CommentResponse(
            Long id,
            String content,
            String authorName,
            LocalDateTime createdAt
    ) {}

    public record PostListItemResponse(
            Long id,
            String title,
            String authorName,
            String category,
            Long courseId,
            Set<String> tags,
            boolean pinned,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long commentsCount,
            long likesCount,
            long dislikesCount
    ) {}

    public record PostDetailsResponse(
            Long id,
            String title,
            String content,
            String authorName,
            String category,
            Long courseId,
            Set<String> tags,
            boolean pinned,
            Long acceptedCommentId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<CommentResponse> comments,
            long likesCount,
            long dislikesCount
    ) {}

    public record AcceptAnswerRequest(Long commentId) {}

    public record ReportRequest(
            @NotBlank @Size(max = 80) String reporterName,
            @NotBlank @Size(max = 300) String reason
    ) {}

    public record ReportResponse(
            Long id,
            String targetType,
            Long targetId,
            String reporterName,
            String reason,
            String status,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt,
            String reviewedBy
    ) {}

    public record CourseSuggestion(
            Long id,
            String title,
            String level,
            Integer durationHours,
            Double price
    ) {}

    public record RecommendationsResponse(
            List<PostListItemResponse> similarPosts,
            List<CourseSuggestion> suggestedCourses
    ) {}
}

