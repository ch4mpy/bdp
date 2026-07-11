package nc.sgcb.labs.account.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;

@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  private Iban iban;

  @Column(nullable = false)
  @ToString.Include
  private Long customerId;

  @Embedded
  private Amount balance;
}
