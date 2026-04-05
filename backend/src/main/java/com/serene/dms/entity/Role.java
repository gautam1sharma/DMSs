package com.serene.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles",
    indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;  // ADMIN, DEALER, CUSTOMER

    @Column(name = "description", length = 200)
    private String description;
}
