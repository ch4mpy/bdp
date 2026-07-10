package nc.sgcb.labs.account.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Iban;

public interface AccountJpaRepository extends JpaRepository<Account, Iban> {

  List<Account> findByCustomerId(Long customerId);

}
