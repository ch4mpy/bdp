package nc.sgcb.labs.account.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Iban;

/**
 * Package-private JPA repository for {@link Account} entities, used by {@link AccountRepository}.
 * 
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
interface JpaAccountRepository extends JpaRepository<Account, Iban> {

  List<Account> findByCustomerId(String customerId);

}
