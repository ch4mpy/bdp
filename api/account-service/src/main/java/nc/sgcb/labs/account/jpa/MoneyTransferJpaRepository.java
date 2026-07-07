package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.MoneyTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MoneyTransferJpaRepository
    extends JpaRepository<MoneyTransfer, Long>, JpaSpecificationExecutor<MoneyTransfer> {
}
