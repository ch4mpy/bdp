package nc.sgcb.labs.customer;

import java.time.LocalDate;
import java.util.UUID;
import nc.sgcb.labs.customer.domain.Customer;

public class CustomerFixtures {

  public static Customer createJeanBonot() {
    return Customer
        .builder()
        .id(UUID.randomUUID().toString())
        .firstName("Jean")
        .lastName("Bonot")
        .birthDate(LocalDate.of(1978, 10, 31))
        .birthLocation("Longjumeau (91)")
        .email("jean.bonot@test.pf")
        .build();
  }

  public static Customer createJohnDeuf() {
    return Customer
        .builder()
        .id("john-deuf-subject")
        .firstName("John")
        .lastName("Deuf")
        .birthDate(LocalDate.of(1980, 10, 29))
        .birthLocation("Aix-en-Provence (13)")
        .email("john.deuf@test.pf")
        .build();
  }

  public static Customer createJefHini() {
    return Customer
        .builder()
        .id(UUID.randomUUID().toString())
        .firstName("Jef")
        .lastName("Hini")
        .birthDate(LocalDate.of(1985, 11, 23))
        .birthLocation("St Mandé (94)")
        .email("jef.ini@test.pf")
        .build();
  }

}
