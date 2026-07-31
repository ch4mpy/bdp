package nc.sgcb.labs.account.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.jpa.IbanStringAttributeConverter;

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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSeq")
  @SequenceGenerator(name = "accountSeq", sequenceName = "accounts_seq", allocationSize = 1)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @Column(unique = true, nullable = false)
  @Convert(converter = IbanStringAttributeConverter.class)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Iban iban;

  @Column(nullable = false)
  @ToString.Include
  private String customerId;

  @Embedded
  private Amount balance;
}
