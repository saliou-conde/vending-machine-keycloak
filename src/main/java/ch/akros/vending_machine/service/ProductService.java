package ch.akros.vending_machine.service;

import ch.akros.vending_machine.dto.PriceRequestDTO;
import ch.akros.vending_machine.dto.ProductDTO;
import ch.akros.vending_machine.dto.ProductResponseDto;
import ch.akros.vending_machine.exception.ProductNotFoundException;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getProducts();
    ProductResponseDto getProduct(Integer id) throws ProductNotFoundException;
    ProductResponseDto createProduct(ProductDTO product);
    ProductResponseDto deleteProduct(Integer id);
    ProductResponseDto updateProduct(ProductDTO product, Integer id);
    ProductResponseDto buyProduct(Integer id, PriceRequestDTO priceRequestDTO) throws ProductNotFoundException;
}
