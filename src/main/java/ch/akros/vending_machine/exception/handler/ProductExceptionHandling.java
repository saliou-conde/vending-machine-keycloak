package ch.akros.vending_machine.exception.handler;

import ch.akros.vending_machine.exception.ProductNotFoundException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

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
  public ResponseEntity<ProblemDetail> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
    Set<HttpMethod> supportedMethods = exception.getSupportedHttpMethods();
    String allowedMethod = !supportedMethods.isEmpty() ? supportedMethods.iterator().next().name()
            : "UNKNOWN";
    return handleGenericException(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, allowedMethod));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleProductNotFoundException(ProductNotFoundException exception) {
    return handleGenericException(BAD_REQUEST, exception.getMessage(), PRODUCT_NOT_FOUND_BY_ID);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException(AuthenticationException exception) {
    return handleGenericException(UNAUTHORIZED, UNAUTHORIZED_ACCESS, exception.getMessage());
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

  private ResponseEntity<ProblemDetail> handleGenericException(HttpStatus status, String message, String title) {
    ProblemDetail problemDetail = createProblemDetail(status, message, title);
    return new ResponseEntity<>(problemDetail, status);
  }

  private ResponseEntity<ProblemDetail> handleGenericException(HttpStatus status, String message) {
    return handleGenericException(status, message, null);
  }

  private ProblemDetail createProblemDetail(HttpStatus status, String message, String title) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setTitle(Optional.ofNullable(title).orElse(status.getReasonPhrase()));
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }
}

