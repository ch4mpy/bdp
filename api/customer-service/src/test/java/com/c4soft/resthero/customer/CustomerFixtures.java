package com.c4soft.resthero.customer;

import java.util.UUID;
import com.c4soft.resthero.customer.domain.Customer;

public class CustomerFixtures {

  public static Customer createJeanBonot() {
    return Customer
        .builder()
        .id(UUID.randomUUID().toString())
        .firstName("Jean")
        .lastName("Bonot")
        .email("jean.bonot@test.pf")
        .build();
  }

  public static Customer createJohnDeuf() {
    return Customer
        .builder()
        .id("john-deuf-subject")
        .firstName("John")
        .lastName("Deuf")
        .email("john.deuf@test.pf")
        .build();
  }

  public static Customer createJefHini() {
    return Customer
        .builder()
        .id(UUID.randomUUID().toString())
        .firstName("Jef")
        .lastName("Hini")
        .email("jef.ini@test.pf")
        .build();
  }

}
