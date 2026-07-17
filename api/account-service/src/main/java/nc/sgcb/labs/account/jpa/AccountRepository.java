package nc.sgcb.labs.account.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Iban;

/**
 * Caching decorator for {@link AccountJpaRepository} to avoid hitting the database too often.
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Repository
@RequiredArgsConstructor
@CacheConfig(cacheNames = {AccountRepository.ACCOUNT_EXISTS_BY_IBAN_CACHE,
    AccountRepository.ACCOUNTS_BY_IBAN_CACHE,
    AccountRepository.ACCOUNTS_BY_CUSTOMER_ID_CACHE})
public class AccountRepository {
  static final String ACCOUNT_EXISTS_BY_IBAN_CACHE = "accountExistsByIban";
  static final String ACCOUNTS_BY_IBAN_CACHE = "accountsByIban";
  static final String ACCOUNTS_BY_CUSTOMER_ID_CACHE = "accountsByCustomerId";

  private final AccountJpaRepository jpaRepository;

  @Cacheable(cacheNames = ACCOUNT_EXISTS_BY_IBAN_CACHE, key = "#iban")
  public boolean existsById(Iban iban) {
    return jpaRepository.existsById(iban);
  }

  @Cacheable(cacheNames = ACCOUNTS_BY_IBAN_CACHE, key = "#iban")
  public Optional<Account> findById(Iban iban) {
    return jpaRepository.findById(iban);
  }

  @Cacheable(cacheNames = ACCOUNTS_BY_CUSTOMER_ID_CACHE, key = "#customerId")
  public List<Account> findByCustomerId(String customerId) {
    return jpaRepository.findByCustomerId(customerId);
  }

  @Caching(put = @CachePut(cacheNames = ACCOUNTS_BY_IBAN_CACHE, key = "#account.iban"),
      evict = {@CacheEvict(cacheNames = ACCOUNT_EXISTS_BY_IBAN_CACHE, key = "#account.iban"),
          @CacheEvict(cacheNames = ACCOUNTS_BY_CUSTOMER_ID_CACHE, key = "#account.customerId")})
  public Account save(Account account) {
    return jpaRepository.save(account);
  }
}
