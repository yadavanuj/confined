package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.PermissionAuthority;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public interface CircuitPermissionAuthority extends PermissionAuthority {
    boolean isPermitted(String key) throws PermissionAuthorityException;

    CircuitBreaker createOrGet(CircuitBreaker.CircuitBreakerConfig config);

    public class Implementation implements CircuitPermissionAuthority {
        private final Map<String, io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry> registries = new HashMap<>();
        private final Map<String, CircuitBreaker> circuitBreakerStore = new HashMap<>();

        @Override
        public boolean isPermitted(String key) throws PermissionAuthorityException {
            return false;
        }

        @Override
        public PermissionAuthorityType getPermissionAuthorityType() {
            return PermissionAuthorityType.CIRCUIT_BREAKER;
        }

        @Override
        public CircuitBreaker createOrGet(CircuitBreaker.CircuitBreakerConfig config) {
            // Create Resilience Circuit Breaker Config
            final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig resilienceCircuitBreakerConfig = CircuitBreakerConfig.custom()
                    .failureRateThreshold(config.getFailureRateThresholdPercentage())
                    .maxWaitDurationInHalfOpenState(Duration.ofMillis(config.getMaxWaitInHalfOpenStateInMillis()))
                    .waitDurationInOpenState(Duration.ofMillis(config.getWaitInOpenStateInMillis()))
                    .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
                    .slidingWindowSize(config.getSlidingWindowSize())
                    .slidingWindowType(config.getSlidingWindowType() == CircuitBreaker.SlidingWindowType.COUNT_BASED ? CircuitBreakerConfig.SlidingWindowType.COUNT_BASED : CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                    .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
                    .slowCallRateThreshold(config.getSlowCallRateThresholdPercentage())
                    .slowCallDurationThreshold(Duration.ofMillis(config.getSlowCallThresholdInMillis()))
                    .maxWaitDurationInHalfOpenState(Duration.ofMillis(config.getMaxWaitInHalfOpenStateInMillis()))
                    .waitDurationInOpenState(Duration.ofMillis(config.getWaitInOpenStateInMillis()))
                    .recordExceptions(config.getRecordExceptions())
                    .ignoreExceptions(config.getIgnoreExceptions())
                    .build();

            // Create Resilience Circuit Breaker Registry For Given Key.
            io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry resilienceCircuitBreakerRegistry
                    = io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(resilienceCircuitBreakerConfig);

            String registryKey = PermissionAuthority.getOperation(config::getOperationName);
            registries.put(registryKey, resilienceCircuitBreakerRegistry);
            io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker = resilienceCircuitBreakerRegistry.circuitBreaker(registryKey);

            CircuitBreaker circuitBreaker = new CircuitBreaker.Implementation(resilienceCircuitBreaker);
            circuitBreakerStore.put(registryKey, circuitBreaker);
            return circuitBreaker;
        }
    }
}
