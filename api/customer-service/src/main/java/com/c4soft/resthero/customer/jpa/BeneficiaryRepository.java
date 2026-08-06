package com.c4soft.resthero.customer.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.resthero.customer.domain.Beneficiary;

public interface BeneficiaryRepository
  // LAB:3.5:REMOVE:START
  extends JpaRepository<Beneficiary, Long>
  // LAB:3.5:REMOVE:END
{

  List<Beneficiary> findByCustomerId(String userId);
}
