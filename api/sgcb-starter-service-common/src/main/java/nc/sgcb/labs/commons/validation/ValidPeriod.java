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
 * Specifies a {@link nc.sgcb.labs.commons.domain.Period} validity according to the following rules:
 * <ul>
 * <li>if fromRequired is true, the period must have a lower bound</li>
 * <li>if toRequired is true, the period must have an upper bound</li>
 * <li>if maxSeconds is greater than 0, the period duration must be less than or equal to
 * maxSeconds</li>
 * <li>if the period is null, it is considered valid (use @NotNull to reject nulls)</li>
 * </ul>
 * 
 * <pre>
 * &#64;NotNull &#64;ValidPeriod(maxSeconds = 3600 * 24 * 31) Period searchPeriod
 * </pre>
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

    @SuppressWarnings("null")
    private ValidPeriod annotation;

    @Override
    public void initialize(@SuppressWarnings("null") ValidPeriod constraintAnnotation) {
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
