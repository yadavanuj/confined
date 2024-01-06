package com.github.yadavanuj.confined.internal.permits.ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterProperties {
    private String key;
    private long capacity;
    @Builder.Default
    private int timeoutInMillis = 500;
    @Builder.Default
    private int limitForPeriod = 5;
    @Builder.Default
    private int limitRefreshPeriodInMillis = 100;
    @Builder.Default
    private RefillStrategy refillStrategy = RefillStrategy.LAZY;
}
