package nc.sgcb.labs.account.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;

import java.time.Instant;

@Entity
@Table(name = "transfers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MoneyTransfer {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transferSeq")
  @SequenceGenerator(name = "transferSeq", sequenceName = "transfers_seq", allocationSize = 1)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long number;

  @Column(nullable = false)
  @ToString.Include
  private Iban fromIban;

  @Column(nullable = false)
  @ToString.Include
  private Iban toIban;

  @Embedded
  private Amount amount;

  @Column(nullable = false)
  @Builder.Default
  private Instant timestamp = Instant.now();

  private String label;
}
