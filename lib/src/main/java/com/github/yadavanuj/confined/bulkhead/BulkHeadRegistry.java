package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class BulkHeadRegistry extends Registry.BaseRegistry<BulkHead, BulkHeadConfig> {
    private final RegistryStore store;

    BulkHeadRegistry(BulkHeadConfig config, RegistryStore store) {
        final String policyKey = this.getPolicyKey(config.getOperationName());

        // Store
        this.store = store;

        // Initialize
        final BulkHead bulkhead = new BulkHead.BulkHeadImpl(policyKey, config.getMaxWaitDurationInMillis());
        store.getPolicies().put(policyKey, bulkhead);
        store.getConfigurations().put(policyKey, config);
    }

    public BulkHeadRegistry(BulkHeadConfig config) {
        this(config, RegistryStore.getInstance());
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
