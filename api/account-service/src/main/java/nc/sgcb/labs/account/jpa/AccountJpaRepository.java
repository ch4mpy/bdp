package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Iban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountJpaRepository extends JpaRepository<Account, Iban> {

  List<Account> findByCustomerId(Long customerId);

}
