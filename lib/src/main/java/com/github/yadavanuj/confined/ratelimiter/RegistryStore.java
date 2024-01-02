package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.Policy;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter
public class RegistryStore {
    private static final RegistryStore INSTANCE = new RegistryStore();
    private final Map<String, RateLimiterSlice> slices = new HashMap<>();
    private final Map<String, Semaphore> semaphores = new HashMap<>();
    private final Map<String, RateLimiter> policies = new HashMap<>();
    private final Map<String, RateLimiterConfig> configurations = new HashMap<>();
    private final Policy.PolicyType policyType = Policy.PolicyType.RateLimiter;

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
        long activePermissions = slices.get(key).sliceStateChangeOnAcquired();
        semaphores.get(key).release();
        return activePermissions;
    }

    public boolean acquire(String key, AcquisitionState acquisitionState) throws RateLimiterException {
        try {
            if (semaphores.get(key).tryAcquire(1, sleepTime(key), TimeUnit.MILLISECONDS)) {
//                    Debug.log(isDebug, Debug.SemaphoreAcquired, acquisitionState.getCycle());
                long activePermissions = 0;
                if (shouldStartSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = stateChangeOnAcquisition(key);
                    }
//                        Debug.log(isDebug, Debug.StartingSlice, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (shouldEndSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = stateChangeOnAcquisition(key);
                    }
//                        Debug.log(isDebug, Debug.EndingSlice, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (hasPermission(key)) {
                    activePermissions = stateChangeOnAcquisition(key);
//                        Debug.log(isDebug, Debug.AcquiringWithinWindow, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (acquisitionState.getCycle() < configurations.get(key).getTimeoutSlicingFactor() - 1) {
                    try {
                        acquisitionState.setCycle(acquisitionState.getCycle() + 1);
//                            Debug.log(isDebug, Debug.GoingToSleep, acquisitionState.getCycle());
                        semaphores.get(key).release();
                        Thread.sleep(sleepTime(key));
                    } catch (InterruptedException e) {
                        semaphores.get(key).release();
                        e.printStackTrace();
                        // TODO: Handle Better
                        throw new RateLimiterException(e);
                    }
                    return acquire(key, acquisitionState);
                }

//                semaphores.get(key).release();
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: Could not acquire semaphore
            throw new RateLimiterException(e);
        }
        return false;
    }

    public void release(String key) {
        semaphores.get(key).release();
    }
}
