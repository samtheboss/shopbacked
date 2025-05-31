package com.smartapps.shop.Repos;

import com.smartapps.shop.Models.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findBySalespersonId(Long salespersonId);

    List<Allocation> findBySalespersonIdAndStatus(Long salespersonId, Allocation.AllocationStatus status);

    List<Allocation> findBySalespersonIdAndStatusAndId(Long salesperson_id, Allocation.AllocationStatus status, Long id);

    List<Allocation> findByAllocationDate(LocalDate date);
    List<Allocation> findByItemId(Long item_id);
    List<Allocation> findByOrderId(Long orderId);

    List<Allocation> findByStatus(Allocation.AllocationStatus status);

    @Query("SELECT a FROM Allocation a WHERE a.allocationDate BETWEEN :startDate AND :endDate")
    List<Allocation> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.salesperson JOIN FETCH a.item ORDER BY a.createdAt DESC")
    List<Allocation> findAllWithDetails();

    @Query("SELECT a FROM Allocation a WHERE DATE(a.allocationDate) = :date ORDER BY a.createdAt DESC")
    List<Allocation> findByAllocationDateOnly(@Param("date") LocalDate date);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.salesperson JOIN FETCH a.item WHERE DATE(a.allocationDate) = :date ORDER BY a.createdAt DESC")
    List<Allocation> findByAllocationDateWithDetails(@Param("date") LocalDate date);

    @Query("SELECT a FROM Allocation a WHERE a.allocationDate BETWEEN :startDate AND :endDate ORDER BY a.allocationDate DESC, a.createdAt DESC")
    List<Allocation> findByAllocationDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.salesperson JOIN FETCH a.item WHERE a.allocationDate BETWEEN :startDate AND :endDate ORDER BY a.allocationDate DESC, a.createdAt DESC")
    List<Allocation> findByAllocationDateBetweenWithDetails(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT a.allocationDate FROM Allocation a ORDER BY a.allocationDate DESC")
    List<LocalDate> findDistinctAllocationDates();
}
