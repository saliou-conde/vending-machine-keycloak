package ch.akros.vending_machine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductDTO {
    private Integer productId;
    @NotBlank(message = "the productId must not null or empty")
    private String productName;
    @NotNull(message = "the productPrice must not null")
    private Integer productPrice;
    private Integer quantity;
}
