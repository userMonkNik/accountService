package account.repository;

import account.entity.SecurityLogs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityLogsRepository extends CrudRepository<SecurityLogs, Long> {
}
