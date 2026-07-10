package nc.sgcb.labs.commons.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import nc.sgcb.labs.commons.domain.Iban.NotAnIbanException;

/**
 * Custom validation annotation sample
 * 
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Iban.IbanConstraintValidator.class)
public @interface Iban {
  String message() default "Doesn't look like an IBAN";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  public static class IbanConstraintValidator implements ConstraintValidator<Iban, String> {

    @Override
    public boolean isValid(@Nullable String value, @Nullable ConstraintValidatorContext context) {
      if (value == null) {
        return false;
      }
      try {
        nc.sgcb.labs.commons.domain.Iban.parse(value);
        return true;
      } catch (NotAnIbanException e) {
        return false;
      }
    }

  }
}
