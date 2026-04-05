package com.serene.dms.service;

import com.serene.dms.dto.request.CreateDealerRequest;
import com.serene.dms.dto.response.DealerResponse;
import com.serene.dms.entity.Dealer;
import com.serene.dms.entity.User;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.CustomerRepository;
import com.serene.dms.repository.DealerRepository;
import com.serene.dms.repository.UserRepository;
import com.serene.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public DealerResponse createDealer(CreateDealerRequest req) {
        if (dealerRepository.existsByCode(req.getCode())) {
            throw AppException.conflict("Dealer code already exists: " + req.getCode());
        }

        User owner = null;
        if (req.getUserId() != null) {
            owner = userRepository.findById(req.getUserId())
                .orElseThrow(() -> AppException.notFound("User", req.getUserId()));
        }

        Dealer dealer = Dealer.builder()
            .name(req.getName())
            .code(req.getCode().toUpperCase())
            .address(req.getAddress())
            .city(req.getCity())
            .state(req.getState())
            .phone(req.getPhone())
            .email(req.getEmail())
            .user(owner)
            .build();

        dealer = dealerRepository.save(dealer);
        log.info("Dealer created: {} ({})", dealer.getName(), dealer.getCode());
        return toResponse(dealer);
    }

    @Transactional(readOnly = true)
    public Page<DealerResponse> getAllDealers(Pageable pageable) {
        return dealerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DealerResponse getDealerById(Long id) {
        return dealerRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> AppException.notFound("Dealer", id));
    }

    @Transactional
    public DealerResponse updateDealer(Long id, CreateDealerRequest req) {
        Dealer dealer = dealerRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Dealer", id));

        if (!dealer.getCode().equals(req.getCode()) && dealerRepository.existsByCode(req.getCode())) {
            throw AppException.conflict("Dealer code already exists: " + req.getCode());
        }

        dealer.setName(req.getName());
        dealer.setCode(req.getCode().toUpperCase());
        dealer.setAddress(req.getAddress());
        dealer.setCity(req.getCity());
        dealer.setState(req.getState());
        dealer.setPhone(req.getPhone());
        dealer.setEmail(req.getEmail());

        if (req.getUserId() != null) {
            User owner = userRepository.findById(req.getUserId())
                .orElseThrow(() -> AppException.notFound("User", req.getUserId()));
            dealer.setUser(owner);
        }

        return toResponse(dealerRepository.save(dealer));
    }

    @Transactional
    public void toggleStatus(Long id, String status) {
        Dealer dealer = dealerRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Dealer", id));
        dealer.setStatus(Dealer.DealerStatus.valueOf(status));
        dealerRepository.save(dealer);
    }

    @Transactional
    public void deleteDealer(Long id) {
        if (!dealerRepository.existsById(id)) throw AppException.notFound("Dealer", id);
        dealerRepository.deleteById(id);
    }

    private DealerResponse toResponse(Dealer d) {
        String ownerName = d.getUser() != null
            ? d.getUser().getFirstName() + " " + d.getUser().getLastName()
            : null;
        long custCount = customerRepository.countByDealerId(d.getId());
        long vehCount  = vehicleRepository.countByDealerIdAndStatus(d.getId(), com.serene.dms.entity.Vehicle.VehicleStatus.AVAILABLE);
        return DealerResponse.builder()
            .id(d.getId())
            .name(d.getName())
            .code(d.getCode())
            .address(d.getAddress())
            .city(d.getCity())
            .state(d.getState())
            .phone(d.getPhone())
            .email(d.getEmail())
            .status(d.getStatus().name())
            .userId(d.getUser() != null ? d.getUser().getId() : null)
            .ownerName(ownerName)
            .customerCount(custCount)
            .vehicleCount(vehCount)
            .createdAt(d.getCreatedAt())
            .createdBy(d.getCreatedBy())
            .build();
    }
}
