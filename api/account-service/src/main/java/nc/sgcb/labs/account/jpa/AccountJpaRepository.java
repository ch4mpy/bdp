package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {
}
