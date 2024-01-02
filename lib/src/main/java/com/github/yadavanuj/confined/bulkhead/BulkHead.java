package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Policy;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public interface BulkHead extends Policy {

    public class BulkHeadImpl implements BulkHead {
        private final Semaphore semaphore;
        private final int maxWaitDurationInMillis;

        public BulkHeadImpl(String policyKey,
                            int maxWaitDurationInMillis) {

            this.maxWaitDurationInMillis = maxWaitDurationInMillis;
            this.semaphore = new Semaphore(1);
        }

        @Override
        public PolicyType getPolicyType() {
            return PolicyType.BulkHead;
        }

        @Override
        public boolean acquire() {
            try {
                return semaphore.tryAcquire(maxWaitDurationInMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void release() {
            semaphore.release();
        }
    }
}
