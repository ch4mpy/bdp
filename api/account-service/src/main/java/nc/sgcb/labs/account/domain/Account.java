package nc.sgcb.labs.account.domain;

import jakarta.persistence.*;
import lombok.*;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.jpa.IbanStringAttributeConverter;

import java.io.Serializable;

@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(Account.Pk.class)
public class Account {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  private Iban iban;

  @Column(nullable = false)
  @ToString.Include
  private String customerId;

  @Embedded
  private Amount balance;


  @Data
  static class Pk implements Serializable {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Convert(converter = IbanStringAttributeConverter.class)
    private Iban iban;
  }
}
