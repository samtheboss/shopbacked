package com.smartapps.shop.Models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustment")
@Data
public class StockAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String adjustmentType;

    private double quantity;
    private double preQuantity;

    private String adjustedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
