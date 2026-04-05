package com.serene.sems.config;

import com.serene.sems.model.Product;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.ProductRepository;
import com.serene.sems.repository.RoleRepository;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedRolesAndAdmin(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder,
            IndianDemoBulkSeed indianDemoBulkSeed,
            MenuService menuService) {
        return args -> {
            ensureRole(roleRepository, "ADMIN");
            ensureRole(roleRepository, "DEALER");
            ensureRole(roleRepository, "CUSTOMER");

            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@serene.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                log.info("Seeded default admin user (admin / admin123)");
            }

            migrateLegacyDemoProducts(productRepository);
            seedCatalogIfEmpty(productRepository);
            ensureSereneVehicleCatalog(productRepository);

            indianDemoBulkSeed.ensureIndianDemoSeeded(passwordEncoder);
            menuService.ensureDefaultMenusIfEmpty();
            menuService.ensureAdminMenusCrudLink();
        };
    }

    /**
     * Renames legacy placeholder SKUs (purifiers, bundles, kits) to Serene vehicle line-up
     * so existing DBs update in place without breaking order line FKs.
     */
    private void migrateLegacyDemoProducts(ProductRepository productRepository) {
        renameIfPresent(
                productRepository,
                "Serene Air Purifier Pro",
                "Serene Compact",
                "Serene compact city car — efficient everyday mobility.",
                new BigDecimal("649000.00"),
                18,
                "Hatchback");
        renameIfPresent(
                productRepository,
                "Serene Humidifier",
                "Serene Sedan",
                "Serene mid-size sedan — comfort and balance for daily drives.",
                new BigDecimal("899000.00"),
                14,
                "Sedan");
        renameIfPresent(
                productRepository,
                "Serene Essential Oil Set",
                "Serene Estate",
                "Serene estate — versatile cabin and load space for families.",
                new BigDecimal("949000.00"),
                12,
                "Estate");
        renameIfPresent(
                productRepository,
                "Serene Signature Bundle",
                "Serene Cross",
                "Serene crossover — raised stance and practical cabin layout.",
                new BigDecimal("1099000.00"),
                16,
                "Crossover");
        renameIfPresent(
                productRepository,
                "Serene Everyday Kit",
                "Serene Sport",
                "Serene sport tourer — responsive drive with Serene refinement.",
                new BigDecimal("1249000.00"),
                10,
                "Sport");
        renameIfPresent(
                productRepository,
                "Serene Refill Pack",
                "Serene Executive",
                "Serene executive saloon — quiet cabin and long-distance comfort.",
                new BigDecimal("1499000.00"),
                8,
                "Executive");
    }

    private void renameIfPresent(
            ProductRepository repo,
            String oldName,
            String newName,
            String description,
            BigDecimal price,
            int stockQty,
            String category) {
        repo.findByName(oldName).ifPresent(p -> {
            p.setName(newName);
            p.setDescription(description);
            p.setPrice(price);
            p.setStockQty(stockQty);
            p.setCategory(category);
            repo.save(p);
            log.info("Migrated legacy demo product '{}' -> '{}'", oldName, newName);
        });
    }

    private void seedCatalogIfEmpty(ProductRepository productRepository) {
        if (productRepository.count() != 0) {
            return;
        }
        for (SereneVehicle v : SereneVehicle.values()) {
            seedProduct(productRepository, v.name, v.description, v.price, v.stock, v.category);
        }
        log.info("Seeded default Serene vehicle catalog ({} SKUs)", SereneVehicle.values().length);
    }

    /** Adds any missing Serene vehicle SKUs (e.g. after migrating older 3-SKU catalogs). */
    private void ensureSereneVehicleCatalog(ProductRepository productRepository) {
        for (SereneVehicle v : SereneVehicle.values()) {
            if (productRepository.findByName(v.name).isEmpty()) {
                seedProduct(productRepository, v.name, v.description, v.price, v.stock, v.category);
                log.info("Seeded missing Serene vehicle '{}'", v.name);
            }
        }
    }

    private enum SereneVehicle {
        COMPACT(
                "Serene Compact",
                "Serene compact city car — efficient everyday mobility.",
                new BigDecimal("649000.00"),
                18,
                "Hatchback"),
        SEDAN(
                "Serene Sedan",
                "Serene mid-size sedan — comfort and balance for daily drives.",
                new BigDecimal("899000.00"),
                14,
                "Sedan"),
        ESTATE(
                "Serene Estate",
                "Serene estate — versatile cabin and load space for families.",
                new BigDecimal("949000.00"),
                12,
                "Estate"),
        CROSS(
                "Serene Cross",
                "Serene crossover — raised stance and practical cabin layout.",
                new BigDecimal("1099000.00"),
                16,
                "Crossover"),
        SPORT(
                "Serene Sport",
                "Serene sport tourer — responsive drive with Serene refinement.",
                new BigDecimal("1249000.00"),
                10,
                "Sport"),
        EXECUTIVE(
                "Serene Executive",
                "Serene executive saloon — quiet cabin and long-distance comfort.",
                new BigDecimal("1499000.00"),
                8,
                "Executive"),
        URBAN_EV(
                "Serene Urban EV",
                "Serene urban electric — zero tailpipe, city-first range and charging.",
                new BigDecimal("1149000.00"),
                15,
                "Electric"),
        GRAND_TOURER(
                "Serene Grand Tourer",
                "Serene grand tourer — long-haul comfort and stable highway manners.",
                new BigDecimal("1699000.00"),
                6,
                "Tourer");

        final String name;
        final String description;
        final BigDecimal price;
        final int stock;
        final String category;

        SereneVehicle(String name, String description, BigDecimal price, int stock, String category) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.category = category;
        }
    }

    private void ensureRole(RoleRepository repo, String name) {
        if (repo.findByName(name).isEmpty()) {
            repo.save(new Role(name));
            log.info("Seeded role {}", name);
        }
    }

    private void seedProduct(ProductRepository repo, String name, String desc, BigDecimal price, int stock, String category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStockQty(stock);
        p.setCategory(category);
        p.setActive(true);
        repo.save(p);
    }
}
