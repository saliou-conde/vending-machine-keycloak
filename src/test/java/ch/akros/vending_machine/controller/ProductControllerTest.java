package ch.akros.vending_machine.controller;

import ch.akros.vending_machine.dto.PriceRequestDTO;
import ch.akros.vending_machine.dto.ProductDTO;
import ch.akros.vending_machine.dto.ProductResponseDto;
import ch.akros.vending_machine.exception.ProductNotFoundException;
import ch.akros.vending_machine.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;

import static ch.akros.vending_machine.constant.AppConstant.PRODUCT_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.HttpStatus.OK;

class ProductControllerTest {

  @InjectMocks
  private ProductController controller;

  @Mock
  private ProductService productService;

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  void getAllProducts() {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    ProductDTO productDTO2 = ProductDTO.builder()
            .productId(1)
            .productName("Fanta")
            .productPrice(350)
            .quantity(1)
            .build();

    var productDTOS = List.of(productDTO1, productDTO2);
    when(productService.getProducts()).thenReturn(productDTOS);

    //When
    var responseEntity = controller.getAllProducts();
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    int size = responseEntity.getBody().size();
    assertThat(size).isEqualTo(productDTOS.size());

    //Verify
    verify(productService, times(1)).getProducts();
  }

  @Test
  void addProduct() {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    ProductResponseDto responseDto = ProductResponseDto.builder()
            .message("Product added")
            .data(Map.of(PRODUCT_KEY, productDTO1))
            .status(OK)
            .statusCode(OK.value())
            .build();

    when(productService.createProduct(productDTO1)).thenReturn(responseDto);

    //When
    var responseEntity = controller.addProduct(productDTO1);

    //Then
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    ProductDTO actual = responseEntity.getBody().getData().get(PRODUCT_KEY);
    assertThat(actual).isEqualTo(productDTO1);
  }

  @Test
  void getProductById() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    ProductResponseDto responseDto = ProductResponseDto.builder()
            .message("Product found by ID: "+productDTO1.getProductId())
            .data(Map.of(PRODUCT_KEY, productDTO1))
            .status(OK)
            .statusCode(OK.value())
            .build();

    when(productService.getProduct(productDTO1.getProductId())).thenReturn(responseDto);

    //When
    var responseEntity = controller.getProductById(productDTO1.getProductId());

    //Then
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    ProductDTO actual = responseEntity.getBody().getData().get(PRODUCT_KEY);
    assertThat(actual).isEqualTo(productDTO1);
  }

  @Test
  void deleteProductById() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    ProductResponseDto responseDto = ProductResponseDto.builder()
            .message("Product deleted by ID: "+productDTO1.getProductId())
            .data(Map.of(PRODUCT_KEY, productDTO1))
            .status(OK)
            .statusCode(OK.value())
            .build();

    when(productService.deleteProduct(productDTO1.getProductId())).thenReturn(responseDto);

    //When
    var responseEntity = controller.deleteProductById(productDTO1.getProductId());

    //Then
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    ProductDTO actual = responseEntity.getBody().getData().get(PRODUCT_KEY);
    assertThat(actual).isEqualTo(productDTO1);
  }

  @Test
  void updateProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    ProductResponseDto responseDto = ProductResponseDto.builder()
            .message("Product deleted by ID: "+productDTO1.getProductId())
            .data(Map.of(PRODUCT_KEY, productDTO1))
            .status(OK)
            .statusCode(OK.value())
            .build();

    when(productService.updateProduct(productDTO1, productDTO1.getProductId())).thenReturn(responseDto);

    //When
    productDTO1.setQuantity(9);
    var responseEntity = controller.updateProduct(productDTO1, productDTO1.getProductId());

    //Then
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    ProductDTO actual = responseEntity.getBody().getData().get(PRODUCT_KEY);
    assertThat(actual).isEqualTo(productDTO1);
  }

  @Test
  void buyProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO1 = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(50, 100, 200))
            .build();

    ProductResponseDto responseDto = ProductResponseDto.builder()
            .message("Product deleted by ID: "+productDTO1.getProductId())
            .data(Map.of(PRODUCT_KEY, productDTO1))
            .status(OK)
            .statusCode(OK.value())
            .build();

    when(productService.buyProduct(productDTO1.getProductId(), priceRequestDTO)).thenReturn(responseDto);

    //When
    var responseEntity = controller.buyProduct(productDTO1.getProductId(), priceRequestDTO);

    //Then
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    ProductDTO actual = responseEntity.getBody().getData().get(PRODUCT_KEY);
    assertThat(actual).isEqualTo(productDTO1);
  }
}