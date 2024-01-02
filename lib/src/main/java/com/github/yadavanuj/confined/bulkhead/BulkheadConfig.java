package com.github.yadavanuj.confined.bulkhead;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkHeadConfig {
    @Builder.Default
    private String key = UUID.randomUUID().toString();
    @Builder.Default
    private String operationName = UUID.randomUUID().toString();
    @Builder.Default
    private int maxConcurrentCalls = 25;
    @Builder.Default
    private int maxWaitDurationInMillis = 500;
}
