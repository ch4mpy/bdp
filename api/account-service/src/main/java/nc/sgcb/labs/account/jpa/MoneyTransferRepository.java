package nc.sgcb.labs.account.jpa;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.account.domain.MoneyTransfer_;
import nc.sgcb.labs.commons.domain.Amount_;
import nc.sgcb.labs.commons.domain.Iban;

/**
 * 
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public interface MoneyTransferRepository
    extends JpaRepository<MoneyTransfer, Long>, JpaSpecificationExecutor<MoneyTransfer> {

  static Specification<MoneyTransfer> searchSpec(MoneyTransferFilteringCriteria criteria) {
    var spec = Specification.<MoneyTransfer>unrestricted();

    if (criteria.sourceIban().isPresent()) {
      spec = spec.and(sourceAccountNumberLike(criteria.sourceIban().get()));
    }
    if (criteria.destinationIban().isPresent()) {
      spec = spec.and(destinationAccountNumberLike(criteria.destinationIban().get()));
    }
    if (criteria.minAmount().isPresent()) {
      spec = spec.and(amountGe(criteria.minAmount().get()));
    }
    if (criteria.maxAmount().isPresent()) {
      spec = spec.and(amountLe(criteria.maxAmount().get()));
    }
    if (criteria.currencyIso3().isPresent()) {
      spec = spec.and(currencyLike(criteria.currencyIso3().get()));
    }
    if (criteria.timestampBefore().isPresent()) {
      spec = spec.and(timestampBefore(criteria.timestampBefore().get()));
    }
    if (criteria.timestampAfter().isPresent()) {
      spec = spec.and(timestampAfter(criteria.timestampAfter().get()));
    }
    if (criteria.labelContaining().isPresent()) {
      spec = spec.and(labelLike(criteria.labelContaining().get()));
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
  private static Specification<MoneyTransfer> currencyLike(String iso3) {
    return (root, query, cb) -> cb
        .like(root.get(MoneyTransfer_.amount).get(Amount_.currencyIso3), iso3);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> amountGe(Long digits) {
    return (root, query, cb) -> cb.ge(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
  }

  @SuppressWarnings("unused")
  private static Specification<MoneyTransfer> amountLe(Long digits) {
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
