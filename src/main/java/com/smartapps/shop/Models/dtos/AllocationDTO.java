package com.smartapps.shop.Models.dtos;

import com.smartapps.shop.Models.Allocation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AllocationDTO {
    private Long id;

    @NotNull(message = "Salesperson ID is required")
    private Long salespersonId;

    private String salespersonName;
    private String productName;
    private String productId;
    @NotNull(message = "Item ID is required")
    private Long itemId;

    private String itemName;
    private BigDecimal itemPrice;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    private LocalDate allocationDate;
    private Allocation.AllocationStatus status;
    private Double soldQuantity;
    private BigDecimal paymentReceived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AllocationDTO(Long salespersonId, Long itemId, Double quantity) {
        this.salespersonId = salespersonId;
        this.itemId = itemId;
        this.quantity = quantity;
    }
}
