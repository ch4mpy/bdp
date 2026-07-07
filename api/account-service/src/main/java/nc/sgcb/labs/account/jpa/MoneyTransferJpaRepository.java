package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.account.domain.MoneyTransfer_;
import nc.sgcb.labs.commons.domain.Amount_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;

public interface MoneyTransferJpaRepository
        extends JpaRepository<MoneyTransfer, Long>, JpaSpecificationExecutor<MoneyTransfer> {

    default Specification<MoneyTransfer> searchSpec(MoneyTransferFilteringCriteria criteria) {
        var spec = Specification.<MoneyTransfer>unrestricted();

        if (criteria.fromAccountNumber().isPresent()) {
            spec = spec.and(fromAccountNumberLike(criteria.fromAccountNumber().get()));
        }
        if (criteria.toAccountNumber().isPresent()) {
            spec = spec.and(toAccountNumberLike(criteria.toAccountNumber().get()));
        }
        if (criteria.minAmount().isPresent()) {
            spec = spec.and(amountGe(criteria.minAmount().get()));
        }

        return orderBytimestampDesc(spec);
    }

    default Specification<MoneyTransfer> fromAccountNumberLike(String accountNumber) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.fromAccountNumber), accountNumber);
    }

    default Specification<MoneyTransfer> toAccountNumberLike(String accountNumber) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.toAccountNumber), accountNumber);
    }

    default Specification<MoneyTransfer> currencyLike(String iso3) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.amount).get(Amount_.currencyIso3), iso3);
    }

    default Specification<MoneyTransfer> amountGe(Long digits) {
        return (root, query, cb) -> cb.ge(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
    }

    default Specification<MoneyTransfer> amountLe(Long digits) {
        return (root, query, cb) -> cb.le(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
    }

    default Specification<MoneyTransfer> timestampAfter(Instant timestamp) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
    }

    default Specification<MoneyTransfer> timestampBefore(Instant timestamp) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
    }

    default Specification<MoneyTransfer> orderBytimestampDesc(Specification<MoneyTransfer> spec) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get(MoneyTransfer_.timestamp)));
            return spec.toPredicate(root, query, cb);
        };
    }
}
