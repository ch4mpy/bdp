package nc.sgcb.labs.card.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Iban;

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
  private Iban iban;

  @Embedded
  private Ceilings ceilings;


  @Embeddable
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @AllArgsConstructor
  public static class Ceilings {

    @Column(name = "transaction_ceiling", nullable = false)
    private Long transaction;

    @Column(name = "rolling30_ceiling", nullable = false)
    private Long rolling30;
  }
}
