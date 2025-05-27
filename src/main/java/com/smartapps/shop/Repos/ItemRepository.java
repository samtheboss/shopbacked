package com.smartapps.shop.Repos;

import com.smartapps.shop.Models.Inventory;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ItemRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findById(int id);
    Optional<Inventory> findBySku(String sku);

    List<Inventory> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String sku, String category);

    @Query("SELECT i FROM Inventory i WHERE i.stock <= i.minStock")
    List<Inventory> findLowStockItems();

    List<Inventory> findByCategory(String category);

    @Query("SELECT i FROM Inventory i WHERE i.stock > 0 ORDER BY i.name")
    List<Inventory> findAvailableItems();


}
