package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReport;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportTargetType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumComment;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPostReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReactionType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumUserStats;
import tn.esprit.examen.nomPrenomClasseExamen.exceptions.ForumCommentNotFoundException;
import tn.esprit.examen.nomPrenomClasseExamen.exceptions.ForumPostNotFoundException;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumCommentRepository;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumPostRepository;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumPostReactionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumReportRepository;
import tn.esprit.examen.nomPrenomClasseExamen.repositories.ForumUserStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ForumServiceImpl implements ForumService {

    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumPostReactionRepository reactionRepository;
    private final ForumReportRepository reportRepository;
    private final ForumUserStatsRepository userStatsRepository;
    private final ForumTextModerationService moderationService;

    @Override
    public ForumPost createPost(ForumPost post) {
        // rate limit: max 5 posts/day per author
        String author = post.getAuthorName() == null ? "" : post.getAuthorName().trim();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = postRepository.countByAuthorNameAndCreatedAtAfterAndDeletedFalse(author, startOfDay);
        if (todayCount >= 5) {
            throw new IllegalArgumentException("Daily post limit reached for authorName=" + author);
        }

        if (post.getPinned() == null) post.setPinned(Boolean.FALSE);
        if (post.getDeleted() == null) post.setDeleted(Boolean.FALSE);
        post.setTitle(moderationService.censor(post.getTitle()));
        post.setContent(moderationService.censor(post.getContent()));

        ForumPost saved = postRepository.save(post);

        // points: +2 post
        userStatsRepository.findByAuthorName(author).ifPresentOrElse(stats -> {
            stats.setPostsCount(stats.getPostsCount() + 1);
            stats.setPoints(stats.getPoints() + 2);
            userStatsRepository.save(stats);
        }, () -> userStatsRepository.save(ForumUserStats.builder()
                .authorName(author)
                .points(2L)
                .postsCount(1L)
                .commentsCount(0L)
                .build()));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForumPost> searchPosts(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return postRepository.searchActive(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ForumPost getPostById(Long id) {
        return postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ForumPostNotFoundException(id));
    }

    @Override
    public ForumPost updatePost(Long id, ForumPost update) {
        ForumPost existing = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ForumPostNotFoundException(id));

        existing.setTitle(moderationService.censor(update.getTitle()));
        existing.setContent(moderationService.censor(update.getContent()));
        existing.setAuthorName(update.getAuthorName());
        return postRepository.save(existing);
    }

    @Override
    public void softDeletePost(Long id) {
        ForumPost existing = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ForumPostNotFoundException(id));
        existing.setDeleted(Boolean.TRUE);
        postRepository.save(existing);
    }

    @Override
    public ForumPost setPinned(Long id, boolean pinned) {
        ForumPost existing = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ForumPostNotFoundException(id));
        existing.setPinned(pinned);
        return postRepository.save(existing);
    }

    @Override
    public ForumComment addComment(Long postId, ForumComment comment) {
        ForumPost post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ForumPostNotFoundException(postId));
        // rate limit: max 15 comments/day per author
        String author = comment.getAuthorName() == null ? "" : comment.getAuthorName().trim();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = commentRepository.countByAuthorNameAndCreatedAtAfter(author, startOfDay);
        if (todayCount >= 15) {
            throw new IllegalArgumentException("Daily comment limit reached for authorName=" + author);
        }

        comment.setContent(moderationService.censor(comment.getContent()));
        comment.setPost(post);
        ForumComment saved = commentRepository.save(comment);

        // points: +1 comment
        userStatsRepository.findByAuthorName(author).ifPresentOrElse(stats -> {
            stats.setCommentsCount(stats.getCommentsCount() + 1);
            stats.setPoints(stats.getPoints() + 1);
            userStatsRepository.save(stats);
        }, () -> userStatsRepository.save(ForumUserStats.builder()
                .authorName(author)
                .points(1L)
                .postsCount(0L)
                .commentsCount(1L)
                .build()));

        return saved;
    }

    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ForumCommentNotFoundException(commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public ForumPost reactToPost(Long postId, String voterName, ForumReactionType type) {
        ForumPost post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ForumPostNotFoundException(postId));

        String vn = voterName == null ? "" : voterName.trim();
        if (vn.isBlank()) {
            throw new IllegalArgumentException("voterName is required");
        }

        ForumPostReaction existing = reactionRepository.findByPostIdAndVoterName(postId, vn).orElse(null);
        if (existing == null) {
            reactionRepository.save(ForumPostReaction.builder()
                    .post(post)
                    .voterName(vn)
                    .type(type)
                    .build());
        } else {
            if (existing.getType() == type) {
                // toggle off if same reaction clicked again
                reactionRepository.delete(existing);
            } else {
                existing.setType(type);
                reactionRepository.save(existing);
            }
        }

        return postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ForumPostNotFoundException(postId));
    }

    @Override
    public ForumPost acceptAnswer(Long postId, Long commentId) {
        ForumPost post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ForumPostNotFoundException(postId));
        ForumComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ForumCommentNotFoundException(commentId));
        if (comment.getPost() == null || !post.getId().equals(comment.getPost().getId())) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }
        post.setAcceptedCommentId(commentId);
        return postRepository.save(post);
    }

    @Override
    public ForumReport report(ForumReportTargetType targetType, Long targetId, String reporterName, String reason) {
        ForumReport r = ForumReport.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporterName(reporterName == null ? "" : reporterName.trim())
                .reason(moderationService.censor(reason))
                .status(ForumReportStatus.PENDING)
                .build();
        return reportRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForumReport> listReports(ForumReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable);
    }

    @Override
    public ForumReport reviewReport(Long reportId, ForumReportStatus status, String reviewedBy) {
        ForumReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(status);
        report.setReviewedBy(reviewedBy);
        report.setReviewedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
}

