package ch.akros.vending_machine.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;
    @Column(nullable = false, unique = true)
    @NotBlank(message = "the productId must not null or empty")
    private String productName;
    @NotNull(message = "the productPrice must not null")
    @Column(nullable = false)
    private Integer productPrice;
    private Integer quantity;
}
