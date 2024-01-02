package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Supplier;

public class RateLimiterRegistry extends Registry.BaseRegistry<RateLimiter, RateLimiterConfig> {
    private final RegistryStore store;

    public RateLimiterRegistry(RateLimiterConfig config) {
        this(config, RegistryStore.getInstance());
    }

    RateLimiterRegistry(RateLimiterConfig config, RegistryStore store) {
        this.store = store;
        this.initialize(config);
    }

    @Override
    protected boolean onAcquire(String policyKey) {
        final Policy policy = store.getPolicies().get(policyKey);
        Objects.requireNonNull(policy, "Policy not found exception");
        return policy.acquire();
    }

    @Override
    protected void onRelease(String policyKey) {
        final Policy policy = store.getPolicies().get(policyKey);
        Objects.requireNonNull(policy, "Policy not found exception");
        policy.release();
    }

    private void initialize(RateLimiterConfig config) {
        final String policyKey = this.getPolicyKey(config.getOperationName());

        // Initialize New
        final Semaphore semaphore = new Semaphore(1);
        // TODO:
        final RateLimiter maybeLimiter = new RateLimiter.RateLimiterImpl(policyKey, RegistryStore.getInstance());

        // TODO: Permission creation should be more dynamic.
        // This will let execution to be little dumb.
        final RateLimiterSlice slice = RateLimiterSlice.builder()
                .key(policyKey)
                .config(config)
                .activePermissions(config.getProperties().getLimitForPeriod())
                .build();

        // Register
        store.getConfigurations().put(policyKey, config);
        store.getSemaphores().put(policyKey, semaphore);
        store.getPolicies().put(policyKey, maybeLimiter);
        store.getSlices().put(policyKey, slice);
    }

    @Override
    public Policy.PolicyType policyType() {
        return store.getPolicyType();
    }

    public <R> Supplier<R> decorate(String policyKey, Supplier<R> supplier) {
        if (this.acquire(policyKey)) {
            return new Supplier<R>() {
                @Override
                public R get() {
                    R result = supplier.get();
                    store.getPolicies().get(policyKey).release();
                    return result;
                }
            };
        }
        throw new RuntimeException("");
    }

    public <T, R> Function<T, R> decorate(String policyKey, Function<T, R> func) {
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
        throw new RuntimeException("");
    }
}
