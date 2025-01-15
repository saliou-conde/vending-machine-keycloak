package ch.akros.vending_machine.controller;

import ch.akros.vending_machine.dto.PriceRequestDTO;
import ch.akros.vending_machine.dto.ProductDTO;
import ch.akros.vending_machine.dto.ProductResponseDto;
import ch.akros.vending_machine.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @Operation(
          description = "Get all Products",
          summary = "Display all the products.",
          responses = {
                  @ApiResponse(
                          description = "Success",
                          responseCode = "200"
                  )
          }
  )
  @GetMapping
  @PreAuthorize("hasRole('client_user')")
  public ResponseEntity<List<ProductDTO>> getAllProducts() {
    return ResponseEntity.ok(productService.getProducts());
  }

  @Operation(
          description = "Add Product",
          summary = "A new product will be added into the database",
          responses = {
                  @ApiResponse(
                          description = "Created",
                          responseCode = "201"
                  ),
                  @ApiResponse(
                          description = "Bad Request",
                          responseCode = "400"
                  )
          }
  )
  @PostMapping
  @PreAuthorize("hasRole('client_admin')")
  public ResponseEntity<ProductResponseDto> addProduct(@RequestBody ProductDTO productDTO) {
    var save = productService.createProduct(productDTO);
    return ResponseEntity.ok(save);
  }

  @Operation(
          description = "Get Product by ID",
          summary = "The found product by the given id will be shown.",
          responses = {
                  @ApiResponse(
                          description = "Success",
                          responseCode = "200"
                  ),
                  @ApiResponse(
                          description = "Not Found",
                          responseCode = "404"
                  )
          }
  )
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('client_user')")
  public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") Integer id) {
    ProductResponseDto productResponseDto = productService.getProduct(id);
    return  new ResponseEntity<>(productResponseDto, productResponseDto.getStatus());
  }

  @Operation(
          description = "Delete Product by ID",
          summary = "The found product will be deleted from the database.",
          responses = {
                  @ApiResponse(
                          description = "Success",
                          responseCode = "200"
                  ),
                  @ApiResponse(
                          description = "Not Found",
                          responseCode = "404"
                  )
          }
  )
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('client_remove')")
  public ResponseEntity<ProductResponseDto> deleteProductById(@PathVariable("id") Integer id) {
    return  ResponseEntity.ok(productService.deleteProduct(id));
  }

  @Operation(
          description = "Update Product by ID",
          summary = "Product will be updated.",
          responses = {
                  @ApiResponse(
                          description = "OK",
                          responseCode = "200"
                  ),
                  @ApiResponse(
                          description = "Not Found",
                          responseCode = "404"
                  )
          }
  )
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('client_update')")
  public ResponseEntity<ProductResponseDto> updateProduct(@RequestBody ProductDTO productDTO, @PathVariable("id")Integer id) {
    return  ResponseEntity.ok(productService.updateProduct(productDTO, id));
  }

  @Operation(
          description = "Buy Product",
          summary = "By product by given Id and price",
          responses = {
                  @ApiResponse(
                          description = "OK",
                          responseCode = "200"
                  ),
                  @ApiResponse(
                          description = "Bad Request",
                          responseCode = "400"
                  ),
                  @ApiResponse(
                          description = "Not Found",
                          responseCode = "404"
                  )
          }
  )
  @PostMapping("/{id}")
  public ResponseEntity<ProductResponseDto> buyProduct(@PathVariable("id") Integer id, @RequestBody PriceRequestDTO priceRequestDTO) {
    ProductResponseDto responseDto = productService.buyProduct(id, priceRequestDTO);
    return new ResponseEntity<>(responseDto, responseDto.getStatus());
  }
}
