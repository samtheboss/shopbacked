package com.smartapps.shop.Controllers;

import com.smartapps.shop.Models.dtos.SalespersonDTO;
import com.smartapps.shop.Services.SalespersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salespeople")
@CrossOrigin(origins = "*")
public class SalespersonController {

    @Autowired
    private SalespersonService salespersonService;

    @GetMapping
    public ResponseEntity<List<SalespersonDTO>> getAllSalespeople() {
        List<SalespersonDTO> salespeople = salespersonService.getAllSalespeople();
        return ResponseEntity.ok(salespeople);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalespersonDTO> getSalespersonById(@PathVariable Long id) {
        return salespersonService.getSalespersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SalespersonDTO> createSalesperson( @RequestBody SalespersonDTO salespersonDTO) {
        System.out.println(salespersonDTO.toString());
        SalespersonDTO createdSalesperson = salespersonService.createSalesperson(salespersonDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSalesperson);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalespersonDTO> updateSalesperson(@PathVariable Long id,  @RequestBody SalespersonDTO salespersonDTO) {
        return salespersonService.updateSalesperson(id, salespersonDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalesperson(@PathVariable Long id) {

        if (salespersonService.deleteSalesperson(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SalespersonDTO>> searchSalespeople(@RequestParam String name) {
        List<SalespersonDTO> salespeople = salespersonService.searchSalespeople(name);
        return ResponseEntity.ok(salespeople);
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<SalespersonDTO>> getTopPerformers() {
        List<SalespersonDTO> topPerformers = salespersonService.getTopPerformers();
        return ResponseEntity.ok(topPerformers);
    }
    @GetMapping("/keepawake")
    public ResponseEntity<String> keepAwake(){
        return  ResponseEntity.ok("requested");
    }
}
