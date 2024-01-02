package com.github.yadavanuj.confined;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Registry <P, C> {
    Policy.PolicyType policyType();
    boolean acquire(String key);
    void release(String key);
    <R> Supplier<R> decorate(String policyKey, Supplier<R> supplier);
    <T, R> Function<T, R> decorate(String policyKey, Function<T, R> func);

    public abstract class BaseRegistry<T, C> implements Registry<T, C> {
//        protected Map<String, T> policies = new HashMap<>();
//        protected Map<String, C> configurations = new HashMap<>();

        protected abstract boolean onAcquire(String policyKey);
        protected abstract void onRelease(String policyKey);

        protected String getPolicyKey(String key) {
            Objects.requireNonNull(key);

//            final String[] parts = key.split(":");
//            if (parts.length < 2 || parts[1].isEmpty()) {
//                throw new RuntimeException("PolicyKey is required.");
//            }

            return key;
        }

        public boolean acquire(String key) {
            String policyKey = this.getPolicyKey(key);
            return this.onAcquire(policyKey);
        }

        public void release(String key) {
            String policyKey = this.getPolicyKey(key);
            this.onRelease(policyKey);
        }
    }
}
