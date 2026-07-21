package nc.sgcb.labs.customer.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.customer.domain.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
}
