package com.github.yadavanuj.confined.types;

import com.github.yadavanuj.confined.internal.permits.ratelimiter.RateLimiterProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.UUID;
import java.util.function.Function;

@Getter
@SuperBuilder(toBuilder = true)
public class RateLimiterConfig extends ConfinedConfig {
    @Builder.Default
    private String operationName = UUID.randomUUID().toString();
    @NonNull
    private RateLimiterProperties properties;
    @Builder.Default
    private int timeoutSlicingFactor = 2;
    @NonNull
    private Function<String, Boolean> permissionProvider;
}
