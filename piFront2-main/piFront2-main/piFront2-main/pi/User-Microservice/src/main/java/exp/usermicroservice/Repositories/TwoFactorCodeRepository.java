package exp.usermicroservice.Repositories;

import exp.usermicroservice.Entities.TwoFactorCode;
import exp.usermicroservice.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByUser(User user);
    void deleteByUser(User user);
}

