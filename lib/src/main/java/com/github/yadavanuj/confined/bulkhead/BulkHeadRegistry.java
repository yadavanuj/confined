package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.PermitType;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedUtils;

import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class BulkHeadRegistry extends Registry.BaseRegistry<BulkHeadConfig> {
    private final Semaphore semaphore;
    private final BulkHeadConfig config;

    public BulkHeadRegistry(BulkHeadConfig config) {
        this.config = config;
        this.semaphore = new Semaphore(config.getMaxConcurrentCalls());
    }

    @Override
    protected boolean onAcquire(String key) throws ConfinedException {
        return ConfinedUtils.acquirePermitExceptionally(semaphore, config.getMaxWaitDurationInMillis());
    }

    @Override
    protected void onRelease(String key) {
        semaphore.release();
    }

    @Override
    public PermitType permitType() {
        return PermitType.BulkHead;
    }

    public <T, R> Function<T, R> decorate(String key, Function<T, R> func) {
        try {
            if (this.acquire(key)) {
                final BaseRegistry<BulkHeadConfig> that = this;
                return t -> {
                    R result = func.apply(t);
                    that.release(key);
                    return result;
                };
            }
        } catch (ConfinedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("");
    }
}
