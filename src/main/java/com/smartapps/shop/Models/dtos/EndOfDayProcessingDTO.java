package com.smartapps.shop.Models.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class EndOfDayProcessingDTO {
    @NotNull(message = "Allocation ID is required")
    private Long allocationId;

    @NotNull(message = "Sold quantity is required")
    @PositiveOrZero(message = "Sold quantity must be zero or positive")
    private Double soldQuantity;

    @NotNull(message = "Payment received is required")
    @PositiveOrZero(message = "Payment received must be zero or positive")
    private BigDecimal paymentReceived;


    public EndOfDayProcessingDTO() {}

    public EndOfDayProcessingDTO(Long allocationId, Double soldQuantity, BigDecimal paymentReceived) {
        this.allocationId = allocationId;
        this.soldQuantity = soldQuantity;
        this.paymentReceived = paymentReceived;
    }
}
