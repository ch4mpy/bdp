package com.c4soft.resthero.card.domain;

import java.time.Instant;
import org.hibernate.envers.Audited;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Iban;
import com.c4soft.resthero.commons.jpa.IbanStringAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Audited
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

  // LAB:3.3:REMOVE:START
  @ManyToOne
  @JoinColumn(name = "card_number", nullable = false, updatable = false)
  // LAB:3.3:REMOVE:END
  private Card card;

  @Column(nullable = false)
  @Convert(converter = IbanStringAttributeConverter.class)
  private Iban destinationIban;

  @Column(nullable = false)
  @Builder.Default
  private boolean accepted = false;
}
