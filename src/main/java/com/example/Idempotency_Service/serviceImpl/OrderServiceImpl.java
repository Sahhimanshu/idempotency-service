package com.example.Idempotency_Service.serviceImpl;

import com.example.Idempotency_Service.dto.CacheResponse;
import com.example.Idempotency_Service.dto.ErrorResponse;
import com.example.Idempotency_Service.dto.OrderRequest;
import com.example.Idempotency_Service.dto.OrderResponse;
import com.example.Idempotency_Service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final RedisTemplate<String,Object> redisTemplate;

    @Override
    public ResponseEntity<?> submitOrder(String idempotencyKey, OrderRequest orderRequest) {
        String cacheKey = getKeyForResponse(idempotencyKey);
        String lockKey = "idempotency:lock:" + idempotencyKey;
        CacheResponse cacheResponse = getCacheResponse(cacheKey);
        if (cacheResponse != null) {
            return ResponseEntity.status(cacheResponse.getHttpStatusCode()).body(cacheResponse.getResponseBody());
        }

        boolean lockAcquired = acquireLock(lockKey,Duration.ofSeconds(30));
        if (!lockAcquired) {
            return conflict();
        }
        try {
            OrderResponse response = processOrder(orderRequest);
            CacheResponse newCacheResponse = new CacheResponse(
                    HttpStatus.CREATED.value(),
                    response
            );
            redisTemplate.opsForValue().set(cacheKey, newCacheResponse, Duration.ofMinutes(10));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong"));
        }finally {
            releaseLock(lockKey);
        }
    }

    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    private boolean acquireLock(String lockKey, Duration lockTtl) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", lockTtl);

        return Boolean.TRUE.equals(acquired);
    }

    private CacheResponse getCacheResponse(String idempotencyKey) {
        return (CacheResponse) redisTemplate.opsForValue().get(idempotencyKey);
    }

    private String getKeyForResponse(String idempotencyKey) {
        return "idempotency:response:" + idempotencyKey;
    }


    private ResponseEntity<ErrorResponse> conflict() {
        ErrorResponse errorResponse = new ErrorResponse(
                "REQUEST_IN_PROGRESS",
                "A request with the same Idempotency-Key is already being processed. Please retry later."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    private OrderResponse processOrder(OrderRequest request) {
        return new OrderResponse(
                UUID.randomUUID().toString(),
                "CREATED",
                "Order created successfully"
        );
    }
}
