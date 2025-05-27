package com.smartapps.shop.Models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class ItemDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be zero or positive")
    private Double stock;

    @PositiveOrZero(message = "Minimum stock must be zero or positive")
    private Integer minStock;

    private String category;

    @NotBlank(message = "SKU is required")
    private String sku;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemDTO(String name, String description, BigDecimal price, Double stock, String sku) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.sku = sku;
    }
}
