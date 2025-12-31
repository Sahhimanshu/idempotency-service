package com.example.Idempotency_Service.controller;

import com.example.Idempotency_Service.dto.ErrorResponse;
import com.example.Idempotency_Service.dto.OrderRequest;
import com.example.Idempotency_Service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class OrderController {

    @Autowired
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            ErrorResponse error = new ErrorResponse(
                    "IDEMPOTENCY_KEY_MISSING",
                    "Idempotency-Key header is required"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 2️⃣ Delegate to service
        return orderService.submitOrder(idempotencyKey, orderRequest);
    }
}
