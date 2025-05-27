package com.smartapps.shop.Models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class SalespersonDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    private BigDecimal totalSales;
    private Double itemsAllocated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public SalespersonDTO(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
