package com.serene.dms.service;

import com.serene.dms.dto.request.CreateVehicleRequest;
import com.serene.dms.entity.Dealer;
import com.serene.dms.entity.Vehicle;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.DealerRepository;
import com.serene.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;

    @Transactional
    public Map<String, Object> createVehicle(CreateVehicleRequest req) {
        Dealer dealer = dealerRepository.findById(req.getDealerId())
            .orElseThrow(() -> AppException.notFound("Dealer", req.getDealerId()));

        if (req.getVin() != null && !req.getVin().isBlank() && vehicleRepository.existsByVin(req.getVin())) {
            throw AppException.conflict("VIN already exists: " + req.getVin());
        }

        Vehicle vehicle = Vehicle.builder()
            .dealer(dealer)
            .model(req.getModel())
            .variant(req.getVariant())
            .vin(req.getVin())
            .year(req.getYear())
            .color(req.getColor())
            .price(req.getPrice())
            .mileage(req.getMileage())
            .fuelType(req.getFuelType())
            .transmission(req.getTransmission())
            .description(req.getDescription())
            .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created: {} {} for dealer {}", vehicle.getYear(), vehicle.getModel(), dealer.getCode());
        return toMap(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getVehiclesByDealer(Long dealerId, Pageable pageable) {
        return vehicleRepository.findByDealerId(dealerId, pageable).map(this::toMap);
    }

    @Transactional
    public Map<String, Object> updateVehicle(Long id, CreateVehicleRequest req) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Vehicle", id));

        vehicle.setModel(req.getModel());
        vehicle.setVariant(req.getVariant());
        vehicle.setYear(req.getYear());
        vehicle.setColor(req.getColor());
        vehicle.setPrice(req.getPrice());
        vehicle.setFuelType(req.getFuelType());
        vehicle.setTransmission(req.getTransmission());
        vehicle.setDescription(req.getDescription());

        return toMap(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) throw AppException.notFound("Vehicle", id);
        vehicleRepository.deleteById(id);
    }

    private Map<String, Object> toMap(Vehicle v) {
        return Map.ofEntries(
            Map.entry("id",           v.getId()),
            Map.entry("model",        v.getModel()),
            Map.entry("variant",      v.getVariant() != null ? v.getVariant() : ""),
            Map.entry("vin",          v.getVin() != null ? v.getVin() : ""),
            Map.entry("year",         v.getYear()),
            Map.entry("color",        v.getColor() != null ? v.getColor() : ""),
            Map.entry("price",        v.getPrice()),
            Map.entry("fuelType",     v.getFuelType() != null ? v.getFuelType() : ""),
            Map.entry("transmission", v.getTransmission() != null ? v.getTransmission() : ""),
            Map.entry("status",       v.getStatus().name()),
            Map.entry("dealerId",     v.getDealer().getId()),
            Map.entry("dealerName",   v.getDealer().getName()),
            Map.entry("createdAt",    v.getCreatedAt() != null ? v.getCreatedAt().toString() : ""),
            Map.entry("description",  v.getDescription() != null ? v.getDescription() : "")
        );
    }
}
