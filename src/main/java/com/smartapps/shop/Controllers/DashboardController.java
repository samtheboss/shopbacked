package com.smartapps.shop.Controllers;


import com.smartapps.shop.Services.AllocationService;
import com.smartapps.shop.Services.ItemService;
import com.smartapps.shop.Services.SalespersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private SalespersonService salespersonService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AllocationService allocationService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get basic counts
        var salespeople = salespersonService.getAllSalespeople();
        var items = itemService.getAllItems();
        var lowStockItems = itemService.getLowStockItems();

        // Calculate totals
        BigDecimal totalSales = salespeople.stream()
                .map(s -> s.getTotalSales() != null ? s.getTotalSales() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double totalStock = items.stream()
                .map(i -> i.getStock() != null ? i.getStock() : 0)
                .reduce(0.0, Double::sum);

        BigDecimal totalInventoryValue = items.stream()
                .map(i -> (i.getPrice() != null && i.getStock() != null) ?
                        i.getPrice().multiply(BigDecimal.valueOf(i.getStock())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgSalesPerPerson = salespeople.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(salespeople.size()), 2, BigDecimal.ROUND_HALF_UP);

        stats.put("totalSales", totalSales);
        stats.put("activeSalespeople", salespeople.size());
        stats.put("totalInventory", totalStock);
        stats.put("avgSalesPerPerson", avgSalesPerPerson);
        stats.put("totalItems", items.size());
        stats.put("lowStockItems", lowStockItems.size());
        stats.put("inventoryValue", totalInventoryValue);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-performers")
    public ResponseEntity<Object> getTopPerformers() {
        return ResponseEntity.ok(salespersonService.getTopPerformers());
    }

    @GetMapping("/low-stock-items")
    public ResponseEntity<Object> getLowStockItems() {
        return ResponseEntity.ok(itemService.getLowStockItems());
    }
}