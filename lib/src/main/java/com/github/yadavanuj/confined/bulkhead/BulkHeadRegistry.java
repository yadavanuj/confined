package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Policy;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedUtils;

import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class BulkHeadRegistry extends Registry.BaseRegistry<BulkHead, BulkHeadConfig> {
    private final Semaphore semaphore;
    private final BulkHeadConfig config;

    public BulkHeadRegistry(BulkHeadConfig config) {
        this.config = config;
        this.semaphore = new Semaphore(config.getMaxConcurrentCalls());
    }

    @Override
    protected boolean onAcquire(String policyKey) throws ConfinedException {
        return ConfinedUtils.acquirePermitExceptionally(semaphore, config.getMaxWaitDurationInMillis());
    }

    @Override
    protected void onRelease(String policyKey) {
        semaphore.release();
    }

    @Override
    public Policy.PolicyType policyType() {
        return Policy.PolicyType.BulkHead;
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
