package ch.akros.vending_machine.service;

import ch.akros.vending_machine.domain.Product;
import ch.akros.vending_machine.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader {

  @Value("${data.file}")
  private String fileName;

  private final ObjectMapper objectMapper;
  private final ProductRepository repository;

  public void load() throws IOException {
    try(InputStream in = TypeReference.class.getResourceAsStream(fileName)) {
      repository.saveAll(objectMapper.readValue(in, new TypeReference<List<Product>>() {}));
    }
  }

}
