package nc.sgcb.labs.card.payment.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.jpa.IbanStringAttributeConverter;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CardPayment {

  @Id
  @GeneratedValue(generator = "cardPaymentSeq")
  @SequenceGenerator(name = "cardPaymentSeq", sequenceName = "payment_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  @Builder.Default
  private Instant timestamp = Instant.now();

  @Embedded()
  private Amount amount;

  @ManyToOne
  @JoinColumn(name = "card_number", nullable = false, updatable = false)
  private Card card;

  @Column(nullable = false)
  @Convert(converter = IbanStringAttributeConverter.class)
  private Iban destinationIban;

  @Column(nullable = false)
  @Builder.Default
  private boolean accepted = false;
}
