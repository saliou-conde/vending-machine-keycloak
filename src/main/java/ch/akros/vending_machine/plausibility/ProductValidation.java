package ch.akros.vending_machine.plausibility;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductValidation {

    VALID("Product Valid"),
    PRODUCT_NOT_FOUND_BY_ID("Product not found by ID");
    private String description;
}
