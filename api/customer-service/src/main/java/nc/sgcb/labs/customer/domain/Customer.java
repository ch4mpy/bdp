package nc.sgcb.labs.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Customer {

  private String id;
  private final String firstName;
  private final String lastName;
  private final String email;

}
