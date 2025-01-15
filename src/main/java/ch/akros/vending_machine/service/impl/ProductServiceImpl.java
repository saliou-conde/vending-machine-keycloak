package ch.akros.vending_machine.service.impl;

import ch.akros.vending_machine.domain.Product;
import ch.akros.vending_machine.dto.PriceRequestDTO;
import ch.akros.vending_machine.dto.ProductDTO;
import ch.akros.vending_machine.dto.ProductResponseDto;
import ch.akros.vending_machine.dto.mapper.ProductMapper;
import ch.akros.vending_machine.plausibility.ProductValidation;
import ch.akros.vending_machine.plausibility.ProductValidator;
import ch.akros.vending_machine.repository.ProductRepository;
import ch.akros.vending_machine.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static ch.akros.vending_machine.constant.AppConstant.PRODUCT_API_PATH;
import static ch.akros.vending_machine.constant.AppConstant.PRODUCT_KEY;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private static final ProductMapper PRODUCT_MAPPER = ProductMapper.PRODUCT_MAPPER;

    @Override
    public List<ProductDTO> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductMapper.PRODUCT_MAPPER::mapToProductDTO).toList();
    }

    @Override
    public ProductResponseDto getProduct(Integer id) {
        Product productById = findProductById(id);
        var validation = ProductValidator.findProductById(id).apply(PRODUCT_MAPPER.mapToProductDTO(productById));
        if (validation == ProductValidation.VALID) {
            return ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(OK)
                    .message("Product found by ID: " + id)
                    .error(null)
                    .path(PRODUCT_API_PATH + id)
                    .statusCode(OK.value())
                    .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(productById)))
                    .build();
        }
        return productNotFoundById(id);
    }

    @Override
    public ProductResponseDto createProduct(ProductDTO productDTO) {

        var productName = productDTO.getProductName();
        var findProduct = productRepository.findByProductName(productName);

        if (findProduct == null) {
            Product product1 = new Product();
            product1.setProductName(productName);
            product1.setProductPrice(productDTO.getProductPrice());
            product1.setQuantity(1);

            ProductResponseDto responseDto = ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(CREATED)
                    .error(null)
                    .path(PRODUCT_API_PATH)
                    .statusCode(CREATED.value())
                    .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(productRepository.save(product1))))
                    .build();
            log.info("HttpStatus: {}", responseDto.getStatus());
            return responseDto;
        } else {
            if (findProduct.getQuantity() > 9) {
                return ProductResponseDto.builder()
                        .timestamp(Instant.now().toString())
                        .status(BAD_REQUEST)
                        .error("Product cannot be added")
                        .path(PRODUCT_API_PATH)
                        .statusCode(BAD_REQUEST.value())
                        .data(Map.of(PRODUCT_KEY, productDTO))
                        .build();
            }
            findProduct.setQuantity(findProduct.getQuantity() + 1);
            findProduct.setProductId(findProduct.getProductId());
            findProduct.setProductName(findProduct.getProductName());
            findProduct.setProductPrice(productDTO.getProductPrice() == null ? findProduct.getProductPrice() : productDTO.getProductPrice());

            return ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(CREATED)
                    .error(null)
                    .path(PRODUCT_API_PATH)
                    .statusCode(CREATED.value())
                    .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(productRepository.save(findProduct))))
                    .build();
        }
    }

    @Override
    public ProductResponseDto deleteProduct(Integer id) {
        Product product = findProductById(id);
        if (product != null) {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                productRepository.save(product);

                return ProductResponseDto.builder()
                        .timestamp(Instant.now().toString())
                        .status(OK)
                        .error(null)
                        .statusCode(OK.value())
                        .path(PRODUCT_API_PATH + id)
                        .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(product)))
                        .build();

            }

            productRepository.deleteById(id);
            return ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(OK)
                    .message("Product deleted")
                    .error(null)
                    .statusCode(OK.value())
                    .path(PRODUCT_API_PATH + id)
                    .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(product)))
                    .build();
        }
        return productNotFoundById(id);
    }

    @Override
    public ProductResponseDto updateProduct(ProductDTO productDTO, Integer id) {
        if(productDTO.getQuantity() != null && productDTO.getQuantity() > 10) {
            return ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(BAD_REQUEST)
                    .message("Product quantity must be less than 11")
                    .error("Product cannot be updated, due the number of products: " + productDTO.getQuantity())
                    .statusCode(BAD_REQUEST.value())
                    .path(PRODUCT_API_PATH + id)
                    .data(Map.of(PRODUCT_KEY, productDTO))
                    .build();
        }
        Product findProductById = findProductById(id);
        if (findProductById != null && findProductById.getProductId().equals(id)) {
            if (productDTO.getProductName() != null) {
                findProductById.setProductName(productDTO.getProductName());
            }
            if (productDTO.getProductPrice() != null) {
                findProductById.setProductPrice(productDTO.getProductPrice());
            }
            if (productDTO.getQuantity() != null) {
                findProductById.setQuantity(productDTO.getQuantity());
            }
            var update = productRepository.save(findProductById);
            return ProductResponseDto.builder()
                    .timestamp(Instant.now().toString())
                    .status(OK)
                    .error(null)
                    .statusCode(OK.value())
                    .path(PRODUCT_API_PATH + id)
                    .data(Map.of(PRODUCT_KEY, PRODUCT_MAPPER.mapToProductDTO(update)))
                    .build();
        }
        return productNotFoundById(id);
    }

    public ProductResponseDto buyProduct(Integer id, PriceRequestDTO priceRequestDTO) {
        List<Integer> prices = priceRequestDTO.getPrices();
        var notAllowed = prices.stream().filter(price -> price == 1 || price == 5 || price == 500).toList();
        if (!notAllowed.isEmpty()) {
            return ProductResponseDto.builder()
                    .message("Coin not allowed")
                    .error("Vending Machine does not accept coins: [1 or 5 or 500]")
                    .status(HttpStatus.BAD_REQUEST)
                    .timestamp(Instant.now().toString())
                    .path(PRODUCT_API_PATH + id)
                    .build();
        }

        return buyProduct(id, prices.stream().reduce(0, Integer::sum));
    }

    private ProductResponseDto buyProduct(Integer id, Integer price) {
        ProductResponseDto productResponseDto = getProduct(id);
        if (productResponseDto != null && productResponseDto.getStatus() == OK) {
            ProductDTO productDTO = productResponseDto.getData().get(PRODUCT_KEY);
            log.info("Given Price is: {}, Product price is: {}", price, productDTO.getProductPrice());
            if (price > productDTO.getProductPrice()) {
                return ProductResponseDto.builder()
                        .message("Please insert a price = " + productDTO.getProductPrice())
                        .error("Vending Machine can not return change")
                        .status(HttpStatus.BAD_REQUEST)
                        .timestamp(Instant.now().toString())
                        .path(PRODUCT_API_PATH + id)
                        .build();
            } else if (price < productDTO.getProductPrice()) {
                return ProductResponseDto.builder()
                        .message("Please insert a price = " + productDTO.getProductPrice())
                        .error("Inserted price is lower than expected price")
                        .status(HttpStatus.BAD_REQUEST)
                        .timestamp(Instant.now().toString())
                        .path(PRODUCT_API_PATH + id)
                        .build();
            }
            return deleteProduct(id);
        }
        return productNotFoundById(id);
    }

    private Product findProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    private ProductResponseDto productNotFoundById(Integer id) {
        return ProductResponseDto.builder()
                .timestamp(Instant.now().toString())
                .status(NOT_FOUND)
                .error("Product does not exist in the DB")
                .message("Product not found by ID: " + id)
                .statusCode(NOT_FOUND.value())
                .path(PRODUCT_API_PATH + id)
                .data(Map.of(PRODUCT_KEY, new ProductDTO()))
                .build();
    }
}
