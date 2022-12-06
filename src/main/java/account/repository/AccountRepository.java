package account.repository;

import account.entity.Account;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    List<Account> findAll();
    boolean existsByEmail(String email);
    @Query(value = "UPDATE ACCOUNTS SET FAILED_ATTEMPT = ?1 WHERE EMAIL = ?2",
    nativeQuery = true)
    @Modifying
    @Transactional
    void updateFailedAttempts(int failedAttempt, String email);
}
