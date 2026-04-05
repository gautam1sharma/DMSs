package com.serene.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inquiries",
    indexes = {
        @Index(name = "idx_inquiry_dealer", columnList = "dealer_id"),
        @Index(name = "idx_inquiry_customer", columnList = "customer_id"),
        @Index(name = "idx_inquiry_status", columnList = "status")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.OPEN;

    public enum InquiryStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
