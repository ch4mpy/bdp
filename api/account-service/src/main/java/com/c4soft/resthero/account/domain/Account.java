package com.c4soft.resthero.account.domain;

import org.hibernate.envers.Audited;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Audited
@Entity
@Table(name = "accounts")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

  @Id
  // LAB:3.2:REMOVE:START
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSeq")
  @SequenceGenerator(name = "accountSeq", sequenceName = "accounts_seq", allocationSize = 1)
  // LAB:3.2:REMOVE:END
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

  /**
   * 
   * @param amount
   * @throws IllegalArgumentException if the amount currency is different from the account currency
   */
  public void credit(Amount amount) throws IllegalArgumentException {
    if (!balance.getCurrency().equals(amount.getCurrency())) {
      throw new IllegalArgumentException("Cannot credit an amount with a different currency");
    }
    balance = new Amount(balance.getDigits() + amount.getDigits(), balance.getCurrency());
  }

  /**
   * 
   * @param amount
   * @throws IllegalArgumentException if the amount currency is different from the account currency
   */
  public void debit(Amount amount) throws IllegalArgumentException {
    if (!balance.getCurrency().equals(amount.getCurrency())) {
      throw new IllegalArgumentException("Cannot debit an amount with a different currency");
    }
    balance = new Amount(balance.getDigits() - amount.getDigits(), balance.getCurrency());
  }

  public static Account create(Iban iban, String customerId, Currency currency) {
    return new Account(null, iban, customerId, Amount.zero(currency));
  }
}
