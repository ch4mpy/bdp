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

    static Specification<MoneyTransfer> searchSpec(MoneyTransferFilteringCriteria criteria) {
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

    static Specification<MoneyTransfer> fromAccountNumberLike(String accountNumber) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.fromAccountNumber), accountNumber);
    }

    static Specification<MoneyTransfer> toAccountNumberLike(String accountNumber) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.toAccountNumber), accountNumber);
    }

    static Specification<MoneyTransfer> currencyLike(String iso3) {
        return (root, query, cb) -> cb.like(root.get(MoneyTransfer_.amount).get(Amount_.currencyIso3), iso3);
    }

    static Specification<MoneyTransfer> amountGe(Long digits) {
        return (root, query, cb) -> cb.ge(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
    }

    static Specification<MoneyTransfer> amountLe(Long digits) {
        return (root, query, cb) -> cb.le(root.get(MoneyTransfer_.amount).get(Amount_.digits), digits);
    }

    static Specification<MoneyTransfer> timestampAfter(Instant timestamp) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
    }

    static Specification<MoneyTransfer> timestampBefore(Instant timestamp) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(MoneyTransfer_.timestamp), timestamp);
    }

    static Specification<MoneyTransfer> labelLike(String labelPart) {
        return (root, query, cb) -> cb.like(cb.upper(root.get(MoneyTransfer_.amount).get(Amount_.currencyIso3)), labelPart.toUpperCase());
    }

    static Specification<MoneyTransfer> orderBytimestampDesc(Specification<MoneyTransfer> spec) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get(MoneyTransfer_.timestamp)));
            return spec.toPredicate(root, query, cb);
        };
    }
}
