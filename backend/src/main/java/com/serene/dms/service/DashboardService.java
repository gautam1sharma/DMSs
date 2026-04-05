package com.serene.dms.service;

import com.serene.dms.dto.response.DashboardStatsResponse;
import com.serene.dms.entity.Inquiry;
import com.serene.dms.entity.Order;
import com.serene.dms.entity.Vehicle;
import com.serene.dms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final InquiryRepository inquiryRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminStats() {
        long totalDealers  = dealerRepository.count();
        long activeDealers = dealerRepository.findByStatus(com.serene.dms.entity.Dealer.DealerStatus.ACTIVE,
            org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalUsers    = userRepository.count();
        BigDecimal revenue = orderRepository.sumTotalRevenue();

        return DashboardStatsResponse.builder()
            .totalDealers(totalDealers)
            .activeDealers(activeDealers)
            .totalUsers(totalUsers)
            .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .build();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDealerStats(Long dealerId) {
        long customers        = customerRepository.countByDealerId(dealerId);
        long vehicles         = vehicleRepository.countByDealerIdAndStatus(dealerId, Vehicle.VehicleStatus.AVAILABLE);
        long totalOrders      = orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.PENDING)
                              + orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.CONFIRMED)
                              + orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.PROCESSING)
                              + orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.COMPLETED)
                              + orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.CANCELLED);
        long pendingOrders    = orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.PENDING);
        long completedOrders  = orderRepository.countByDealerIdAndStatus(dealerId, Order.OrderStatus.COMPLETED);
        long openInquiries    = inquiryRepository.countByDealerIdAndStatus(dealerId, Inquiry.InquiryStatus.OPEN);
        BigDecimal revenue    = orderRepository.sumCompletedRevenueByDealer(dealerId);

        return DashboardStatsResponse.builder()
            .totalCustomers(customers)
            .totalVehicles(vehicles)
            .availableVehicles(vehicles)
            .totalOrders(totalOrders)
            .pendingOrders(pendingOrders)
            .completedOrders(completedOrders)
            .openInquiries(openInquiries)
            .dealerRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .build();
    }
}
