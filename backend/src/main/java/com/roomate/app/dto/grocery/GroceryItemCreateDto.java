package com.roomate.app.dto.grocery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroceryItemCreateDto {

    @NotBlank(message = "Item name is required")
    private String name;

    private String quantity;

    private String category;

    private String notes;

    private BigDecimal estimatedPrice;
}
