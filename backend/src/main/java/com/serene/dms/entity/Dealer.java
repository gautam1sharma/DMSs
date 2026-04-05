package com.serene.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dealers",
    indexes = {
        @Index(name = "idx_dealer_code", columnList = "code", unique = true),
        @Index(name = "idx_dealer_user", columnList = "user_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dealer extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DealerStatus status = DealerStatus.ACTIVE;

    // The user account that owns/manages this dealer profile
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public enum DealerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
