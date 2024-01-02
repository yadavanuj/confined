package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterConfig implements Serializable {
    @Builder.Default
    private Policy.PolicyType policyType = Policy.PolicyType.RateLimiter;
    @Builder.Default
    private String operationName = UUID.randomUUID().toString();
    private RateLimiterProperties properties;
    @Builder.Default
    private int timeoutSlicingFactor = 2;
    private Function<String, Boolean> permissionProvider;
}
