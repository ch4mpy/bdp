/** */
package nc.sgcb.labs.commons.exception;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@RestControllerAdvice
public class ExceptionsHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ApiResponse(responseCode = "422",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ValidationProblemDetail.class))})
  public ResponseEntity<ValidationProblemDetail> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    final var detail = new ValidationProblemDetail(ex.getMessage(), ex.getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getCode)));
    return ResponseEntity.status(detail.getStatus()).body(detail);
  }

  @SuppressWarnings("null")
  @ExceptionHandler(ConstraintViolationException.class)
  @ApiResponse(responseCode = "422",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ValidationProblemDetail.class))})
  public ResponseEntity<ValidationProblemDetail> handleConstraintViolation(
      ConstraintViolationException ex) {
    final var problem = new ValidationProblemDetail(ex.getMessage(),
        ex.getConstraintViolations().stream().collect(Collectors
            .toMap(cv -> cv.getPropertyPath().toString(), ConstraintViolation::getMessage)));
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ApiResponse(responseCode = "409",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    final var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    return ResponseEntity.status(detail.getStatus()).body(detail);
  }

  @ExceptionHandler({EntityNotFoundException.class, MissingPathVariableException.class,
      ResourceNotFoundException.class})
  @ApiResponse(responseCode = "404",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleNotFound(Exception ex) {
    final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  @ExceptionHandler({InternalServerErrorException.class})
  @ApiResponse(responseCode = "500",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleInternalServerError(InternalServerErrorException ex) {
    final var problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  public static class ValidationProblemDetail extends ProblemDetail {
    private static final long serialVersionUID = -757082078248225594L;

    @SuppressWarnings("null")
    public static final URI TYPE = URI.create("https://sgbdp.pf/problems/validation");
    public static final String INVALID_FIELDS_PROPERTY = "invalidFields";

    public ValidationProblemDetail(String message, Map<String, String> invalidFields) {
      super(ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, message));
      super.setType(TYPE);
      super.setProperty(INVALID_FIELDS_PROPERTY, invalidFields);
    }

    @SuppressWarnings({"unchecked", "null"})
    @JsonSerialize
    Map<String, String> getInvalidFields() {
      final var properties = super.getProperties();
      return properties == null ? Map.of()
          : (Map<String, String>) properties.get(INVALID_FIELDS_PROPERTY);
    }
  }
}
