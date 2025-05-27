package com.smartapps.shop.Controllers;
import com.smartapps.shop.Models.Inventory;
import com.smartapps.shop.Models.dtos.ItemDTO;
import com.smartapps.shop.Services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<ItemDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem( @RequestBody ItemDTO itemDTO) {
        System.out.println(itemDTO);
        ItemDTO createdItem = itemService.createItem(itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(@PathVariable Long id,  @RequestBody ItemDTO itemDTO) {
        return itemService.updateItem(id, itemDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.deleteItem(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> searchItems(@RequestParam String q) {
        List<ItemDTO> items = itemService.searchItems(q);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ItemDTO>> getLowStockItems() {
        List<ItemDTO> lowStockItems = itemService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ItemDTO>> getAvailableItems() {
        List<ItemDTO> availableItems = itemService.getAvailableItems();
        return ResponseEntity.ok(availableItems);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ItemDTO> updateStock(@PathVariable Long id, @RequestBody Map<String, Double> stockUpdate) {
        Double newStock = stockUpdate.get("stock");
        if (newStock == null) {
            return ResponseEntity.badRequest().build();
        }

        return itemService.updateStock(id, newStock)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}