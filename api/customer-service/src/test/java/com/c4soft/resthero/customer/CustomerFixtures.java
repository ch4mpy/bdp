package com.c4soft.resthero.customer;

import java.util.UUID;
import com.c4soft.resthero.customer.domain.Customer;

public class CustomerFixtures {

  public static Customer createJeanBonot() {
    return new Customer(UUID.randomUUID().toString(), "Jean", "Bonot", "jean.bonot@test.pf");
  }

  public static Customer createJohnDeuf() {
    return new Customer("john-deuf-subject", "John", "Deuf", "john.deuf@test.pf");
  }

  public static Customer createJefHini() {
    return new Customer(UUID.randomUUID().toString(), "Jef", "Hini", "jef.ini@test.pf");
  }

}
