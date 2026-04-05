package com.serene.sems.service;

import com.serene.sems.dto.CreateProductRequest;
import com.serene.sems.dto.ProductResponse;
import com.serene.sems.dto.UpdateProductRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Product;
import com.serene.sems.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, AuditService auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String category, String q, Pageable pageable) {
        if (category != null && !category.isBlank() && q != null && !q.isBlank()) {
            return productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(category.trim(), q.trim(), pageable)
                    .map(this::toResponse);
        }
        if (category != null && !category.isBlank()) {
            return productRepository.findByCategoryIgnoreCase(category.trim(), pageable).map(this::toResponse);
        }
        if (q != null && !q.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(q.trim(), pageable).map(this::toResponse);
        }
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return productRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQty(req.getStockQty());
        p.setCategory(req.getCategory());
        p.setActive(req.isActive());
        Product saved = productRepository.save(p);
        auditService.record(
                AuditAction.PRODUCT_CREATED, true, saved.getName(), "PRODUCT", saved.getId(), null, null);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (req.getName() != null) {
            p.setName(req.getName());
        }
        if (req.getDescription() != null) {
            p.setDescription(req.getDescription());
        }
        if (req.getPrice() != null) {
            p.setPrice(req.getPrice());
        }
        if (req.getStockQty() != null) {
            p.setStockQty(req.getStockQty());
        }
        if (req.getCategory() != null) {
            p.setCategory(req.getCategory());
        }
        if (req.getActive() != null) {
            p.setActive(req.getActive());
        }
        Product saved = productRepository.save(p);
        auditService.record(AuditAction.PRODUCT_UPDATED, true, null, "PRODUCT", id, null, null);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        auditService.record(AuditAction.PRODUCT_DELETED, true, null, "PRODUCT", id, null, null);
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setStockQty(p.getStockQty());
        r.setCategory(p.getCategory());
        r.setActive(p.isActive());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
