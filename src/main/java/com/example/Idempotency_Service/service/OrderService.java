package com.example.Idempotency_Service.service;

import com.example.Idempotency_Service.dto.OrderRequest;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<?> submitOrder(String idempotencyKey, OrderRequest orderRequest);
}
