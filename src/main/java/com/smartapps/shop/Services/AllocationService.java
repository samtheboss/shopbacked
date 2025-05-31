package com.smartapps.shop.Services;

import com.smartapps.shop.Models.*;
import com.smartapps.shop.Models.dtos.AllocationDTO;
import com.smartapps.shop.Models.dtos.DailyAllocationSummaryDTO;
import com.smartapps.shop.Models.dtos.EndOfDayProcessingDTO;
import com.smartapps.shop.Repos.*;
import jakarta.transaction.Transactional;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.smartapps.shop.Models.Allocation.*;


@Service
@Transactional
public class AllocationService {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private SalespersonRepository salespersonRepository;

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    OrdersRepo ordersRepository;


    public List<AllocationDTO> getAllAllocations() {
        return allocationRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<AllocationDTO> getAllocationById(Long id) {
        return allocationRepository.findById(id)
                .map(this::convertToDTO);
    }  public Integer findByItemId(Long id) {
        return allocationRepository.findByItemId(id).size();

    }

    public ResponseEntity<?> createAllocations(List<AllocationDTO> allocationDTOs) {
        if (allocationDTOs.isEmpty()) {
            return ResponseEntity.badRequest().body("Allocation list is empty.");
        }

        Long salespersonId = allocationDTOs.get(0).getSalespersonId();
        Optional<SalesPersons> salespersonOpt = salespersonRepository.findById(salespersonId);
        if (salespersonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Salesperson not found with ID: " + salespersonId);
        }

        SalesPersons salesperson = salespersonOpt.get();

        // Create one order for the batch
        Orders orders = new Orders();
        orders.setSalespersonId(salespersonId);
        orders.setOrderStatus("ALLOCATED"); // or use a value from DTO if needed
        ordersRepository.save(orders); // this will generate the orderId

        List<AllocationDTO> savedAllocations = new ArrayList<>();

        for (AllocationDTO dto : allocationDTOs) {
            Optional<Inventory> itemOpt = itemRepository.findById(dto.getItemId());
            if (itemOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Item not found with ID: " + dto.getItemId());
            }

            Inventory item = itemOpt.get();

            if (item.getStock() < dto.getQuantity()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Insufficient stock for item ID: " + dto.getItemId() +
                                ". Available: " + item.getStock() + ", Requested: " + dto.getQuantity());
            }

            Allocation allocation = new Allocation();
            allocation.setSalesperson(salesperson);
            allocation.setItem(item);
            allocation.setQuantity(dto.getQuantity());
            allocation.setOrderId(orders.getOrderId());
            if (dto.getAllocationDate() != null) {
                allocation.setAllocationDate(dto.getAllocationDate());
            }

            // Deduct stock
            item.setStock(item.getStock() - dto.getQuantity());
            itemRepository.save(item);

            // Save allocation
            Allocation saved = allocationRepository.save(allocation);
            savedAllocations.add(convertToDTO(saved));

            // Update salesperson allocated count
            salesperson.setItemsAllocated(salesperson.getItemsAllocated() + dto.getQuantity());
        }

        // Save updated salesperson once
        salespersonRepository.save(salesperson);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAllocations);
    }

