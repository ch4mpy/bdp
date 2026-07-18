package nc.sgcb.labs.commons.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintValidatorContext;
import nc.sgcb.labs.commons.domain.Period;
import nc.sgcb.labs.commons.validation.ValidPeriod.ValidPeriodConstraintValidator;

class ValidPeriodTest {

  ValidPeriodConstraintValidator validator;

  ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new ValidPeriodConstraintValidator();
    context = mock(ConstraintValidatorContext.class);
  }

  @Test
  void givenValueIsNull_whenIsValid_thenTrue() {
    validator.initialize(annotation(false, false, 0));
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void givenNoBoundIsRequired_whenPeriodHasNoBound_thenValid() {
    validator.initialize(annotation(false, false, 0));
    assertTrue(validator.isValid(new Period(null, null), context));
  }

  @Test
  void givenFromRequired_whenFromIsNull_thenInvalid() {
    validator.initialize(annotation(true, false, 0));
    assertFalse(validator.isValid(new Period(null, Instant.now()), context));
  }

  @Test
  void givenFromRequired_whenFromIsNotNull_thenValid() {
    validator.initialize(annotation(true, false, 0));
    assertTrue(validator.isValid(new Period(Instant.now(), null), context));
  }

  @Test
  void givenToRequired_whenToIsNull_thenInvalid() {
    validator.initialize(annotation(false, true, 0));
    assertFalse(validator.isValid(new Period(Instant.now(), null), context));
  }

  @Test
  void givenToRequired_whenToIsNotNull_thenValid() {
    validator.initialize(annotation(false, true, 0));
    assertTrue(validator.isValid(new Period(null, Instant.now()), context));
  }

  @Test
  void givenMaxSeconds_whenDurationExceedsMax_thenInvalid() {
    validator.initialize(annotation(false, false, 60));
    var from = Instant.now();
    var to = from.plus(61, ChronoUnit.SECONDS);
    assertFalse(validator.isValid(new Period(from, to), context));
  }

  @Test
  void givenMaxSeconds_whenDurationIsExactlyMax_thenValid() {
    validator.initialize(annotation(false, false, 60));
    var from = Instant.now();
    var to = from.plus(60, ChronoUnit.SECONDS);
    assertTrue(validator.isValid(new Period(from, to), context));
  }

  @Test
  void givenMaxSeconds_whenFromIsMissing_thenInvalid() {
    validator.initialize(annotation(false, false, 60));
    assertFalse(validator.isValid(new Period(null, Instant.now()), context));
  }

  @Test
  void givenMaxSeconds_whenToIsMissing_thenInvalid() {
    validator.initialize(annotation(false, false, 60));
    assertFalse(validator.isValid(new Period(Instant.now(), null), context));
  }

  @Test
  void givenMaxSecondsIsZero_whenBothBoundsAreMissing_thenValid() {
    validator.initialize(annotation(false, false, 0));
    assertTrue(validator.isValid(new Period(null, null), context));
  }

  private static ValidPeriod annotation(boolean fromRequired, boolean toRequired, long maxSeconds) {
    final var annotation = mock(ValidPeriod.class);
    when(annotation.fromRequired()).thenReturn(fromRequired);
    when(annotation.toRequired()).thenReturn(toRequired);
    when(annotation.maxSeconds()).thenReturn(maxSeconds);
    return annotation;
  }

}
