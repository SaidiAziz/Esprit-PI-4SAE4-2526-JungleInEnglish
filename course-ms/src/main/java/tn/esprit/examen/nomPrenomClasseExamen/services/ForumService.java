package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumComment;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReport;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportTargetType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReactionType;

public interface ForumService {

    ForumPost createPost(ForumPost post);

    Page<ForumPost> searchPosts(String q, Pageable pageable);

    ForumPost getPostById(Long id);

    ForumPost updatePost(Long id, ForumPost update);

    void softDeletePost(Long id);

    ForumPost setPinned(Long id, boolean pinned);

    ForumComment addComment(Long postId, ForumComment comment);

    void deleteComment(Long commentId);

    ForumPost reactToPost(Long postId, String voterName, ForumReactionType type);

    ForumPost acceptAnswer(Long postId, Long commentId);

    ForumReport report(ForumReportTargetType targetType, Long targetId, String reporterName, String reason);

    Page<ForumReport> listReports(ForumReportStatus status, Pageable pageable);

    ForumReport reviewReport(Long reportId, ForumReportStatus status, String reviewedBy);
}

