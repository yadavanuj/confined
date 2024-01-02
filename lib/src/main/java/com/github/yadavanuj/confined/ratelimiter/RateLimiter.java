package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.Policy;

import java.util.Objects;

/**
 * An instance of RateLimiter which uses {@link RateLimiterKPermissionAuthority} to
 * acquire permission. It merely carries reference to the underlying rate-limiter 
 * using {@link #getPolicyKey()}.
 */
public interface RateLimiter extends Policy {
    String getPolicyKey();

    class RateLimiterImpl implements RateLimiter {
        private final String policyKey;
        private final RegistryStore registryStore;

        public RateLimiterImpl(String policyKey, RegistryStore registryStore) {
            this.policyKey = policyKey;
            this.registryStore = registryStore;
        }

        @Override
        public String getPolicyKey() {
            return policyKey;
        }

        @Override
        public PolicyType getPolicyType() {
            return PolicyType.RateLimiter;
        }

        @Override
        public boolean acquire() {
            final Policy policy = registryStore.getPolicies().get(policyKey);
            Objects.requireNonNull(policy, "Policy not found exception");
            return policy.acquire();
        }

        @Override
        public void release() {
            final Policy policy = registryStore.getPolicies().get(policyKey);
            Objects.requireNonNull(policy, "Policy not found exception");
            policy.release();
        }
    }
}
