package ch.akros.vending_machine.service;

import ch.akros.vending_machine.domain.Product;
import ch.akros.vending_machine.dto.PriceRequestDTO;
import ch.akros.vending_machine.dto.ProductDTO;
import ch.akros.vending_machine.dto.ProductResponseDto;
import ch.akros.vending_machine.dto.mapper.ProductMapper;
import ch.akros.vending_machine.exception.ProductNotFoundException;
import ch.akros.vending_machine.repository.ProductRepository;
import ch.akros.vending_machine.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static ch.akros.vending_machine.constant.AppConstant.PRODUCT_KEY;
import static ch.akros.vending_machine.dto.mapper.ProductMapper.PRODUCT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.HttpStatus.*;

class ProductServiceTest {

  @InjectMocks
  private ProductServiceImpl productService;

  @Mock
  private ProductRepository productRepository;

  private static final ProductMapper MAPPER = PRODUCT_MAPPER;

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  void getProducts() {
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

    List<ProductDTO> expectedProductDTOs = List.of(productDTO1, productDTO2);

    List<Product> products = expectedProductDTOs.stream().map(MAPPER::mapToProduct).toList();

    when(productRepository.findAll()).thenReturn(products);

    //When
    List<ProductDTO> result = productService.getProducts();

    //Then
    assertThat(result).hasSize(expectedProductDTOs.size());

    //Verify
    verify(productRepository, times(1)).findAll();
  }

  @Test
  void getProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    ProductResponseDto responseDto = productService.getProduct(productDTO.getProductId());

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(OK);

    //Verify
    verify(productRepository, times(1)).findById(productDTO.getProductId());
  }

  @Test
  void getProductByNonExistingProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    ProductResponseDto responseDto = productService.getProduct(Integer.MAX_VALUE);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(NOT_FOUND);

    //Verify
    verify(productRepository, times(0)).findById(productDTO.getProductId());
  }

  @Test
  void createProduct() {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findByProductName(productDTO.getProductName())).thenReturn(null);
    when(productRepository.save(any())).thenReturn(product);

    //When
    var responseDto = productService.createProduct(productDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(CREATED);

    //Verify
    verify(productRepository, times(1)).save(any());
  }

  @Test
  void createProductWithQuantityMoreThan10Products() {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(11)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findByProductName(productDTO.getProductName())).thenReturn(product);
    when(productRepository.save(any())).thenReturn(product);

    //When
    var responseDto = productService.createProduct(productDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(BAD_REQUEST);

    //Verify
    verify(productRepository, times(0)).save(any());
  }

  @Test
  void createProductWithAlreadyExistingProduct() {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.save(product)).thenReturn(product);
    when(productRepository.findByProductName(productDTO.getProductName())).thenReturn(product);

    //When
    var responseDto = productService.createProduct(productDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(CREATED);

    //Verify
    verify(productRepository, times(1)).save(product);
  }

  @Test
  void deleteProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.deleteProduct(productDTO.getProductId());

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(OK);

    verify(productRepository, times(1)).deleteById(productDTO.getProductId());
  }

  @Test
  void deleteProductByQuantityMoreThanOneProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(10)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.deleteProduct(productDTO.getProductId());

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(OK);
    ProductDTO productDTOFromResponse = responseDto.getData().get(PRODUCT_KEY);
    assertThat(productDTOFromResponse).isNotNull();
    assertThat(productDTOFromResponse.getQuantity()).isEqualTo(9);
  }

  @Test
  void deleteByNonExistingProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.empty());

    //When
    var responseDto = productService.deleteProduct(productDTO.getProductId());

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(NOT_FOUND);

    verify(productRepository, times(0)).deleteById(productDTO.getProductId());
  }

  @Test
  void updateProductByExistingProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));
    when(productRepository.save(product)).thenReturn(product);


    //When
    productDTO.setQuantity(2);
    var responseDto = productService.updateProduct(productDTO, productDTO.getProductId());

    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(OK);

    //Verify
    verify(productRepository, times(1)).save(product);
  }

  @Test
  void updateProductByNonExistingProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findByProductName(productDTO.getProductName())).thenReturn(product);
    when(productRepository.save(product)).thenReturn(product);


    //When
    var responseDto = productService.updateProduct(productDTO, productDTO.getProductId());

    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(NOT_FOUND);

    //Verify
    verify(productRepository, times(0)).save(product);
  }

  @Test
  void updateProductByQuantityMoreThanTenProducts() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));
    when(productRepository.save(product)).thenReturn(product);


    //When
    productDTO.setQuantity(11);
    var responseDto = productService.updateProduct(productDTO, productDTO.getProductId());

    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(BAD_REQUEST);

    //Verify
    verify(productRepository, times(0)).save(product);
  }

  @Test
  void buyProduct() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(50, 100, 200))
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.buyProduct(productDTO.getProductId(), priceRequestDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(OK);
  }

  @Test
  void buyProductByUsingNonExistingProductName() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(50, 100, 200))
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.buyProduct(Integer.MAX_VALUE, priceRequestDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(NOT_FOUND);
  }

  @Test
  void buyProductWithNotAllowedCoins() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(510)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(1, 5, 500))
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.buyProduct(productDTO.getProductId(), priceRequestDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(BAD_REQUEST);
  }

  @Test
  void buyProductWithLessThanPrice() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(50, 100, 100))
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.buyProduct(productDTO.getProductId(), priceRequestDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(BAD_REQUEST);
  }

  @Test
  void buyProductWithMoreThanPrice() throws ProductNotFoundException {
    //Given
    ProductDTO productDTO = ProductDTO.builder()
            .productId(1)
            .productName("Cola")
            .productPrice(350)
            .quantity(1)
            .build();

    PriceRequestDTO priceRequestDTO = PriceRequestDTO.builder()
            .prices(List.of(50, 100, 1000))
            .build();

    Product product = MAPPER.mapToProduct(productDTO);

    when(productRepository.findById(productDTO.getProductId())).thenReturn(Optional.of(product));

    //When
    var responseDto = productService.buyProduct(productDTO.getProductId(), priceRequestDTO);

    //Then
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getStatus()).isNotNull().isEqualTo(BAD_REQUEST);
  }
}