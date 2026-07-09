package nc.sgcb.labs.customer.jpa;

import nc.sgcb.labs.customer.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("h2")
class CustomerJpaRepositoryTest {

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    Customer customer1, customer2, customer3;

    @BeforeEach
    void setUp() {
        customer1 = customerJpaRepository.save(Customer.builder()
                .firstName("Jean")
                .lastName("Bonot")
                .birthDate(LocalDate.of(1978, 10, 31))
                .birthLocation("Longjumeau (91)")
                .build());
        customer2 = customerJpaRepository.save(Customer.builder()
                .firstName("John")
                .lastName("Deuf")
                .birthDate(LocalDate.of(1980, 10, 29))
                .birthLocation("Aix-en-Provence (13)")
                .build());
        customer3 = customerJpaRepository.save(Customer.builder()
                .firstName("Jef")
                .lastName("Hini")
                .birthDate(LocalDate.of(1985, 11, 23))
                .birthLocation("St Mandé (94)")
                .build());
    }

    @Test
    void givenTwoCustomersWithNeedleInFirstNameAndAnotherInLastName_whenFindByFirstOrLastNameContainingIgnoreCase_thenAll3Returned() {
        final var actual = customerJpaRepository.findByFirstOrLastNameContainingIgnoreCase("e");
        assertThat(actual).hasSize(3);
        assertTrue(actual.stream().anyMatch(c -> Objects.equals("Jean", c.getFirstName())));
        assertTrue(actual.stream().anyMatch(c -> Objects.equals("Jef", c.getFirstName())));
        assertTrue(actual.stream().anyMatch(c -> Objects.equals("Deuf", c.getLastName())));
    }

    @Test
    void givenOnlyOneCustomersWithNeedleInForstOrLastName_whenFindByFirstOrLastNameContainingIgnoreCase_thenOnlyItReturned() {
        final var actual = customerJpaRepository.findByFirstOrLastNameContainingIgnoreCase("hin");
        assertThat(actual).hasSize(1);
        assertTrue(actual.stream().anyMatch(c -> Objects.equals("Hini", c.getLastName())));
    }
}
