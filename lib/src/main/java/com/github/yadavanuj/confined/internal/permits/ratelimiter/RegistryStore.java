package com.github.yadavanuj.confined.internal.permits.ratelimiter;

import com.github.yadavanuj.confined.types.PermitType;
import com.github.yadavanuj.confined.types.RateLimiterConfig;
import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.internal.ConfinedUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// TODO: Algorithm must consider capacity (actually provider should)
@Getter
public class RegistryStore {
    private static final RegistryStore INSTANCE = new RegistryStore();
    private final Map<String, RateLimiterSlice> slices = new HashMap<>();
    private final Map<String, Semaphore> semaphores = new HashMap<>();
    private final Map<String, RateLimiterConfig> configurations = new HashMap<>();
    private final PermitType permitType = PermitType.RateLimiter;

    public static RegistryStore getInstance() {
        return INSTANCE;
    }

    private boolean shouldStartSlice(String key) {
        Objects.requireNonNull(configurations.get(key));
        final RateLimiterConfig config = configurations.get(key);

        Objects.requireNonNull(slices.get(key));
        final RateLimiterSlice limiterState = slices.get(key);

        final long timelineRelativeTimeDiff = System.currentTimeMillis() - limiterState.getStartTime();
        return limiterState.getAcquisitionTime() < limiterState.getStartTime() || timelineRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis();
    }

    private boolean shouldEndSlice(String key) {
        Objects.requireNonNull(slices.get(key));
        Objects.requireNonNull(configurations.get(key));

        final RateLimiterConfig config = configurations.get(key);
        final RateLimiterSlice limiterState = slices.get(key);

        final long acquisitionRelativeTimeDiff = limiterState.getAcquisitionTime() - limiterState.getStartTime();
        final long timelineRelativeTimeDiff = System.currentTimeMillis() - limiterState.getStartTime();

        return timelineRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis() || acquisitionRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis();
    }

    private boolean hasPermission(String key) {
        Objects.requireNonNull(slices.get(key));
        final RateLimiterSlice limiterState = slices.get(key);

        Objects.requireNonNull(configurations.get(key));
        final RateLimiterConfig config = configurations.get(key);

        // TODO: Overflow/Optimizations Here.
        if (limiterState.getActivePermissions() <= 0) {
            return false;
        }

        return config.getPermissionProvider().apply(key);
    }

    private int sleepTime(String key) {
        Objects.requireNonNull(configurations.get(key));
        final RateLimiterConfig config = configurations.get(key);
        return config.getProperties().getTimeoutInMillis() / config.getTimeoutSlicingFactor();
    }

    private void tick(String key) {
        // TODO: Capture Old Slice Info
        slices.get(key).sliceStateChangeOnTick();
    }

    private long stateChangeOnAcquisition(String key) {
        return slices.get(key).sliceStateChangeOnAcquired();
    }

    public boolean acquire(String key) throws ConfinedException {
        return acquire(key, AcquisitionState.builder().build());
    }
    public boolean acquire(String key, AcquisitionState acquisitionState) throws ConfinedException {
        try {
            if (semaphores.get(key).tryAcquire(1, sleepTime(key), TimeUnit.MILLISECONDS)) {
                long activePermissions = 0;
                if (shouldStartSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = stateChangeOnAcquisition(key);
                    }
                    return true;
                } else if (shouldEndSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = stateChangeOnAcquisition(key);
                    }
                    return true;
                } else if (hasPermission(key)) {
                    activePermissions = stateChangeOnAcquisition(key);
                    return true;
                } else if (acquisitionState.getCycle() < configurations.get(key).getTimeoutSlicingFactor() - 1) {
                    acquisitionState.setCycle(acquisitionState.getCycle() + 1);
                    semaphores.get(key).release();
                    ConfinedUtils.sleepUninterruptedly(sleepTime(key));
                    return acquire(key, acquisitionState);
                }
                return false;
            }
        } catch (InterruptedException e) {
            // TODO: Handle
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
        }
        return false;
    }

    public void release(String key) {
        semaphores.get(key).release();
    }
}
