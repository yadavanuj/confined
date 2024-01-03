package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedSupplier;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class CircuitBreakerRegistry extends Registry.BaseRegistry<CircuitBreaker, CircuitBreakerConfig> {
    private final RegistryStore store;
    private final Semaphore semaphore;

    public CircuitBreakerRegistry(CircuitBreakerConfig config) {
        this(config, RegistryStore.getInstance(), new Semaphore(1));
    }

    CircuitBreakerRegistry(CircuitBreakerConfig config, RegistryStore store, Semaphore semaphore) {
        this.store = store;
        this.semaphore = semaphore;

        try {
            if (this.semaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                    this.initialize(config);
                    semaphore.release();
                }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CircuitBreakerException(e);
        }
    }

    @Override
    protected boolean onAcquire(String policyKey) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(policyKey);
        Objects.requireNonNull(circuitBreakerRegistry, "Policy not found exception");
        return circuitBreakerRegistry.circuitBreaker(policyKey).tryAcquirePermission();
    }

    @Override
    protected void onRelease(String policyKey) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(policyKey);
        Objects.requireNonNull(circuitBreakerRegistry, "Policy not found exception");
        circuitBreakerRegistry.circuitBreaker(policyKey).releasePermission();
    }

    private void initialize(CircuitBreakerConfig config) {
        final String policyKey = this.getPolicyKey(config.getOperationName());

        // Create Resilience Circuit Breaker Config
        final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig resilienceCircuitBreakerConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(config.getFailureRateThresholdPercentage())
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(config.getMaxWaitInHalfOpenStateInMillis()))
                .waitDurationInOpenState(Duration.ofMillis(config.getWaitInOpenStateInMillis()))
                .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
                .slidingWindowSize(config.getSlidingWindowSize())
                .slidingWindowType(getSlidingWindowType(config))
                .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
                .slowCallRateThreshold(config.getSlowCallRateThresholdPercentage())
                .slowCallDurationThreshold(Duration.ofMillis(config.getSlowCallThresholdInMillis()))
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(config.getMaxWaitInHalfOpenStateInMillis()))
                .waitDurationInOpenState(Duration.ofMillis(config.getWaitInOpenStateInMillis()))
                .recordExceptions(config.getRecordExceptions())
                .ignoreExceptions(config.getIgnoreExceptions())
                .build();

        // Create Resilience Circuit Breaker Registry For Given Key.
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry resilienceCircuitBreakerRegistry
                = io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(resilienceCircuitBreakerConfig);


        store.getRegistries().put(policyKey, resilienceCircuitBreakerRegistry);
        io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker = resilienceCircuitBreakerRegistry.circuitBreaker(policyKey);

        CircuitBreaker circuitBreaker = new CircuitBreaker.CircuitBreakerImpl(resilienceCircuitBreaker);
        store.getPolicies().put(policyKey, circuitBreaker);
    }

    private io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType getSlidingWindowType(CircuitBreakerConfig config) {
        return config.getSlidingWindowType() == SlidingWindowType.COUNT_BASED ? io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED : io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
    }

    @Override
    public Policy.PolicyType policyType() {
        return store.getPolicyType();
    }

    public <R> ConfinedSupplier<R> decorate(String policyKey, Supplier<R> supplier) {
        try {
            if (this.acquire(policyKey)) {
                return new ConfinedSupplier<R>() {
                    @Override
                    public R get() {
                        R result = supplier.get();
                        store.getPolicies().get(policyKey).release();
                        return result;
                    }
                };
            }
        } catch (ConfinedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("");
    }

    public <T, R> Function<T, R> decorate(String policyKey, Function<T, R> func) {
        try {
            if (this.acquire(policyKey)) {
                return new Function<T, R>() {
                    @Override
                    public R apply(T t) {
                        R result = func.apply(t);
                        store.getPolicies().get(policyKey).release();
                        return result;
                    }
                };
            }
        } catch (ConfinedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("");
    }
}
