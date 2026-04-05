package com.serene.dms.service;

import com.serene.dms.dto.request.CreateOrderRequest;
import com.serene.dms.dto.response.OrderResponse;
import com.serene.dms.entity.*;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DealerRepository dealerRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    private final AtomicLong orderSeq = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        Dealer dealer = dealerRepository.findById(req.getDealerId())
            .orElseThrow(() -> AppException.notFound("Dealer", req.getDealerId()));

        Customer customer = customerRepository.findById(req.getCustomerId())
            .orElseThrow(() -> AppException.notFound("Customer", req.getCustomerId()));

        // Ensure customer belongs to this dealer
        if (!customer.getDealer().getId().equals(dealer.getId())) {
            throw AppException.forbidden("Customer does not belong to this dealer");
        }

        Vehicle vehicle = null;
        if (req.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> AppException.notFound("Vehicle", req.getVehicleId()));
        }

        BigDecimal discount = req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = req.getAmount().subtract(discount);

        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .dealer(dealer)
            .customer(customer)
            .vehicle(vehicle)
            .amount(req.getAmount())
            .discount(discount)
            .finalAmount(finalAmount)
            .notes(req.getNotes())
            .build();

        if (vehicle != null) {
            vehicle.setStatus(Vehicle.VehicleStatus.RESERVED);
            vehicleRepository.save(vehicle);
        }

        order = orderRepository.save(order);
        log.info("Order created: {} for customer {} under dealer {}", order.getOrderNumber(), customer.getId(), dealer.getCode());
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByDealer(Long dealerId, Pageable pageable) {
        return orderRepository.findByDealerId(dealerId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Order", id));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
        order.setStatus(newStatus);

        // Update vehicle status when order completes/cancels
        if (order.getVehicle() != null) {
            if (newStatus == Order.OrderStatus.COMPLETED) {
                order.getVehicle().setStatus(Vehicle.VehicleStatus.SOLD);
                vehicleRepository.save(order.getVehicle());
            } else if (newStatus == Order.OrderStatus.CANCELLED) {
                order.getVehicle().setStatus(Vehicle.VehicleStatus.AVAILABLE);
                vehicleRepository.save(order.getVehicle());
            }
        }

        return toResponse(orderRepository.save(order));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "SRN-" + date + "-" + String.format("%05d", orderSeq.incrementAndGet() % 100000);
    }

    private OrderResponse toResponse(Order o) {
        String vehicleInfo = o.getVehicle() != null
            ? o.getVehicle().getYear() + " " + o.getVehicle().getModel()
            : null;
        return OrderResponse.builder()
            .id(o.getId())
            .orderNumber(o.getOrderNumber())
            .dealerId(o.getDealer().getId())
            .dealerName(o.getDealer().getName())
            .customerId(o.getCustomer().getId())
            .customerName(o.getCustomer().getFirstName() + " " + o.getCustomer().getLastName())
            .vehicleId(o.getVehicle() != null ? o.getVehicle().getId() : null)
            .vehicleInfo(vehicleInfo)
            .amount(o.getAmount())
            .discount(o.getDiscount())
            .finalAmount(o.getFinalAmount())
            .status(o.getStatus().name())
            .notes(o.getNotes())
            .createdAt(o.getCreatedAt())
            .createdBy(o.getCreatedBy())
            .build();
    }
}
