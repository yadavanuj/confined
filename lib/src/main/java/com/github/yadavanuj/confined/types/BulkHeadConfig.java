package com.github.yadavanuj.confined.types;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
public class BulkHeadConfig extends ConfinedConfig {
    @Builder.Default
    private String key = UUID.randomUUID().toString();
    @Builder.Default
    private String operationName = UUID.randomUUID().toString();
    @Builder.Default
    private int maxConcurrentCalls = 25;
    @Builder.Default
    private int maxWaitDurationInMillis = 500;
}
