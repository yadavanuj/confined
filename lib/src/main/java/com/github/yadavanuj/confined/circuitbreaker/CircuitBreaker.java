package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.Policy;

public interface CircuitBreaker extends Policy {
    static Policy create(io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker) {
        return new CircuitBreakerImpl(resilienceCircuitBreaker);
    }

    public class CircuitBreakerImpl implements CircuitBreaker {
        private final io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker;

        public CircuitBreakerImpl(io.github.resilience4j.circuitbreaker.CircuitBreaker resilienceCircuitBreaker) {
            this.resilienceCircuitBreaker = resilienceCircuitBreaker;
        }

        @Override
        public PolicyType getPolicyType() {
            return PolicyType.CircuitBreaker;
        }

        @Override
        public boolean acquire() {
            return resilienceCircuitBreaker.tryAcquirePermission();
        }

        @Override
        public void release() {
            resilienceCircuitBreaker.releasePermission();
        }
    }
}
