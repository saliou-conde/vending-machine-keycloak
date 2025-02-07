package ch.akros.vending_machine.exception.handler;

import ch.akros.vending_machine.exception.ProductNotFoundException;
import org.hibernate.PropertyValueException;
import org.postgresql.util.PSQLException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ProductExceptionHandling implements ErrorController {

  private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
  private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
  private static final String UNAUTHORIZED_ACCESS = "Authentication failed. Please provide valid credentials.";
  private static final String PRODUCT_NOT_FOUND_BY_ID = "Product does not exist in the database";
  private static final String TOKEN_EXPIRED = "Your session has expired. Please log in again.";
  private static final String INVALID_TOKEN = "Invalid token. Please log in again.";
  private static final String GENERIC_AUTH_ERROR = "Authentication error. Please provide a valid token.";

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDeniedException() {
    return handleGenericException(FORBIDDEN, NOT_ENOUGH_PERMISSION);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
    HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
    return handleGenericException(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleProductNotFoundException(ProductNotFoundException exception) {
    return generateProblemDetail(BAD_REQUEST, exception.getMessage(), PRODUCT_NOT_FOUND_BY_ID);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException(AuthenticationException exception) {
    return generateProblemDetail(UNAUTHORIZED, UNAUTHORIZED_ACCESS, exception.getMessage());
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ProblemDetail> handleJwtException(JwtException exception) {
    String message = Optional.ofNullable(exception.getMessage())
            .map(msg -> msg.contains("expired") ? TOKEN_EXPIRED
                    : msg.contains("invalid") ? INVALID_TOKEN
                    : GENERIC_AUTH_ERROR)
            .orElse(GENERIC_AUTH_ERROR);
    return handleGenericException(UNAUTHORIZED, message);
  }

  @ExceptionHandler(PropertyValueException.class)
  public ResponseEntity<ProblemDetail> handlePropertyValueException(PropertyValueException exception) {
    return handleGenericException(BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(PSQLException.class)
  public ResponseEntity<ProblemDetail> handlePSQLException(PSQLException exception) {
    return handleGenericException(BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
    Map<String, Object> errors = new HashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return handleGenericException(exception.getBody().getDetail(), errors);
  }

  private ResponseEntity<ProblemDetail> generateProblemDetail(HttpStatus status, String message, String title) {
    ProblemDetail problemDetail = handleGenericException(status, message, title, null);
    return new ResponseEntity<>(problemDetail, status);
  }

  private ResponseEntity<ProblemDetail> handleGenericException(HttpStatus status, String message) {
    return generateProblemDetail(status, message, null);
  }

  private ResponseEntity<ProblemDetail> handleGenericException(String message, Map<String, Object> errors) {
    ProblemDetail problemDetail = handleGenericException(HttpStatus.BAD_REQUEST, message, null, errors);
    return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
  }

  private ProblemDetail handleGenericException(HttpStatus status, String message, String title, Map<String, Object> properties) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setTitle(Optional.ofNullable(title).orElse(status.getReasonPhrase()));
    problemDetail.setProperties(Optional.ofNullable(properties).orElseGet(HashMap::new));
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }
}

