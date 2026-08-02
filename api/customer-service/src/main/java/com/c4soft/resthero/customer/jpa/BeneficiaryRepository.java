package com.c4soft.resthero.customer.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.resthero.customer.domain.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

  List<Beneficiary> findByCustomerId(String userId);
}
