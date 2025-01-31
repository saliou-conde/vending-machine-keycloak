package ch.akros.vending_machine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductDTO {
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
}
