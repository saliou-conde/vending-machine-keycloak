package ch.akros.vending_machine.dto.mapper;

import ch.akros.vending_machine.domain.Product;
import ch.akros.vending_machine.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {

    ProductMapper PRODUCT_MAPPER = Mappers.getMapper(ProductMapper.class);

    Product mapToProduct(ProductDTO productDTO);

    ProductDTO mapToProductDTO(Product product);
}
