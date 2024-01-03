package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedSupplier;
import com.github.yadavanuj.confined.commons.ConfinedUtils;

import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Supplier;

public class BulkHeadRegistry extends Registry.BaseRegistry<BulkHead, BulkHeadConfig> {
    private final Semaphore semaphore;
    private final BulkHeadConfig config;

    public BulkHeadRegistry(BulkHeadConfig config) {
        this.config = config;
        this.semaphore = new Semaphore(config.getMaxConcurrentCalls());
    }

    @Override
    protected boolean onAcquire(String policyKey) throws ConfinedException {
        return ConfinedUtils.acquirePermitsExceptionally(semaphore, config.getMaxWaitDurationInMillis());
    }

    @Override
    protected void onRelease(String policyKey) {
        semaphore.release();
    }

    @Override
    public Policy.PolicyType policyType() {
        return Policy.PolicyType.BulkHead;
    }

    public <R> ConfinedSupplier<R> decorate(String policyKey, Supplier<R> supplier) {
        return new ConfinedSupplier<R>() {
            @Override
            public R get() throws ConfinedException {
                if (BulkHeadRegistry.this.acquire(policyKey)) {
                    R result = supplier.get();
                    BulkHeadRegistry.this.release(policyKey);
                    return result;
                }
                return null;
            }
        };
    }

    public <T, R> Function<T, R> decorate(String policyKey, Function<T, R> func) {
        try {
            if (this.acquire(policyKey)) {
                final Registry<BulkHead, BulkHeadConfig> that = this;
                return t -> {
                    R result = func.apply(t);
                    that.release(policyKey);
                    return result;
                };
            }
        } catch (ConfinedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("");
    }
}
