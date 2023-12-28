package com.github.yadavanuj.confined.ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.function.Function;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterConfig implements Serializable {
    private RateLimiterProperties properties;
    @Builder.Default
    private int timeoutSlicingFactor = 2;
    private Function<String, Boolean> permissionProvider;
}
