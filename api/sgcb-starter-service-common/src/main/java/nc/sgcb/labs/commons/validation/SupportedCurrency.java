package nc.sgcb.labs.commons.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import nc.sgcb.labs.commons.domain.Currency;
import org.jspecify.annotations.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * <p>True if part of the {@link Currency} enum</p>
 *
 * null is valid to allow optional currency parameters and properties. Use @NotNull to reject nulls:
 * 
 * <pre>
 * &#64;NotNull &#64;SupportedCurrency String currency
 * </pre>
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SupportedCurrency.SupportedCurrencyConstraintValidator.class)
public @interface SupportedCurrency {
  String message() default "Isn't part of the supported ISO 4217 currency codes";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class SupportedCurrencyConstraintValidator implements ConstraintValidator<SupportedCurrency, String> {

    @Override
    public boolean isValid(@Nullable String value, @Nullable ConstraintValidatorContext context) {
      if (value == null) {
        return true;
      }
      try {
        Currency.valueOf(value);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    }

  }
}
