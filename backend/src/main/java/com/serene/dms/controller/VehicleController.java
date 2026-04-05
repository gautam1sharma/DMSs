package com.serene.dms.controller;

import com.serene.dms.dto.request.CreateVehicleRequest;
import com.serene.dms.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle inventory management")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Add a vehicle to inventory")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateVehicleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(req));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER', 'CUSTOMER')")
    @Operation(summary = "List vehicles by dealer")
    public ResponseEntity<Page<Map<String, Object>>> listByDealer(
        @PathVariable Long dealerId,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(vehicleService.getVehiclesByDealer(dealerId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Update a vehicle")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody CreateVehicleRequest req) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Remove a vehicle")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
