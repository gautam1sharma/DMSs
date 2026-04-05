package com.serene.dms.service;

import com.serene.dms.entity.Customer;
import com.serene.dms.entity.Dealer;
import com.serene.dms.entity.Inquiry;
import com.serene.dms.entity.Vehicle;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.CustomerRepository;
import com.serene.dms.repository.DealerRepository;
import com.serene.dms.repository.InquiryRepository;
import com.serene.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final DealerRepository dealerRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public Map<String, Object> createInquiry(Map<String, Object> req) {
        Long dealerId = Long.valueOf(req.get("dealerId").toString());
        Dealer dealer = dealerRepository.findById(dealerId)
            .orElseThrow(() -> AppException.notFound("Dealer", dealerId));

        Inquiry inquiry = Inquiry.builder()
            .dealer(dealer)
            .name(str(req, "name"))
            .email(str(req, "email"))
            .phone(str(req, "phone"))
            .subject(str(req, "subject"))
            .message(str(req, "message"))
            .build();

        if (req.containsKey("customerId") && req.get("customerId") != null) {
            Long customerId = Long.valueOf(req.get("customerId").toString());
            inquiry.setCustomer(customerRepository.findById(customerId).orElse(null));
        }

        inquiry = inquiryRepository.save(inquiry);
        return toMap(inquiry);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getByDealer(Long dealerId, Pageable pageable) {
        return inquiryRepository.findByDealerId(dealerId, pageable).map(this::toMap);
    }

    @Transactional
    public Map<String, Object> respond(Long id, String response) {
        Inquiry inquiry = inquiryRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Inquiry", id));
        inquiry.setResponse(response);
        inquiry.setStatus(Inquiry.InquiryStatus.RESOLVED);
        return toMap(inquiryRepository.save(inquiry));
    }

    @Transactional
    public Map<String, Object> updateStatus(Long id, String status) {
        Inquiry inquiry = inquiryRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Inquiry", id));
        inquiry.setStatus(Inquiry.InquiryStatus.valueOf(status));
        return toMap(inquiryRepository.save(inquiry));
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    private Map<String, Object> toMap(Inquiry i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("dealerId", i.getDealer().getId());
        m.put("dealerName", i.getDealer().getName());
        m.put("customerName", i.getCustomer() != null
            ? i.getCustomer().getFirstName() + " " + i.getCustomer().getLastName() : null);
        m.put("name", i.getName());
        m.put("email", i.getEmail());
        m.put("phone", i.getPhone());
        m.put("subject", i.getSubject());
        m.put("message", i.getMessage());
        m.put("response", i.getResponse());
        m.put("status", i.getStatus().name());
        m.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        return m;
    }
}
