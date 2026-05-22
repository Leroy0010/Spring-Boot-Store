package com.leroy.store.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class WebhookRequest {
    private Map<String, String> headers;
    private String payload;
}
