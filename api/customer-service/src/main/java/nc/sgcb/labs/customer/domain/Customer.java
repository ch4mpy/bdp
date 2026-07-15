package nc.sgcb.labs.customer.domain;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "customers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Customer {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  private String id;

  @Column(nullable = false, name = "firstname")
  @ToString.Include
  private String firstName;

  @Column(nullable = false, name = "lastname")
  @ToString.Include
  private String lastName;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String birthLocation;

  @Column(nullable = false)
  private String email;

}
