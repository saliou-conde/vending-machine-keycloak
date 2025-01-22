package ch.akros.vending_machine.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class PriceRequestDTO {
    private List<Integer> prices;
}
