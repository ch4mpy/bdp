package nc.sgcb.labs.customer.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.customer.domain.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

  List<Beneficiary> findByCustomerId(String userId);
}
