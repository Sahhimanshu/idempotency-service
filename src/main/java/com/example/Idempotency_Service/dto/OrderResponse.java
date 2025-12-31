package com.example.Idempotency_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -7668831882710107160L;

    private String orderId;
    private String status;
    private String message;

}
