package exp.usermicroservice.Repositories;

import exp.usermicroservice.Entities.Role;
import exp.usermicroservice.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    java.util.Optional<User> findByEmail(String email);

  @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR LOWER(u.FirstName) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(u.LastName)  LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%')))
        AND   (:role   IS NULL OR u.role = :role)
    """)
  Page<User> searchUsers(
    @Param("search") String search,
    @Param("role") Role role,
    Pageable pageable
  );
}
