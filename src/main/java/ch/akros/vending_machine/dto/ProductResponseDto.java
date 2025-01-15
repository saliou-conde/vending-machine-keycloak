package ch.akros.vending_machine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
public class ProductResponseDto {
  private String message;
  private String error;
  private String path;
  HttpStatus status;
  private Integer statusCode;
  private String timestamp;
  private Map<String, ProductDTO> data;
}
