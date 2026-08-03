package com.c4soft.resthero.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Amount {

  /**
   * In minor unit (i.e. 1000 for 1000 XPF, 10.00 USD, 1.000 KWD)
   */
  @Column(nullable = false)
  private int digits = 0;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Currency currency;

  public static Amount zero(Currency currency) {
    return new Amount(0, currency);
  }
}
