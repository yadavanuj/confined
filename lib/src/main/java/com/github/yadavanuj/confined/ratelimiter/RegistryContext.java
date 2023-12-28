package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.PermissionAuthority;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter
public class RegistryContext {
    public final Map<String, RateLimiterSlice> sliceStore = new HashMap<>();
    public final Map<String, RateLimiter> limiterStore = new HashMap<>();
    public final Map<String, Semaphore> semaphoreStore = new HashMap<>();
    public final Map<String, RateLimiterConfig> configStore = new HashMap<>();

    public final boolean isDebug = true;

    public boolean shouldStartSlice(String key) {
        Objects.requireNonNull(configStore.get(key));
        final RateLimiterConfig config = configStore.get(key);

        Objects.requireNonNull(sliceStore.get(key));
        final RateLimiterSlice limiterState = sliceStore.get(key);

        final long timelineRelativeTimeDiff = System.currentTimeMillis() - limiterState.getStartTime();
        return limiterState.getAcquisitionTime() < limiterState.getStartTime() || timelineRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis();
    }

    public boolean shouldEndSlice(String key) {
        Objects.requireNonNull(sliceStore.get(key));
        Objects.requireNonNull(configStore.get(key));

        final RateLimiterConfig config = configStore.get(key);
        final RateLimiterSlice limiterState = sliceStore.get(key);

        final long acquisitionRelativeTimeDiff = limiterState.getAcquisitionTime() - limiterState.getStartTime();
        final long timelineRelativeTimeDiff = System.currentTimeMillis() - limiterState.getStartTime();

        return timelineRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis() || acquisitionRelativeTimeDiff >= config.getProperties().getLimitRefreshPeriodInMillis();
    }

    public boolean hasPermission(String key) {
        Objects.requireNonNull(sliceStore.get(key));
        final RateLimiterSlice limiterState = sliceStore.get(key);

        Objects.requireNonNull(configStore.get(key));
        final RateLimiterConfig config = configStore.get(key);

        // TODO: Overflow/Optimizations Here.
        if (limiterState.getActivePermissions() <= 0) {
            return false;
        }

        return config.getPermissionProvider().apply(key);
    }

    public int sleepTime(String key) {
        Objects.requireNonNull(configStore.get(key));
        final RateLimiterConfig config = configStore.get(key);
        return config.getProperties().getTimeoutInMillis() / config.getTimeoutSlicingFactor();
    }

    public void tick(String key) {
        // TODO: Capture Old Slice Info
        sliceStore.get(key).tick();
    }

    public long onAcquisition(String key) {
        long activePermissions = sliceStore.get(key).acquired();
        semaphoreStore.get(key).release();
        return activePermissions;
    }

    public boolean tryAcquire(String key, RateLimiterPermissionAuthority.AcquisitionState acquisitionState) throws RateLimiterException {
        try {
            if (semaphoreStore.get(key).tryAcquire(1, sleepTime(key), TimeUnit.MILLISECONDS)) {
                Debug.log(isDebug, Debug.SemaphoreAcquired, acquisitionState.getCycle());
                long activePermissions = 0;
                if (shouldStartSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = onAcquisition(key);
                    }
                    Debug.log(isDebug, Debug.StartingSlice, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (shouldEndSlice(key)) {
                    tick(key);
                    if (hasPermission(key)) {
                        activePermissions = onAcquisition(key);
                    }
                    Debug.log(isDebug, Debug.EndingSlice, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (hasPermission(key)) {
                    activePermissions = onAcquisition(key);
                    Debug.log(isDebug, Debug.AcquiringWithinWindow, acquisitionState.getCycle(), activePermissions);
                    return true;
                } else if (acquisitionState.getCycle() < configStore.get(key).getTimeoutSlicingFactor() - 1) {
                    try {
                        acquisitionState.setCycle(acquisitionState.getCycle() + 1);
                        Debug.log(isDebug, Debug.GoingToSleep, acquisitionState.getCycle());
                        semaphoreStore.get(key).release();
                        Thread.sleep(sleepTime(key));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // TODO: Handle Better
                        throw new RateLimiterException(PermissionAuthority.PermissionAuthorityType.RATE_LIMITER);
                    } finally {
                        semaphoreStore.get(key).release();
                    }
                    return tryAcquire(key, acquisitionState);
                }

                semaphoreStore.get(key).release();
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: Could not acquire semaphore
            throw new RateLimiterException(PermissionAuthority.PermissionAuthorityType.RATE_LIMITER);
        }
        return false;
    }
}
