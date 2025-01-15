package ch.akros.vending_machine.plausibility;


import ch.akros.vending_machine.dto.ProductDTO;

import java.util.function.Function;

import static ch.akros.vending_machine.plausibility.ProductValidation.PRODUCT_NOT_FOUND_BY_ID;
import static ch.akros.vending_machine.plausibility.ProductValidation.VALID;

public interface ProductValidator extends Function<ProductDTO, ProductValidation> {

    static ProductValidator findProductById(Integer productId) {
        return productDTO -> productDTO != null && productDTO.getProductId().equals(productId) ? VALID : PRODUCT_NOT_FOUND_BY_ID;
    }
}
