package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Iban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Package-private JPA repository for {@link Account} entities, used by {@link AccountRepository}.
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
interface JpaAccountRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByIban(Iban iban);

  List<Account> findByCustomerId(String customerId);

  boolean existsByIban(Iban iban);
}
