package com.smartapps.shop.Services;

import com.smartapps.shop.Models.Inventory;
import com.smartapps.shop.Models.StockAdjustment;
import com.smartapps.shop.Models.dtos.ItemDTO;
import com.smartapps.shop.Repos.ItemRepository;
import com.smartapps.shop.Repos.StockAdjustmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private StockAdjustmentRepository logRepo;
    @Autowired
    AllocationService allocationService;

    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ItemDTO> getItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::convertToDTO);
    }

    public ItemDTO createItem(ItemDTO itemDTO) {
        Inventory item = convertToEntity(itemDTO);
        Inventory savedItem = itemRepository.save(item);
        return convertToDTO(savedItem);
    }

    public void logStockAdjustment(Long productId, String type, double quantity, double prevQty) {
        StockAdjustment log = new StockAdjustment();
        log.setProductId(productId);
        log.setAdjustmentType(type);
        log.setQuantity(quantity);
        log.setPreQuantity(prevQty);
        logRepo.save(log);
    }

    public Optional<ItemDTO> updateItem(Long id, ItemDTO itemDTO) {
        return itemRepository.findById(id).map(existingItem -> {
            double previousStock = existingItem.getStock();
            existingItem.setName(itemDTO.getName());
            existingItem.setDescription(itemDTO.getDescription());
            existingItem.setPrice(itemDTO.getPrice());
            existingItem.setStock(itemDTO.getStock());
            existingItem.setMinStock(itemDTO.getMinStock());
            existingItem.setCategory(itemDTO.getCategory());
            existingItem.setSku(itemDTO.getSku());
            Inventory savedItem = itemRepository.save(existingItem);
            double newStock = savedItem.getStock();
            if (previousStock != newStock) {
                String adjustmentType = (newStock > previousStock) ? "INCREASE" : "DECREASE";
                logStockAdjustment(
                        savedItem.getId(),
                        adjustmentType,
                        newStock,
                        previousStock
                );
            }
            return convertToDTO(savedItem);
        });
    }


    public boolean deleteItem(Long id) {
        int exist = allocationService.findByItemId(id);
        if (itemRepository.existsById(id) && exist==0) {
            itemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<ItemDTO> searchItems(String searchTerm) {
        return itemRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        searchTerm, searchTerm, searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<ItemDTO> getLowStockItems() {
        return itemRepository.findLowStockItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ItemDTO> getAvailableItems() {
        return itemRepository.findAvailableItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ItemDTO> updateStock(Long id, Double newStock) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setStock(newStock);
                    return convertToDTO(itemRepository.save(item));
                });
    }

    private ItemDTO convertToDTO(Inventory item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setStock(item.getStock());
        dto.setMinStock(item.getMinStock());
        dto.setCategory(item.getCategory());
        dto.setSku(item.getSku());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    private Inventory convertToEntity(ItemDTO dto) {
        Inventory item = new Inventory();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setStock(dto.getStock());
        item.setMinStock(dto.getMinStock() != null ? dto.getMinStock() : 0);
        item.setCategory(dto.getCategory());
        item.setSku(dto.getSku());
        return item;
    }
}