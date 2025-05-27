package com.smartapps.shop.Services;

import com.smartapps.shop.Models.SalesPersons;
import com.smartapps.shop.Models.dtos.SalespersonDTO;
import com.smartapps.shop.Repos.SalespersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalespersonService {

    @Autowired
    private SalespersonRepository salespersonRepository;

    public List<SalespersonDTO> getAllSalespeople() {
        return salespersonRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<SalespersonDTO> getSalespersonById(Long id) {
        return salespersonRepository.findById(id)
                .map(this::convertToDTO);
    }

    public SalespersonDTO createSalesperson(SalespersonDTO salespersonDTO) {
        SalesPersons salesperson = convertToEntity(salespersonDTO);
        SalesPersons savedSalesperson = salespersonRepository.save(salesperson);
        return convertToDTO(savedSalesperson);
    }

    public Optional<SalespersonDTO> updateSalesperson(Long id, SalespersonDTO salespersonDTO) {
        return salespersonRepository.findById(id)
                .map(existingSalesperson -> {
                    existingSalesperson.setName(salespersonDTO.getName());
                    existingSalesperson.setPhone(salespersonDTO.getPhone());
                    return convertToDTO(salespersonRepository.save(existingSalesperson));
                });
    }

    public boolean deleteSalesperson(Long id) {
        if (salespersonRepository.existsById(id)) {
            salespersonRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<SalespersonDTO> searchSalespeople(String name) {
        return salespersonRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SalespersonDTO> getTopPerformers() {
        return salespersonRepository.findAllOrderByTotalSalesDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SalespersonDTO convertToDTO(SalesPersons salesperson) {
        SalespersonDTO dto = new SalespersonDTO();
        dto.setId(salesperson.getId());
        dto.setName(salesperson.getName());
        dto.setPhone(salesperson.getPhone());
        dto.setTotalSales(salesperson.getTotalSales());
        dto.setItemsAllocated(salesperson.getItemsAllocated());
        dto.setCreatedAt(salesperson.getCreatedAt());
        dto.setUpdatedAt(salesperson.getUpdatedAt());
        return dto;
    }

    private SalesPersons convertToEntity(SalespersonDTO dto) {
        SalesPersons salesperson = new SalesPersons();
        salesperson.setName(dto.getName());
        salesperson.setPhone(dto.getPhone());
        return salesperson;
    }}
