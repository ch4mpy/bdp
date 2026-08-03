package com.c4soft.resthero.account.domain;

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
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Audited
@Entity
@Table(name = "transfers")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MoneyTransfer {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transferSeq")
  @SequenceGenerator(name = "transferSeq", sequenceName = "transfers_seq", allocationSize = 1)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @Column(nullable = false)
  @ToString.Include
  @Convert(converter = IbanStringAttributeConverter.class)
  private Iban sourceIban;

  @Column(nullable = false)
  @ToString.Include
  @Convert(converter = IbanStringAttributeConverter.class)
  private Iban destinationIban;

  @Embedded
  private Amount amount;

  @Column(nullable = false)
  @Builder.Default
  private Instant timestamp = Instant.now();

  private String label;

  public static MoneyTransfer of(
      Iban sourceIban,
      Iban destinationIban,
      Amount amount,
      String label) {
    return MoneyTransfer
        .builder()
        .sourceIban(sourceIban)
        .destinationIban(destinationIban)
        .amount(amount)
        .label(label)
        .build();
  }
}
