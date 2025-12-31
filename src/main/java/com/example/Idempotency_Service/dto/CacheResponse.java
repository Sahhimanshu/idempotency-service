package com.example.Idempotency_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1447147303667949911L;
    private int httpStatusCode;
    private Object responseBody;
}
