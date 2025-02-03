package ch.akros.vending_machine.exception.handler;

import ch.akros.vending_machine.exception.ProductNotFoundException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ProductExceptionHandling implements ErrorController {

  private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
  private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
  private static final String UNAUTHORIZED_ACCESS = "Authentication failed. Please provide valid credentials.";

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> accessDeniedException() {
    return handleGenericException(FORBIDDEN, NOT_ENOUGH_PERMISSION);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
    HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
    return handleGenericException(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> productNotFoundException(ProductNotFoundException exception) {
    return handleGenericException(BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException(AuthenticationException exception) {
    return handleGenericException(UNAUTHORIZED, UNAUTHORIZED_ACCESS+" "+exception.getMessage());
  }

  private ResponseEntity<ProblemDetail> handleGenericException(HttpStatus httpStatus, String message) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, message);
    problemDetail.setTitle(httpStatus.getReasonPhrase());
    problemDetail.setProperty("timestamp", Instant.now());
    return new ResponseEntity<>(problemDetail, httpStatus);
  }
}
