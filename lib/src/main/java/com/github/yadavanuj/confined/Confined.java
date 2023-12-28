package com.github.yadavanuj.confined;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.yadavanuj.confined.ratelimiter.RateLimiterPermissionAuthority;
import com.github.yadavanuj.confined.bulkhead.BulkheadPermissionAuthority;
import com.github.yadavanuj.confined.circuitbreaker.CircuitPermissionAuthority;

public interface Confined {
    <R> R exec(Operation<R> operation) throws RuntimeException;
    <R>CompletableFuture<R> execAsync(Operation<R> operation) throws RuntimeException;

    public class ConfinedException extends RuntimeException {
        public ConfinedException(String message) {
            super(message);
        }
    }

    @Builder
    @Getter
    public class Operation<R> {
        private String key;
        private Supplier<R> supplier;
    }

    public class ConfinedConfig {
        private final List<PermissionAuthority> authorities = new ArrayList<>();
        private boolean rateLimiterConfigured = false;
        private boolean circuitBreakerConfigured = false;
        private boolean bulkHeadConfigured = false;
        public void addRateLimiterPermissionAuthority(RateLimiterPermissionAuthority authority) {
            if (!rateLimiterConfigured) {
                this.authorities.add(authority);
                rateLimiterConfigured = true;
            } else {
                throw new ConfinedException("Rate limiter has already been configured.");
            }
        }

        public void addCircuitBreakerPermissionAuthority(CircuitPermissionAuthority authority) {
            if (!circuitBreakerConfigured) {
                this.authorities.add(authority);
                circuitBreakerConfigured = true;
            } else {
                throw new ConfinedException("Circuit breaker has already been configured.");
            }
        }

        public void addBulkHeadPermissionAuthority(BulkheadPermissionAuthority authority) {
            if (!bulkHeadConfigured) {
                this.authorities.add(authority);
                bulkHeadConfigured = true;
            } else {
                throw new ConfinedException("Bulkhead has already been configured.");
            }
        }
    }

    /**
     * TODO: Divide into smaller slices using refresh-period / limit-for-period as an indicative slice count.
     */
    public class Implementation implements Confined {
        private final ConfinedConfig config;

        public Implementation(ConfinedConfig config) {
            this.config = config;
        }

        @Override
        public <R> R exec(Operation<R> operation) throws RuntimeException {
            AtomicBoolean canExecute = new AtomicBoolean(true);
            config.authorities.forEach(approvalAuthority -> {
                if (!approvalAuthority.isPermitted(operation.getKey())) {
                    canExecute.set(false);
                }
            });

            if (canExecute.get()) {
                return operation.getSupplier().get();
            }
            return null;
        }

        @Override
        public <R> CompletableFuture<R> execAsync(Operation<R> operation) throws RuntimeException {
            AtomicBoolean canExecute = new AtomicBoolean(true);
            config.authorities.forEach(approvalAuthority -> {
                if (!approvalAuthority.isPermitted(operation.getKey())) {
                    canExecute.set(false);
                }
            });

            if (canExecute.get()) {
                return CompletableFuture.supplyAsync(operation.getSupplier());
            }
            return CompletableFuture.failedFuture(new RuntimeException());
        }
    }
}
