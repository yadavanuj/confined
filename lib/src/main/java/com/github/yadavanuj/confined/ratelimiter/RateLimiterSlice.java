package com.github.yadavanuj.confined.ratelimiter;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
public class RateLimiterSlice {
    @NonNull
    private String key;
    @Getter
    @Builder.Default
    private long startTime = System.currentTimeMillis();
    @Getter
    @Setter
    @Builder.Default
    private long acquisitionTime  = -1L;
    private long activePermissions;
    @Getter
    private final RateLimiterConfig config;

    public long acquired() {
        acquisitionTime = System.currentTimeMillis();
        return --activePermissions;
    }

    public void tick() {
        // TODO: Capture Old Slice Info
        startTime = System.currentTimeMillis();
        // TODO: Make permissions more flexible.
        activePermissions = config.getProperties().getLimitForPeriod();
    }
}
