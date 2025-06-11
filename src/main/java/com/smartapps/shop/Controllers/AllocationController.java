package com.smartapps.shop.Controllers;


import com.smartapps.shop.Models.Allocation;
import com.smartapps.shop.Models.SalesPersons;
import com.smartapps.shop.Models.dtos.AllocationDTO;
import com.smartapps.shop.Models.dtos.DailyAllocationSummaryDTO;
import com.smartapps.shop.Models.dtos.EndOfDayProcessingDTO;
import com.smartapps.shop.Services.AllocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.smartapps.shop.Models.Allocation.*;

@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "*")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @GetMapping
    public ResponseEntity<List<AllocationDTO>> getAllAllocations() {
        List<AllocationDTO> allocations = allocationService.getAllAllocations();
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<AllocationDTO>> getAllocationById(@PathVariable Long id) {
        Optional<AllocationDTO> allocation = allocationService.getAllocationById(id);
        if (allocation != null) {
            return ResponseEntity.ok(allocation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createAllocation(@RequestBody AllocationDTO allocationDTO) {
        ResponseEntity<?> createdAllocation = allocationService.createAllocation(allocationDTO);
        return ResponseEntity.ok(createdAllocation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAllocation(@PathVariable Long id, @RequestBody AllocationDTO allocationDTO) {
        ResponseEntity<?> updatedAllocation = allocationService.updateAllocation(id, allocationDTO);
        if (updatedAllocation != null) {
            return ResponseEntity.ok(updatedAllocation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAllocation(@PathVariable Long id) {
        allocationService.deleteAllocation(id);
        return ResponseEntity.ok("Allocation deleted Successfully");
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByDate(@PathVariable String date) {
        try {
            LocalDate allocationDate = LocalDate.parse(date);
            List<AllocationDTO> allocations = allocationService.getAllocationsByDate(allocationDate);
            return ResponseEntity.ok(allocations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<AllocationDTO> allocations = allocationService.getAllocationsByDateRange(start, end);
            return ResponseEntity.ok(allocations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/dates")
    public ResponseEntity<List<String>> getDistinctAllocationDates() {
        List<String> dates = allocationService.getDistinctAllocationDates().stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dates);
    }

    @GetMapping("/date/{date}/salesperson/{salespersonId}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByDateAndSalesperson(
            @PathVariable String date,
            @PathVariable Long salespersonId) {
        try {
            LocalDate allocationDate = LocalDate.parse(date);
            List<AllocationDTO> allocations = allocationService.getAllocationsByDateAndSalesperson(allocationDate, salespersonId);
            return ResponseEntity.ok(allocations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date/{date}/status/{status}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByDateAndStatus(
            @PathVariable String date,
            @PathVariable AllocationStatus status) {
        try {
            LocalDate allocationDate = LocalDate.parse(date);
            List<AllocationDTO> allocations = allocationService.getAllocationsByDateAndStatus(allocationDate, status);
            return ResponseEntity.ok(allocations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<List<AllocationDTO>> getTodayAllocations() {
        LocalDate today = LocalDate.now();
        List<AllocationDTO> allocations = allocationService.getAllocationsByDate(today);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/summary/date/{date}")
    public ResponseEntity<DailyAllocationSummaryDTO> getDailyAllocationSummary(@PathVariable String date) {
        try {
            LocalDate allocationDate = LocalDate.parse(date);
            DailyAllocationSummaryDTO summary = allocationService.getDailyAllocationSummary(allocationDate);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/summary/date-range")
    public ResponseEntity<List<DailyAllocationSummaryDTO>> getDailyAllocationSummaries(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<DailyAllocationSummaryDTO> summaries = allocationService.getDailyAllocationSummaries(start, end);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/summary/today")
    public ResponseEntity<DailyAllocationSummaryDTO> getTodayAllocationSummary() {
        LocalDate today = LocalDate.now();
        DailyAllocationSummaryDTO summary = allocationService.getDailyAllocationSummary(today);
        return ResponseEntity.ok(summary);
    }
    @PostMapping("/end-of-day/{salespersonId}")
    public ResponseEntity<?> processEndOfDay(
            @PathVariable Long salespersonId,
            @RequestBody List<EndOfDayProcessingDTO> processingData) {
        return allocationService.processEndOfDay(salespersonId, processingData);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createAllocations(@RequestBody List<AllocationDTO> allocationDTOs) {

        ResponseEntity<?> createdAllocation = allocationService.createAllocations(allocationDTOs);
        return ResponseEntity.ok(createdAllocation);

    }
}
