package com.c4soft.resthero.card.domain;

import org.hibernate.envers.Audited;
import com.c4soft.resthero.commons.domain.Iban;
import com.c4soft.resthero.commons.jpa.IbanStringAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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

@Audited
@Entity
@Table(name = "cards")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Card {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  private String number;

  @Column(nullable = false)
  @ToString.Include
  @Convert(converter = IbanStringAttributeConverter.class)
  private Iban iban;

  @Embedded
  private Ceilings ceilings;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = false;


  @Embeddable
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @AllArgsConstructor
  public static class Ceilings {

    @Column(name = "transaction_ceiling", nullable = false)
    private Integer transaction;

    @Column(name = "rolling30_ceiling", nullable = false)
    private Integer rolling30;
  }
}
