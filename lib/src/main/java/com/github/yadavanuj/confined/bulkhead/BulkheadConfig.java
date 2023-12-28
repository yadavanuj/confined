package com.github.yadavanuj.confined.bulkhead;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkheadConfig {
    @Builder.Default
    private String key = UUID.randomUUID().toString();
    @Builder.Default
    private int maxConcurrentCalls = 25;
    @Builder.Default
    private int maxWaitDurationInMillis = 500;
}
