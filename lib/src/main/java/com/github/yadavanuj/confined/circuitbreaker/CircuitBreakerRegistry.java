package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedErrorCode;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedSupplier;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class CircuitBreakerRegistry extends Registry.BaseRegistry<CircuitBreaker, CircuitBreakerConfig> {
    private final RegistryStore store;

    public CircuitBreakerRegistry(CircuitBreakerConfig config) {
        this(config, RegistryStore.getInstance());
    }

    CircuitBreakerRegistry(CircuitBreakerConfig config, RegistryStore store) {
        this.store = store;
        this.initialize(config);
    }

    @Override
    protected boolean onAcquire(String policyKey) throws ConfinedException {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(policyKey);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.PolicyNotFound.getValue());
        if (!circuitBreakerRegistry.circuitBreaker(policyKey).tryAcquirePermission()) {
            throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
        }
        return true;
    }

    @Override
    protected void onRelease(String policyKey) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(policyKey);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.PolicyNotFound.getValue());
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
                .recordExceptions(config.getRecordExceptions())
                .ignoreExceptions(config.getIgnoreExceptions())
                .build();

        // Create Resilience Circuit Breaker Registry For Given Key.
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry resilienceCircuitBreakerRegistry
                = io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(resilienceCircuitBreakerConfig);


        store.getRegistries().put(policyKey, resilienceCircuitBreakerRegistry);
        io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker = resilienceCircuitBreakerRegistry.circuitBreaker(policyKey);
    }

    private io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType getSlidingWindowType(CircuitBreakerConfig config) {
        return config.getSlidingWindowType() == SlidingWindowType.COUNT_BASED ? io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED : io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
    }

    @Override
    public Policy.PolicyType policyType() {
        return store.getPolicyType();
    }

    public <R> ConfinedSupplier<R> decorate(String policyKey, Supplier<R> supplier) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(policyKey);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.PolicyNotFound.getValue());
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(policyKey);
        Supplier<R> decoratedSupplier = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(circuitBreaker, supplier);;
        return new ConfinedSupplier<R>() {
            @Override
            public R get() throws ConfinedException {
                System.out.println(circuitBreaker.getState());
                R result;
                try {
                    result = decoratedSupplier.get();
                } catch (Exception e) {
                    throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation, e);
                }
                return result;
            }
        };
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
