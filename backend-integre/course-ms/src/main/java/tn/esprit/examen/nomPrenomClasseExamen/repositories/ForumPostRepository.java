package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumCategory;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    @Query("""
            select p from ForumPost p
            where p.deleted = false
              and (:q is null or lower(p.title) like lower(concat('%', :q, '%'))
                   or lower(p.content) like lower(concat('%', :q, '%'))
                   or lower(p.authorName) like lower(concat('%', :q, '%')))
            """)
    Page<ForumPost> searchActive(String q, Pageable pageable);

    Optional<ForumPost> findByIdAndDeletedFalse(Long id);

    long countByAuthorNameAndCreatedAtAfterAndDeletedFalse(String authorName, LocalDateTime after);

    @Query("""
            select distinct p from ForumPost p
            left join p.tags t
            where p.deleted = false
              and p.id <> :postId
              and (p.category = :category or t.slug in :tagSlugs)
            order by p.pinned desc, p.updatedAt desc
            """)
    Page<ForumPost> findSimilar(Long postId, ForumCategory category, Collection<String> tagSlugs, Pageable pageable);
}

