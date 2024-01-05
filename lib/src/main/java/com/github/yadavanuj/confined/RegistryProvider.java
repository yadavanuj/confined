package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.bulkhead.BulkHeadConfig;
import com.github.yadavanuj.confined.bulkhead.BulkHeadRegistry;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerConfig;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerRegistry;
import com.github.yadavanuj.confined.commons.ConfinedConfig;
import com.github.yadavanuj.confined.commons.ConfinedErrorCode;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterConfig;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RegistryProvider {
    private Map<String, Registry<BulkHeadConfig>> bulkHeads = new HashMap<>();
    private Map<String, Registry<CircuitBreakerConfig>> circuitBreakers = new HashMap<>();
    private Map<String, Registry<RateLimiterConfig>> rateLimiters = new HashMap<>();
    private final Semaphore bulkheadSemaphore;
    private final Semaphore circuitBreakerSemaphore;
    private final Semaphore rateLimiterSemaphore;

    public RegistryProvider() {
        this.bulkheadSemaphore = new Semaphore(1);
        this.circuitBreakerSemaphore = new Semaphore(1);
        this.rateLimiterSemaphore = new Semaphore(1);
    }

    private Registry<BulkHeadConfig> register(BulkHeadConfig config) throws ConfinedException {
        try {
            if (bulkheadSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                final Registry<BulkHeadConfig> registry = new BulkHeadRegistry(config);
                this.bulkHeads.put(config.getKey(), registry);
                return registry;
            }
        } catch (InterruptedException e) {
            throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
        }
        return null;
    }

    private Registry<CircuitBreakerConfig> register(CircuitBreakerConfig config) throws ConfinedException {
        try {
            if (circuitBreakerSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                final Registry<CircuitBreakerConfig> registry = new CircuitBreakerRegistry(config);
                this.circuitBreakers.put(config.getOperationName(), registry);
                return registry;
            }
        } catch (InterruptedException e) {
            throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
        }
        return null;
    }

    private Registry<RateLimiterConfig> register(RateLimiterConfig config) throws ConfinedException {
        try {
            if (rateLimiterSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                final Registry<RateLimiterConfig> registry = new RateLimiterRegistry(config);
                this.rateLimiters.put(config.getOperationName(), registry);
                return registry;
            }
        } catch (InterruptedException e) {
            throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
        }
        return null;
    }

    public Registry<? extends ConfinedConfig> create(ConfinedConfig config) throws ConfinedException {
        switch (config.getPermitType()) {
            case BulkHead -> {
                return this.register((BulkHeadConfig) config);
            }
            case CircuitBreaker -> {
                return this.register((CircuitBreakerConfig) config);
            }
            case RateLimiter -> {
                return this.register((RateLimiterConfig) config);
            }
            default -> throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
        }
    }
}
