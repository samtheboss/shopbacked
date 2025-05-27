package com.smartapps.shop.Models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
public class DailyAllocationSummaryDTO {
    private LocalDate date;
    private Integer totalAllocations;
    private Double totalQuantityAllocated;
    private Double totalSold;
    private Double totalReturned;
    private BigDecimal totalRevenue;
    private Integer uniqueSalespeople;
    private Integer uniqueItems;
    public DailyAllocationSummaryDTO(LocalDate date) {
        this.date = date;
        this.totalAllocations = 0;
        this.totalQuantityAllocated = 0.0;
        this.totalSold = 0.0;
        this.totalReturned = 0.0;
        this.totalRevenue = BigDecimal.ZERO;
        this.uniqueSalespeople = 0;
        this.uniqueItems = 0;
    }
}
