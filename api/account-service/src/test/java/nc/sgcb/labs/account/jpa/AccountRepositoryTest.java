package nc.sgcb.labs.account.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import nc.sgcb.labs.account.AccountFixtures;
import nc.sgcb.labs.account.CacheConfiguration;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;

@SpringBootTest(classes = {CacheConfiguration.class, AccountRepository.class})
@ActiveProfiles("h2")
class AccountRepositoryTest {

  @MockitoBean
  JpaAccountRepository jpaAccountRepo;

  @Autowired
  AccountRepository accountRepo;

  Map<String, Account> accountDatabase = new HashMap<>();

  Account customerXpfAccount;

  @BeforeEach
  void setUp() {
    customerXpfAccount = AccountFixtures.createCustomersXpfAccount(100000L);

    final var accounts = new ConcurrentHashMap<Iban, Account>();
    accounts.put(customerXpfAccount.getIban(), customerXpfAccount);

    when(jpaAccountRepo.existsById(any(Iban.class)))
        .thenAnswer(invocation -> accounts.containsKey(invocation.getArgument(0)));
    when(jpaAccountRepo.findById(any(Iban.class)))
        .thenAnswer(invocation -> Optional.ofNullable(accounts.get(invocation.getArgument(0))));
    when(jpaAccountRepo.findByCustomerId(anyString()))
        .thenAnswer(
            invocation -> accounts
                .values()
                .stream()
                .filter(a -> a.getCustomerId().equals(invocation.getArgument(0)))
                .toList());
    when(jpaAccountRepo.save(any(Account.class))).thenAnswer(invocation -> {
      Account account = invocation.getArgument(0);
      accounts.put(account.getIban(), account);
      return account;
    });
  }

  @Test
  @DirtiesContext
  // prevent cache operation conflict between tests
  void givenFindByIdCalledTwiceWithSameIban_whenSaveAccountWithSameIbanAndCallFindByIdAgain_thenCacheUpdatedAndFindByIdCalledOnlyOnceOverall() {
    // accountService.findById called twice, but underlying jpaAccountRepo.findById should be
    // called only once.
    var actual = accountRepo.findById(customerXpfAccount.getIban());
    var actual2 = accountRepo.findById(customerXpfAccount.getIban());
    assertEquals(100000L, actual.get().getBalance().getDigits());
    assertEquals(100000L, actual2.get().getBalance().getDigits());

    // save a new Account instance with the same iban and a different balance
    // (do not work with a reference to the instance already in the cache)
    var account = accountRepo
        .save(
            Account
                .builder()
                .iban(customerXpfAccount.getIban())
                .customerId(customerXpfAccount.getCustomerId())
                .balance(new Amount("XPF", 200000L))
                .build());
    assertEquals(200000L, account.getBalance().getDigits());

    // retrieve the account from the cache to verify that it was updated when saving the new
    // instance
    var actual3 = accountRepo.findById(customerXpfAccount.getIban());
    assertEquals(200000L, actual3.get().getBalance().getDigits());

    // only the 1st call to accountService.findById should delegate to jpaAccountRepo.findById
    // (save should @CachePut here, not @CacheEvict)
    verify(jpaAccountRepo, times(1)).findById(customerXpfAccount.getIban());
  }

  @Test
  @DirtiesContext
  // prevent cache operation conflict between tests
  void givenExistsByIdReturnedFalseBeforeSavingAccount_whenSaveAccountWithSameIbanAndCallExistsByIdAgain_thenCacheEvictedAndExistsByIdCalledOnlyTwiceOverall() {
    var customerEurAccount = AccountFixtures.createCustomersEurAccount(200000L);

    // accountService.existsById called twice, but underlying jpaAccountRepo.existsById should be
    // called only once.
    var actual = accountRepo.existsById(customerEurAccount.getIban());
    assertFalse(actual);


    // save a new Account instance with the same iban
    accountRepo.save(customerEurAccount);

    // call existsById again to verify that the cache was evicted when saving
    var actual2 = accountRepo.existsById(customerEurAccount.getIban());
    assertTrue(actual2);

    // only the 1st and 3rd calls to accountService.existsById should delegate to
    // jpaAccountRepo.existsById (save should @CacheEvict here)
    verify(jpaAccountRepo, times(2)).existsById(customerEurAccount.getIban());
  }

  @Test
  @DirtiesContext
  // prevent cache operation conflict between tests
  void givenFindByCustomerIdCalledTwiceWithSameCustomerId_whenSaveAccountWithSameCustomerIdAndCallFindByCustomerIdAgain_thenCacheEvictedAndFindByCustomerIdCalledOnlyTwiceOverall() {
    var customerEurAccount = AccountFixtures.createCustomersEurAccount(200000L);

    assertEquals(customerXpfAccount.getCustomerId(), customerEurAccount.getCustomerId());

    // accountService.findByCustomerId called twice, but underlying
    // jpaAccountRepo.findByCustomerId should be called only once.
    var actual = accountRepo.findByCustomerId(customerXpfAccount.getCustomerId());
    actual = accountRepo.findByCustomerId(customerXpfAccount.getCustomerId());
    assertEquals(1, actual.size());

    // save a new Account instance with the same customerId
    accountRepo.save(customerEurAccount);

    // retrieve the accounts from the cache to verify that it was evicted when saving the new
    // instance
    var actual3 = accountRepo.findByCustomerId(customerXpfAccount.getCustomerId());
    assertEquals(2, actual3.size());

    // only the 1st and 3rd calls to accountService.findByCustomerId should delegate to
    // jpaAccountRepo.findByCustomerId (save should @CacheEvict here)
    verify(jpaAccountRepo, times(2)).findByCustomerId(customerXpfAccount.getCustomerId());
  }

}