//    public AllocationDTO createAllocation(AllocationDTO allocationDTO) {
//        Optional<SalesPersons> salesperson = salespersonRepository.findById(allocationDTO.getSalespersonId());
//        Optional<Inventory> item = itemRepository.findById(allocationDTO.getItemId());
//
//        if (salesperson.isEmpty()) {
//            throw new RuntimeException("Salesperson not found with ID: " + allocationDTO.getSalespersonId());
//        }
//
//        if (item.isEmpty()) {
//            throw new RuntimeException("Item not found with ID: " + allocationDTO.getItemId());
//        }
//
//        Inventory itemEntity = item.get();
//        if (itemEntity.getStock() < allocationDTO.getQuantity()) {
//            throw new RuntimeException("Insufficient stock. Available: " + itemEntity.getStock() +
//                    ", Requested: " + allocationDTO.getQuantity());
//        }
//
//        Allocation allocation = new Allocation();
//        allocation.setSalesperson(salesperson.get());
//        allocation.setItem(itemEntity);
//        allocation.setQuantity(allocationDTO.getQuantity());
//
//        if (allocationDTO.getAllocationDate() != null) {
//            allocation.setAllocationDate(allocationDTO.getAllocationDate());
//        }
//
//        // Update item stock
//        itemEntity.setStock(itemEntity.getStock() - allocationDTO.getQuantity());
//        itemRepository.save(itemEntity);
//
//        // Update salesperson allocated items
//        SalesPersons salespersonEntity = salesperson.get();
//        salespersonEntity.setItemsAllocated(salespersonEntity.getItemsAllocated() + allocationDTO.getQuantity());
//        salespersonRepository.save(salespersonEntity);
//
//        Allocation savedAllocation = allocationRepository.save(allocation);
//        return convertToDTO(savedAllocation);
//    }

    public ResponseEntity<?> createAllocation(AllocationDTO allocationDTO) {
        Optional<SalesPersons> salesperson = salespersonRepository.findById(allocationDTO.getSalespersonId());
        Optional<Inventory> item = itemRepository.findById(allocationDTO.getItemId());

        if (salesperson.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Salesperson not found with ID: " + allocationDTO.getSalespersonId());
        }

        if (item.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found with ID: " + allocationDTO.getItemId());
        }

        Inventory itemEntity = item.get();
        if (itemEntity.getStock() < allocationDTO.getQuantity()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Insufficient stock. Available: " + itemEntity.getStock() +
                            ", Requested: " + allocationDTO.getQuantity());
        }

        Orders orders = new Orders();
        orders.setSalespersonId(allocationDTO.getSalespersonId());
        orders.setOrderStatus(String.valueOf(allocationDTO.getStatus()));
        ordersRepository.save(orders);

        // Proceed with allocation
        Allocation allocation = new Allocation();
        allocation.setSalesperson(salesperson.get());
        allocation.setItem(itemEntity);
        allocation.setOrderId(orders.getOrderId());
        allocation.setQuantity(allocationDTO.getQuantity());

        if (allocationDTO.getAllocationDate() != null) {
            allocation.setAllocationDate(allocationDTO.getAllocationDate());
        }

        // Update item stock
        itemEntity.setStock(itemEntity.getStock() - allocationDTO.getQuantity());
        itemRepository.save(itemEntity);

        // Update salesperson allocated items
        SalesPersons salespersonEntity = salesperson.get();
        salespersonEntity.setItemsAllocated(
                salespersonEntity.getItemsAllocated() + allocationDTO.getQuantity());
        salespersonRepository.save(salespersonEntity);

        Allocation savedAllocation = allocationRepository.save(allocation);
        AllocationDTO responseDTO = convertToDTO(savedAllocation);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

