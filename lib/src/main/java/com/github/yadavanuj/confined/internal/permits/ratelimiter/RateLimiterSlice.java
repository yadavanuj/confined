package com.github.yadavanuj.confined.internal.permits.ratelimiter;

import com.github.yadavanuj.confined.types.RateLimiterConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
public class RateLimiterSlice {
    @NonNull
    private String key;
    @Builder.Default
    private long startTime = System.currentTimeMillis();
    @Setter
    @Builder.Default
    private long acquisitionTime  = -1L;
    private long activePermissions;
    private final RateLimiterConfig config;

    public long sliceStateChangeOnAcquired() {
        acquisitionTime = System.currentTimeMillis();
        return --activePermissions;
    }

    public void sliceStateChangeOnTick() {
        // TODO: Capture Old Slice Info
        startTime = System.currentTimeMillis();
        // TODO: Make permissions more flexible.
        activePermissions = config.getProperties().getLimitForPeriod();
    }
}
