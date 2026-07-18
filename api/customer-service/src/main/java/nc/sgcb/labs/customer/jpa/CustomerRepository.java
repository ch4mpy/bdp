package nc.sgcb.labs.customer.jpa;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.customer.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {
  Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
      String firstNamePart,
      String lastNamePart,
      Pageable pageable);

  default Page<Customer> findByFirstOrLastNameContainingIgnoreCase(
      String firstOrLastNamePart,
      Pageable pageable) {
    return findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        firstOrLastNamePart,
        firstOrLastNamePart,
        pageable);
  }

  boolean existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
      String firstName,
      String lastName,
      LocalDate birthDate,
      String birthLocation);
}
