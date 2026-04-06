package com.serene.sems.service;

import com.serene.sems.dto.CreateOrderRequest;
import com.serene.sems.dto.OrderItemRequest;
import com.serene.sems.dto.OrderItemResponse;
import com.serene.sems.dto.OrderResponse;
import com.serene.sems.dto.UpdateOrderStatusRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Customer;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Order;
import com.serene.sems.model.OrderItem;
import com.serene.sems.model.OrderStatus;
import com.serene.sems.model.Product;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.OrderRepository;
import com.serene.sems.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final DealerRepository dealerRepository;
    private final ProductRepository productRepository;
    private final DealerService dealerService;
    private final AuditService auditService;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            DealerRepository dealerRepository,
            ProductRepository productRepository,
            DealerService dealerService,
            AuditService auditService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.dealerRepository = dealerRepository;
        this.productRepository = productRepository;
        this.dealerService = dealerService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<OrderResponse> listAdmin(Long dealerId, OrderStatus status, Pageable pageable) {
        if (dealerId != null && status != null) {
            return orderRepository.findByDealerIdAndStatus(dealerId, status, pageable).map(this::toResponse);
        }
        if (dealerId != null) {
            return orderRepository.findByDealerId(dealerId, pageable).map(this::toResponse);
        }
        if (status != null) {
            return orderRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public OrderResponse getAdmin(Long id) {
        return orderRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponse createAdmin(CreateOrderRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Dealer dealer;
        if (req.getDealerId() != null) {
            dealer = dealerRepository.findById(req.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
            if (!dealer.isActive()) {
                throw new IllegalArgumentException("Cannot create order: selected dealer is inactive");
            }
            if (customer.getDealer() != null && !customer.getDealer().getId().equals(dealer.getId())) {
                throw new IllegalArgumentException("Customer does not belong to selected dealer");
            }
        } else {
            dealer = customer.getDealer();
        }
        if (dealer == null) {
            throw new IllegalArgumentException("Customer has no assigned dealer; choose a dealer for this order.");
        }
        if (!dealer.isActive()) {
            throw new IllegalArgumentException("Cannot create order: customer's assigned dealer is inactive");
        }
        Order saved = persistOrder(customer, dealer, req.getItems());
        auditService.record(
                AuditAction.ORDER_CREATED,
                true,
                "Order " + saved.getOrderNumber() + " total " + saved.getTotalAmount(),
                "ORDER",
                saved.getId(),
                null,
                null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponse updateStatusAdmin(Long id, UpdateOrderStatusRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus previous = order.getStatus();
        previous.validateTransitionTo(req.getStatus());
        order.setStatus(req.getStatus());
        if (req.getStatus() == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        Order saved = orderRepository.save(order);
        auditService.record(
                AuditAction.ORDER_STATUS_CHANGED,
                true,
                order.getOrderNumber() + ": " + previous + " -> " + req.getStatus(),
                "ORDER",
                id,
                null,
                null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<OrderResponse> listDealer(OrderStatus status, Pageable pageable) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        if (status != null) {
            return orderRepository.findByDealerIdAndStatus(dealer.getId(), status, pageable).map(this::toResponse);
        }
        return orderRepository.findByDealerId(dealer.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public OrderResponse getDealer(Long id) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getDealer() == null || !order.getDealer().getId().equals(dealer.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        return toResponse(order);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponse createDealer(CreateOrderRequest req) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getDealer() == null || !customer.getDealer().getId().equals(dealer.getId())) {
            throw new IllegalArgumentException("Customer not found");
        }
        Order saved = persistOrder(customer, dealer, req.getItems());
        auditService.record(
                AuditAction.ORDER_CREATED,
                true,
                "Order " + saved.getOrderNumber() + " total " + saved.getTotalAmount(),
                "ORDER",
                saved.getId(),
                null,
                null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponse updateStatusDealer(Long id, UpdateOrderStatusRequest req) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getDealer() == null || !order.getDealer().getId().equals(dealer.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        OrderStatus previous = order.getStatus();
        previous.validateTransitionTo(req.getStatus());
        order.setStatus(req.getStatus());
        if (req.getStatus() == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        Order saved = orderRepository.save(order);
        auditService.record(
                AuditAction.ORDER_STATUS_CHANGED,
                true,
                order.getOrderNumber() + ": " + previous + " -> " + req.getStatus(),
                "ORDER",
                id,
                null,
                null);
        return toResponse(saved);
    }

    /**
     * Restores stock for every item in a cancelled order using pessimistic locks to avoid lost updates.
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProduct().getId()));
            product.setStockQty(product.getStockQty() + item.getQuantity());
            productRepository.save(product);
        }
    }

    /** Pessimistic locks on product rows; runs in caller's transaction (see createAdmin/createDealer). */
    protected Order persistOrder(Customer customer, Dealer dealer, List<OrderItemRequest> itemReqs) {
        if (!dealer.isActive()) {
            throw new IllegalArgumentException("Cannot create order: dealer is inactive");
        }
        Order order = new Order();
        order.setCustomer(customer);
        order.setDealer(dealer);
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest line : itemReqs) {
            Product product = productRepository.findByIdForUpdate(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + line.getProductId()));
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product inactive: " + product.getName());
            }
            if (product.getStockQty() < line.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName());
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(line.getQuantity());
            oi.setUnitPrice(product.getPrice());
            items.add(oi);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            product.setStockQty(product.getStockQty() - line.getQuantity());
            productRepository.save(product);
        }
        order.setItems(items);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setOrderNumber(o.getOrderNumber());
        r.setCustomerId(o.getCustomer().getId());
        r.setCustomerName(o.getCustomer().getFullName());
        if (o.getDealer() != null) {
            r.setDealerId(o.getDealer().getId());
            r.setDealerCompanyName(o.getDealer().getCompanyName());
        } else {
            r.setDealerId(null);
            r.setDealerCompanyName(null);
        }
        r.setTotalAmount(o.getTotalAmount());
        r.setStatus(o.getStatus());
        r.setOrderDate(o.getOrderDate());
        r.setItems(o.getItems().stream().map(this::itemToResponse).collect(Collectors.toList()));
        return r;
    }

    private OrderItemResponse itemToResponse(OrderItem i) {
        OrderItemResponse r = new OrderItemResponse();
        r.setId(i.getId());
        r.setProductId(i.getProduct().getId());
        r.setProductName(i.getProduct().getName());
        r.setQuantity(i.getQuantity());
        r.setUnitPrice(i.getUnitPrice());
        return r;
    }
}
