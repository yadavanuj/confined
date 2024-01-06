package com.github.yadavanuj.confined.internal.permits.circuitbreaker;

import com.github.yadavanuj.confined.ConfinedFunction;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.types.CircuitBreakerConfig;
import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class CircuitBreakerRegistry extends Registry.BaseRegistry<CircuitBreakerConfig> {
    private final RegistryStore store;

    public CircuitBreakerRegistry(CircuitBreakerConfig config) {
        this(config, RegistryStore.getInstance());
    }

    CircuitBreakerRegistry(CircuitBreakerConfig config, RegistryStore store) {
        this.store = store;
        this.initialize(config);
    }

    @Override
    protected boolean onAcquire(String key) throws ConfinedException {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(key);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.RegistryNotFound.getValue());
        if (!circuitBreakerRegistry.circuitBreaker(key).tryAcquirePermission()) {
            throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
        }
        return true;
    }

    @Override
    protected void onRelease(String key) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(key);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.RegistryNotFound.getValue());
        circuitBreakerRegistry.circuitBreaker(key).releasePermission();
    }

    private void initialize(CircuitBreakerConfig config) {
        final String permitKey = this.getPermitKey(config.getOperationName());

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


        store.getRegistries().put(permitKey, resilienceCircuitBreakerRegistry);
        io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker = resilienceCircuitBreakerRegistry.circuitBreaker(permitKey);
    }

    private io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType getSlidingWindowType(CircuitBreakerConfig config) {
        return config.getSlidingWindowType() == SlidingWindowType.COUNT_BASED ? io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED : io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
    }

    @Override
    public PermitType permitType() {
        return store.getPermitType();
    }

    public <R> ConfinedSupplier<R> decorate(String key, Supplier<R> supplier) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(key);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.RegistryNotFound.getValue());
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(key);
        Supplier<R> decoratedSupplier = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        ;
        return new ConfinedSupplier<R>() {
            @Override
            public R get() throws ConfinedException {
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

    @Override
    public <I, O> ConfinedFunction<I, O> decorate(String key, Function<I, O> func) {
        final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry = store.getRegistries().get(key);
        Objects.requireNonNull(circuitBreakerRegistry, ConfinedErrorCode.RegistryNotFound.getValue());
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(key);
        Function<I, O> decoratedFunc = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return new ConfinedFunction<I, O>() {
            @Override
            public O apply(I input) throws ConfinedException {
                O output;
                try {
                    output = decoratedFunc.apply(input);
                } catch (Exception e) {
                    throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation, e);
                }
                return output;
            }
        };
    }
}
