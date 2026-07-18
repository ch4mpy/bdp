package nc.sgcb.labs.customer.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import nc.sgcb.labs.customer.CustomerFixtures;
import nc.sgcb.labs.customer.domain.Customer;

@DataJpaTest
@ActiveProfiles("h2")
class CustomerRepositoryTest {

  @Autowired
  private CustomerRepository customerJpaRepository;

  Customer jefHini, johnDoeuf, jeanBonnot;

  @BeforeEach
  void setUp() {
    jefHini = customerJpaRepository.save(CustomerFixtures.createJefHini());
    johnDoeuf = customerJpaRepository.save(CustomerFixtures.createJohnDeuf());
    jeanBonnot = customerJpaRepository.save(CustomerFixtures.createJeanBonot());
  }

  @Test
  void givenTwoCustomersWithNeedleInFirstNameAndAnotherInLastName_whenFindByFirstOrLastNameContainingIgnoreCase_thenAll3Returned() {
    final var actual =
        customerJpaRepository.findByFirstOrLastNameContainingIgnoreCase("e", PageRequest.of(0, 10));
    assertThat(actual).hasSize(3);
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Jean", c.getFirstName())));
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Jef", c.getFirstName())));
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Deuf", c.getLastName())));
  }

  @Test
  void givenOnlyOneCustomersWithNeedleInForstOrLastName_whenFindByFirstOrLastNameContainingIgnoreCase_thenOnlyItReturned() {
    final var actual = customerJpaRepository
        .findByFirstOrLastNameContainingIgnoreCase("hin", PageRequest.of(0, 10));
    assertThat(actual).hasSize(1);
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Hini", c.getLastName())));
  }

  @Test
  void givenNoNeedleMatch_whenFindByFirstOrLastNameContainingIgnoreCase_thenEmptyPage() {
    final var actual = customerJpaRepository
        .findByFirstOrLastNameContainingIgnoreCase("zzz", PageRequest.of(0, 10));
    assertThat(actual).isEmpty();
  }

  @Test
  void givenSmallPageSize_whenFindByFirstOrLastNameContainingIgnoreCase_thenResultIsPaginated() {
    final var actual =
        customerJpaRepository.findByFirstOrLastNameContainingIgnoreCase("e", PageRequest.of(0, 2));
    assertThat(actual.getContent()).hasSize(2);
    assertThat(actual.getTotalElements()).isEqualTo(3);
    assertThat(actual.getTotalPages()).isEqualTo(2);
  }

  @Test
  void givenNeedleOnlyMatchesFirstName_whenFindByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_thenOnlyItReturned() {
    final var actual = customerJpaRepository
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "jean",
            "no-match",
            PageRequest.of(0, 10));
    assertThat(actual).hasSize(1);
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Jean", c.getFirstName())));
  }

  @Test
  void givenNeedleOnlyMatchesLastName_whenFindByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_thenOnlyItReturned() {
    final var actual = customerJpaRepository
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "no-match",
            "deuf",
            PageRequest.of(0, 10));
    assertThat(actual).hasSize(1);
    assertTrue(actual.stream().anyMatch(c -> Objects.equals("Deuf", c.getLastName())));
  }

  @Test
  void givenNeitherNeedleMatches_whenFindByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_thenEmptyPage() {
    final var actual = customerJpaRepository
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "no-match",
            "no-match",
            PageRequest.of(0, 10));
    assertThat(actual).isEmpty();
  }

  @Test
  void givenExistingCustomerWithMatchingFieldsAnyCase_whenExistsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase_thenTrue() {
    final var actual = customerJpaRepository
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            jefHini.getFirstName().toUpperCase(),
            jefHini.getLastName().toLowerCase(),
            jefHini.getBirthDate(),
            jefHini.getBirthLocation().toUpperCase());
    assertTrue(actual);
  }

  @Test
  void givenNoCustomerMatchesFirstName_whenExistsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase_thenFalse() {
    final var actual = customerJpaRepository
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            "Nobody",
            jefHini.getLastName(),
            jefHini.getBirthDate(),
            jefHini.getBirthLocation());
    assertThat(actual).isFalse();
  }

  @Test
  void givenBirthDateDiffers_whenExistsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase_thenFalse() {
    final var actual = customerJpaRepository
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            jefHini.getFirstName(),
            jefHini.getLastName(),
            jefHini.getBirthDate().plusDays(1),
            jefHini.getBirthLocation());
    assertThat(actual).isFalse();
  }

  @Test
  void givenBirthLocationDiffers_whenExistsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase_thenFalse() {
    final var actual = customerJpaRepository
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            jefHini.getFirstName(),
            jefHini.getLastName(),
            jefHini.getBirthDate(),
            "Somewhere else");
    assertThat(actual).isFalse();
  }
}
