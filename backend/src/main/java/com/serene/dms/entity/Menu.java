package com.serene.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menus",
    indexes = {
        @Index(name = "idx_menu_parent", columnList = "parent_id"),
        @Index(name = "idx_menu_sort", columnList = "sort_order")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @Column(name = "label", nullable = false, length = 80)
    private String label;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "url", length = 200)
    private String url;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Comma-separated role names: "ADMIN,DEALER,CUSTOMER"
    @Column(name = "roles", length = 100)
    private String roles;
}
