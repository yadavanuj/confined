package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.bulkhead.BulkHead;
import com.github.yadavanuj.confined.bulkhead.BulkHeadConfig;
import com.github.yadavanuj.confined.bulkhead.BulkHeadRegistry;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreaker;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerConfig;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerRegistry;
import com.github.yadavanuj.confined.commons.ConfinedErrorCode;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.ratelimiter.RateLimiter;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterConfig;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public interface Confined {
    Registry<BulkHead, BulkHeadConfig> register(BulkHeadConfig config) throws ConfinedException;
    Registry<CircuitBreaker, CircuitBreakerConfig> register(CircuitBreakerConfig config) throws ConfinedException;
    Registry<RateLimiter, RateLimiterConfig> register(RateLimiterConfig config) throws ConfinedException;

    public class ConfinedImpl implements Confined {
        private Map<String, Registry<BulkHead, BulkHeadConfig>> bulkHeads = new HashMap<>();
        private Map<String, Registry<CircuitBreaker, CircuitBreakerConfig>> circuitBreakers = new HashMap<>();
        private Map<String, Registry<RateLimiter, RateLimiterConfig>> rateLimiters = new HashMap<>();
        private final Semaphore bulkheadSemaphore;
        private final Semaphore circuitBreakerSemaphore;
        private final Semaphore rateLimiterSemaphore;
        public ConfinedImpl() {
            this.bulkheadSemaphore = new Semaphore(1);
            this.circuitBreakerSemaphore = new Semaphore(1);
            this.rateLimiterSemaphore = new Semaphore(1);
        }

        @Override
        public Registry<BulkHead, BulkHeadConfig> register(BulkHeadConfig config) throws ConfinedException {
            try {
                if (bulkheadSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                    final Registry<BulkHead, BulkHeadConfig> registry = new BulkHeadRegistry(config);
                    this.bulkHeads.put(config.getKey(), registry);
                    return registry;
                }
            } catch (InterruptedException e) {
                throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
            }
            return null;
        }

        @Override
        public Registry<CircuitBreaker, CircuitBreakerConfig> register(CircuitBreakerConfig config) throws ConfinedException {
            try {
                if (circuitBreakerSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                    final Registry<CircuitBreaker, CircuitBreakerConfig> registry = new CircuitBreakerRegistry(config);
                    this.circuitBreakers.put(config.getOperationName(), registry);
                    return registry;
                }
            } catch (InterruptedException e) {
                throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
            }
            return null;
        }

        @Override
        public Registry<RateLimiter, RateLimiterConfig> register(RateLimiterConfig config) throws ConfinedException {
            try {
                if (rateLimiterSemaphore.tryAcquire(1, 1000, TimeUnit.MILLISECONDS)) {
                    final Registry<RateLimiter, RateLimiterConfig> registry = new RateLimiterRegistry(config);
                    this.rateLimiters.put(config.getOperationName(), registry);
                    return registry;
                }
            } catch (InterruptedException e) {
                throw new ConfinedException(ConfinedErrorCode.FailedToInstantiateRegistry);
            }
            return null;
        }
    }
}
