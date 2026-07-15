package nc.sgcb.labs.commons.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import nc.sgcb.labs.commons.domain.Period;

/**
 * Custom validation annotation sample
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPeriod.ValidPeriodConstraintValidator.class)
public @interface ValidPeriod {
  String message() default "Period does not meet its annotated constraints";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  // false means that the period may have no lower bound
  boolean fromRequired() default false;

  // false means that the period may have no upper bound
  boolean toRequired() default false;

  // 0 and negative mean that duration is not checked
  long maxSeconds() default 0;

  public static class ValidPeriodConstraintValidator
      implements ConstraintValidator<ValidPeriod, Period> {

    private ValidPeriod annotation;

    @Override
    public void initialize(ValidPeriod constraintAnnotation) {
      this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(@Nullable Period value, @Nullable ConstraintValidatorContext context) {
      if (value == null || context == null) {
        return true;
      }
      if (annotation.fromRequired() && value.from() == null) {
        return false;
      }
      if (annotation.toRequired() && value.to() == null) {
        return false;
      }
      if (annotation.maxSeconds() > 0 && (value.from() == null || value.to() == null
          || Duration.between(value.from(), value.to()).getSeconds() > annotation.maxSeconds())) {
        return false;
      }
      return true;
    }

  }
}
