package com.github.yadavanuj.confined.internal.permits.bulkhead;

import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.internal.ConfinedUtils;
import com.github.yadavanuj.confined.types.BulkHeadConfig;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;

import java.util.concurrent.Semaphore;

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

    protected Semaphore getSemaphore(){return this.semaphore;}
    public String getName(){
        return "BH";
    }

}
