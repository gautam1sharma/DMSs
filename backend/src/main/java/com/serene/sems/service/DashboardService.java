package com.serene.sems.service;

import com.serene.sems.dto.DashboardSummaryResponse;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Order;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DealerRepository dealerRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final DealerService dealerService;

    public DashboardService(
            DealerRepository dealerRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            DealerService dealerService) {
        this.dealerRepository = dealerRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.dealerService = dealerService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse adminSummary() {
        DashboardSummaryResponse d = new DashboardSummaryResponse();
        d.setDealerCount(dealerRepository.count());
        d.setCustomerCount(customerRepository.count());
        d.setOrderCount(orderRepository.count());
        d.setRevenueTotal(orderRepository.sumTotalAmount());
        List<Order> recent = orderRepository.findTop5ByOrderByOrderDateDesc();
        d.setRecentOrders(recent.stream().map(orderService::toResponse).collect(Collectors.toList()));
        return d;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dealerSummary() {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        DashboardSummaryResponse d = new DashboardSummaryResponse();
        d.setDealerCount(1);
        d.setCustomerCount(customerRepository.countByDealerId(dealer.getId()));
        d.setOrderCount(orderRepository.countByDealerId(dealer.getId()));
        d.setRevenueTotal(orderRepository.sumTotalAmountByDealerId(dealer.getId()));
        List<Order> recent = orderRepository.findByDealerId(dealer.getId(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderDate"))).getContent();
        d.setRecentOrders(recent.stream().map(orderService::toResponse).collect(Collectors.toList()));
        return d;
    }
}