//    public Optional<AllocationDTO> updateAllocation(Long id, AllocationDTO allocationDTO) {
//        return allocationRepository.findById(id)
//                .map(existingAllocation -> {
//                    // Update basic fields
//                    if (allocationDTO.getQuantity() != null) {
//                        // Calculate stock difference
//                        int stockDifference = allocationDTO.getQuantity() - existingAllocation.getQuantity();
//
//                        // Update item stock
//                        Inventory item = existingAllocation.getItem();
//                        if (item.getStock() < stockDifference) {
//                            throw new RuntimeException("Insufficient stock for quantity increase");
//                        }
//                        item.setStock(item.getStock() - stockDifference);
//                        itemRepository.save(item);
//
//                        // Update salesperson allocated items
//                        SalesPersons salesperson = existingAllocation.getSalesperson();
//                        salesperson.setItemsAllocated(salesperson.getItemsAllocated() + stockDifference);
//                        salespersonRepository.save(salesperson);
//
//                        existingAllocation.setQuantity(allocationDTO.getQuantity());
//                    }
//
//                    if (allocationDTO.getAllocationDate() != null) {
//                        existingAllocation.setAllocationDate(allocationDTO.getAllocationDate());
//                    }
//
//                    if (allocationDTO.getStatus() != null) {
//                        existingAllocation.setStatus(allocationDTO.getStatus());
//                    }
//
//                    if (allocationDTO.getSoldQuantity() != null) {
//                        existingAllocation.setSoldQuantity(allocationDTO.getSoldQuantity());
//                    }
//
//                    if (allocationDTO.getPaymentReceived() != null) {
//                        existingAllocation.setPaymentReceived(allocationDTO.getPaymentReceived());
//                    }
//
//                    return convertToDTO(allocationRepository.save(existingAllocation));
//                });
//    }




    public ResponseEntity<?> updateAllocation(Long id, AllocationDTO allocationDTO) {
        Optional<Allocation> allocationOpt = allocationRepository.findById(id);

        if (allocationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Allocation not found with ID: " + id);
        }

        Allocation existingAllocation = allocationOpt.get();

        if (allocationDTO.getQuantity() != null) {
            Double stockDifference = allocationDTO.getQuantity() - existingAllocation.getQuantity();

            Inventory item = existingAllocation.getItem();
            if (stockDifference > 0 && item.getStock() < stockDifference) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Insufficient stock for quantity increase. Available: " +
                                item.getStock() + ", Required: " + stockDifference);
            }

            item.setStock(item.getStock() - stockDifference);
            itemRepository.save(item);

            SalesPersons salesperson = existingAllocation.getSalesperson();
            salesperson.setItemsAllocated(salesperson.getItemsAllocated() + stockDifference);
            salespersonRepository.save(salesperson);

            existingAllocation.setQuantity(allocationDTO.getQuantity());
        }

        if (allocationDTO.getAllocationDate() != null) {
            existingAllocation.setAllocationDate(allocationDTO.getAllocationDate());
        }

        if (allocationDTO.getStatus() != null) {
            existingAllocation.setStatus(allocationDTO.getStatus());
        }

        if (allocationDTO.getSoldQuantity() != null) {
            existingAllocation.setSoldQuantity(allocationDTO.getSoldQuantity());
        }

        if (allocationDTO.getPaymentReceived() != null) {
            existingAllocation.setPaymentReceived(allocationDTO.getPaymentReceived());
        }

        Allocation updatedAllocation = allocationRepository.save(existingAllocation);
        return ResponseEntity.ok(convertToDTO(updatedAllocation));
    }

    public boolean deleteAllocation(Long id) {
        return allocationRepository.findById(id)
                .map(allocation -> {
                    // Return stock to item
                    Inventory item = allocation.getItem();
                    item.setStock(item.getStock() + allocation.getQuantity());
                    itemRepository.save(item);

                    // Update salesperson allocated items
                    SalesPersons salesperson = allocation.getSalesperson();
                    salesperson.setItemsAllocated(salesperson.getItemsAllocated() - allocation.getQuantity());
                    salespersonRepository.save(salesperson);

                    allocationRepository.delete(allocation);
                    return true;
                })
                .orElse(false);
    }

    // Date-based queries

    public List<AllocationDTO> getAllocationsByDate(LocalDate date) {
        return allocationRepository.findByAllocationDateWithDetails(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return allocationRepository.findByAllocationDateBetweenWithDetails(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocalDate> getDistinctAllocationDates() {
        return allocationRepository.findDistinctAllocationDates();
    }

    public List<AllocationDTO> getTodayAllocations() {
        return getAllocationsByDate(LocalDate.now());
    }

    // Salesperson-based queries

    public List<AllocationDTO> getAllocationsBySalesperson(Long salespersonId) {
        return allocationRepository.findBySalespersonId(salespersonId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsBySalespersonAndStatus(Long salespersonId, AllocationStatus status) {
        return allocationRepository.findBySalespersonIdAndStatus(salespersonId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByDateAndSalesperson(LocalDate date, Long salespersonId) {
        return allocationRepository.findByAllocationDateWithDetails(date).stream()
                .filter(allocation -> allocation.getSalesperson().getId().equals(salespersonId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Status-based queries

    public List<AllocationDTO> getAllocationsByStatus(AllocationStatus status) {
        return allocationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByDateAndStatus(LocalDate date, AllocationStatus status) {
        return allocationRepository.findByAllocationDateWithDetails(date).stream()
                .filter(allocation -> allocation.getStatus().equals(status))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // End of Day Processing
    public List<AllocationDTO> processEndOfDayrr(Long salespersonId, List<EndOfDayProcessingDTO> processingData) {
        List<Allocation> allocations = allocationRepository.findBySalespersonIdAndStatus(salespersonId, AllocationStatus.ALLOCATED);

        BigDecimal totalNewSales = BigDecimal.ZERO;

        for (EndOfDayProcessingDTO processing : processingData) {
            Optional<Allocation> allocationOpt = allocations.stream()
                    .filter(a -> a.getId().equals(processing.getAllocationId()))
                    .findFirst();

            if (allocationOpt.isPresent()) {
                Allocation allocation = allocationOpt.get();
                allocation.setSoldQuantity(processing.getSoldQuantity());
                allocation.setPaymentReceived(processing.getPaymentReceived());

                // Update status based on sold quantity
                if (processing.getSoldQuantity() > 0) {
                    allocation.setStatus(AllocationStatus.SOLD);
                } else {
                    allocation.setStatus(AllocationStatus.RETURNED);
                }

                // Return unsold items to stock
                Double returnedQuantity = allocation.getQuantity() - processing.getSoldQuantity();
                if (returnedQuantity > 0) {
                    Inventory item = allocation.getItem();
                    item.setStock(item.getStock() + returnedQuantity);
                    itemRepository.save(item);
                }

                totalNewSales = totalNewSales.add(processing.getPaymentReceived());
                allocationRepository.save(allocation);
            }
        }

        // Update salesperson total sales
        Optional<SalesPersons> salespersonOpt = salespersonRepository.findById(salespersonId);
        if (salespersonOpt.isPresent()) {
            SalesPersons salesperson = salespersonOpt.get();
            salesperson.setTotalSales(salesperson.getTotalSales().add(totalNewSales));
            salespersonRepository.save(salesperson);
        }

        return getAllocationsBySalesperson(salespersonId);
    }

    public ResponseEntity<?> processEndOfDay(Long salespersonId, List<EndOfDayProcessingDTO> processingData) {
        List<Allocation> allocations = allocationRepository
                .findBySalespersonIdAndStatus(salespersonId, AllocationStatus.ALLOCATED);

        Map<Long, Allocation> allocationMap = allocations.stream()
                .collect(Collectors.toMap(Allocation::getId, a -> a));

        BigDecimal totalNewSales = BigDecimal.ZERO;
        int totalItemsProcessed = 0;

        for (EndOfDayProcessingDTO processing : processingData) {
            Allocation allocation = allocationMap.get(processing.getAllocationId());

            if (allocation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Allocation not found or not in ALLOCATED status: " + processing.getAllocationId());
            }

            if (processing.getSoldQuantity() > allocation.getQuantity()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Sold quantity cannot exceed allocated quantity for allocation ID: " +
                                processing.getAllocationId());
            }

            allocation.setSoldQuantity(processing.getSoldQuantity());
            allocation.setPaymentReceived(processing.getPaymentReceived());

            // Update status
            allocation.setStatus(processing.getSoldQuantity() > 0
                    ? AllocationStatus.SOLD
                    : AllocationStatus.RETURNED);

            // Return unsold items to stock
            Double returnedQuantity = allocation.getQuantity() - processing.getSoldQuantity();
            if (returnedQuantity > 0) {
                Inventory item = allocation.getItem();
                item.setStock(item.getStock() + returnedQuantity);
                itemRepository.save(item);
            }

            totalNewSales = totalNewSales.add(processing.getPaymentReceived());
            totalItemsProcessed += processing.getSoldQuantity();
            allocationRepository.save(allocation);
        }

        // Update salesperson
        Optional<SalesPersons> salespersonOpt = salespersonRepository.findById(salespersonId);
        if (salespersonOpt.isPresent()) {
            SalesPersons salesperson = salespersonOpt.get();
            salesperson.setTotalSales(salesperson.getTotalSales().add(totalNewSales));

            Double totalQuantityProcessed = processingData.stream()
                    .mapToDouble(p -> {
                        Allocation a = allocationMap.get(p.getAllocationId());
                        return a != null ? a.getQuantity() : 0;
                    }).sum();

            salesperson.setItemsAllocated(
                    Math.max(0, salesperson.getItemsAllocated() - totalQuantityProcessed)
            );
            salespersonRepository.save(salesperson);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Salesperson not found with ID: " + salespersonId);
        }

        // Final updated allocation list
        List<AllocationDTO> updatedAllocations = getAllocationsBySalesperson(salespersonId);
        return ResponseEntity.ok(updatedAllocations);
    }

    // Summary and Analytics

    public DailyAllocationSummaryDTO getDailyAllocationSummary(LocalDate date) {
        List<Allocation> allocations = allocationRepository.findByAllocationDateWithDetails(date);

        DailyAllocationSummaryDTO summary = new DailyAllocationSummaryDTO(date);

        if (allocations.isEmpty()) {
            return summary;
        }

        summary.setTotalAllocations(allocations.size());
        summary.setTotalQuantityAllocated(allocations.stream()
                .mapToDouble(Allocation::getQuantity)
                .sum());

        summary.setTotalSold(allocations.stream()
                .mapToDouble(a -> a.getSoldQuantity() != null ? a.getSoldQuantity() : 0)
                .sum());

        summary.setTotalReturned(allocations.stream()
                .mapToDouble(a -> {
                    Double sold = a.getSoldQuantity() != null ? a.getSoldQuantity() : 0;
                    return a.getQuantity() - sold;
                })
                .sum());

        summary.setTotalRevenue(allocations.stream()
                .map(a -> a.getPaymentReceived() != null ? a.getPaymentReceived() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        summary.setUniqueSalespeople((int) allocations.stream()
                .map(a -> a.getSalesperson().getId())
                .distinct()
                .count());

        summary.setUniqueItems((int) allocations.stream()
                .map(a -> a.getItem().getId())
                .distinct()
                .count());

        return summary;
    }

    public List<DailyAllocationSummaryDTO> getDailyAllocationSummaries(LocalDate startDate, LocalDate endDate) {
        List<DailyAllocationSummaryDTO> summaries = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            summaries.add(getDailyAllocationSummary(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        return summaries;
    }

    public DailyAllocationSummaryDTO getTodayAllocationSummary() {
        return getDailyAllocationSummary(LocalDate.now());
    }

    // Utility methods for analytics

    public List<AllocationDTO> getPendingAllocations() {
        return getAllocationsByStatus(AllocationStatus.ALLOCATED);
    }

    public List<AllocationDTO> getCompletedAllocations() {
        return getAllocationsByStatus(AllocationStatus.SOLD);
    }

    public List<AllocationDTO> getReturnedAllocations() {
        return getAllocationsByStatus(AllocationStatus.RETURNED);
    }

    public List<AllocationDTO> getAllocationsByItem(Long itemId) {
        return allocationRepository.findAllWithDetails().stream()
                .filter(allocation -> allocation.getItem().getId().equals(itemId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return allocationRepository.findByAllocationDateBetweenWithDetails(startDate, endDate).stream()
                .map(a -> a.getPaymentReceived() != null ? a.getPaymentReceived() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRevenueBySalesperson(Long salespersonId) {
        return allocationRepository.findBySalespersonId(salespersonId).stream()
                .map(a -> a.getPaymentReceived() != null ? a.getPaymentReceived() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Double getTotalQuantitySoldByItem(Long itemId) {
        return allocationRepository.findAllWithDetails().stream()
                .filter(allocation -> allocation.getItem().getId().equals(itemId))
                .mapToDouble(a -> a.getSoldQuantity() != null ? a.getSoldQuantity() : 0)
                .sum();
    }

    // Conversion methods

    private AllocationDTO convertToDTO(Allocation allocation) {
        AllocationDTO dto = new AllocationDTO();
        dto.setId(allocation.getId());
        dto.setSalespersonId(allocation.getSalesperson().getId());
        dto.setSalespersonName(allocation.getSalesperson().getName());
        dto.setItemId(allocation.getItem().getId());
        dto.setItemName(allocation.getItem().getName());
        dto.setItemPrice(allocation.getItem().getPrice());
        dto.setQuantity(allocation.getQuantity());
        dto.setAllocationDate(allocation.getAllocationDate());
        dto.setStatus(allocation.getStatus());
        dto.setSoldQuantity(allocation.getSoldQuantity());
        dto.setPaymentReceived(allocation.getPaymentReceived());
        dto.setCreatedAt(allocation.getCreatedAt());
        dto.setUpdatedAt(allocation.getUpdatedAt());
        return dto;
    }

    private Allocation convertToEntity(AllocationDTO dto) {
        Allocation allocation = new Allocation();

        if (dto.getSalespersonId() != null) {
            SalesPersons salesperson = salespersonRepository.findById(dto.getSalespersonId())
                    .orElseThrow(() -> new RuntimeException("Salesperson not found"));
            allocation.setSalesperson(salesperson);
        }

        if (dto.getItemId() != null) {
            Inventory item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            allocation.setItem(item);
        }

        allocation.setQuantity(dto.getQuantity());
        allocation.setAllocationDate(dto.getAllocationDate());
        allocation.setStatus(dto.getStatus() != null ? dto.getStatus() : AllocationStatus.ALLOCATED);
        allocation.setSoldQuantity(dto.getSoldQuantity());
        allocation.setPaymentReceived(dto.getPaymentReceived());

        return allocation;
    }

}