package com.c4soft.resthero.commons.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.c4soft.resthero.commons.validation.SupportedCurrency.SupportedCurrencyConstraintValidator;

class SupportedCurrencyTest {

  SupportedCurrencyConstraintValidator validator = new SupportedCurrencyConstraintValidator();

  @Test
  void givenValueIsNull_whenIsValid_thenTrue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void givenValueIsThreeUppercaseLetters_whenIsValid_thenTrue() {
    assertTrue(validator.isValid("XPF", null));
    assertTrue(validator.isValid("EUR", null));
    assertTrue(validator.isValid("USD", null));
  }

  @Test
  void givenValueHasLowercaseLetters_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("xpf", null));
    assertFalse(validator.isValid("Eur", null));
  }

  @Test
  void givenValueHasWrongLength_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("EU", null));
    assertFalse(validator.isValid("EURO", null));
    assertFalse(validator.isValid("", null));
  }

  @Test
  void givenValueContainsNonLetters_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("EU1", null));
    assertFalse(validator.isValid("E-R", null));
  }

}
