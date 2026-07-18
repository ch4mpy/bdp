package nc.sgcb.labs.commons.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * null is valid to allow optional currency parameters and properties. Use @NotNull to reject nulls:
 * 
 * <pre>
 * &#64;NotNull &#64;CurrencyIso3 String currency
 * </pre>
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyIso3.IbanConstraintValidator.class)
public @interface CurrencyIso3 {
  String message() default "Doesn't look like an ISO 4217 currency code";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  public static class IbanConstraintValidator implements ConstraintValidator<CurrencyIso3, String> {
    @SuppressWarnings("null")
    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(@Nullable String value, @Nullable ConstraintValidatorContext context) {
      if (value == null) {
        return true;
      }
      return ISO_4217.matcher(value).matches();
    }

  }
}
