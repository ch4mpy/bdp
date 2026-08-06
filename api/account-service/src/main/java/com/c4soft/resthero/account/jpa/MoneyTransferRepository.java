package com.c4soft.resthero.account.jpa;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.c4soft.resthero.account.domain.MoneyTransfer;
import com.c4soft.resthero.account.domain.MoneyTransferFilteringCriteria;
import com.c4soft.resthero.account.domain.MoneyTransfer_;
import com.c4soft.resthero.commons.domain.Amount_;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.domain.Iban;

/**
 * 
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public interface MoneyTransferRepository
  extends JpaRepository<MoneyTransfer, Long>
  // LAB:3.7:REMOVE:START
  , JpaSpecificationExecutor<MoneyTransfer>
  // LAB:3.7:REMOVE:END
{

  static Specification<MoneyTransfer> searchSpec(MoneyTransferFilteringCriteria criteria) {
    var spec = Specification.<MoneyTransfer>unrestricted();

    if (criteria.sourceIban() != null) {
      spec = spec.and(sourceAccountNumberLike(criteria.sourceIban()));
    }
    if (criteria.destinationIban() != null) {
      spec = spec.and(destinationAccountNumberLike(criteria.destinationIban()));
    }
    if (criteria.minAmount() != null) {
      spec = spec.and(amountGe(criteria.minAmount()));
    }
    if (criteria.maxAmount() != null) {
      spec = spec.and(amountLe(criteria.maxAmount()));
    }
    if (criteria.currency() != null) {
      spec = spec.and(currencyLike(criteria.currency()));
    }
    if (criteria.timestampBefore() != null) {
      spec = spec.and(timestampBefore(criteria.timestampBefore()));
    }
    if (criteria.timestampAfter() != null) {
      spec = spec.and(timestampAfter(criteria.timestampAfter()));
    }
    if (criteria.labelContaining() != null) {
      spec = spec.and(labelLike(criteria.labelContaining()));
    }

    return orderBytimestampDesc(spec);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> sourceAccountNumberLike(Iban iban) {
    return (root, query, cb) -> cb.equal(root.get(MoneyTransfer_.sourceIban), iban);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> destinationAccountNumberLike(Iban iban) {
    return (root, query, cb) -> cb.equal(root.get(MoneyTransfer_.destinationIban), iban);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> currencyLike(Currency currency) {
    return (root, query, cb) -> cb
        .equal(root.get(MoneyTransfer_.amount).get(Amount_.currency), currency);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> amountGe(Integer digits) {
    return (root, query, cb) -> cb.ge(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> amountLe(Integer digits) {
    return (root, query, cb) -> cb.le(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
  }

  @SuppressWarnings({"unused"})
  private static Specification<MoneyTransfer> timestampAfter(Instant timestamp) {
    return (root, query, cb) -> cb
        .greaterThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
  }

  @SuppressWarnings({"unused"})
  private static Specification<MoneyTransfer> timestampBefore(Instant timestamp) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> labelLike(String labelPart) {
    return (root, query, cb) -> cb
        .like(
            cb.upper(root.get(MoneyTransfer_.label)),
            "%%%s%%".formatted(labelPart.toUpperCase()));
  }

  private static Specification<MoneyTransfer> orderBytimestampDesc(
      Specification<MoneyTransfer> spec) {
    return (root, query, cb) -> {
      query.orderBy(cb.desc(root.get(MoneyTransfer_.timestamp)));
      return spec.toPredicate(root, query, cb);
    };
  }
}
