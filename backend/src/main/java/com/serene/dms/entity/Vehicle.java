package com.serene.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicles",
    indexes = {
        @Index(name = "idx_vehicle_dealer", columnList = "dealer_id"),
        @Index(name = "idx_vehicle_vin", columnList = "vin", unique = true),
        @Index(name = "idx_vehicle_status", columnList = "status")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "variant", length = 80)
    private String variant;

    @Column(name = "vin", unique = true, length = 17)
    private String vin;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "mileage", length = 30)
    private String mileage;

    @Column(name = "fuel_type", length = 30)
    private String fuelType;

    @Column(name = "transmission", length = 30)
    private String transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public enum VehicleStatus {
        AVAILABLE, RESERVED, SOLD
    }
}
