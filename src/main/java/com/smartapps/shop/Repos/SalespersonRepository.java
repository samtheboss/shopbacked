package com.smartapps.shop.Repos;

import com.smartapps.shop.Models.SalesPersons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalespersonRepository extends JpaRepository<SalesPersons, Long> {

    List<SalesPersons> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM SalesPersons s ORDER BY s.totalSales DESC")
    List<SalesPersons> findAllOrderByTotalSalesDesc();

    @Query("SELECT s FROM SalesPersons s WHERE s.itemsAllocated > 0")
    List<SalesPersons> findSalespeopleWithAllocations();
}