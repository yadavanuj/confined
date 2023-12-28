package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.PermissionAuthority;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public interface BulkheadPermissionAuthority extends PermissionAuthority {
    public class PermissionAuthority implements BulkheadPermissionAuthority {
        private final Map<String, BulkheadConfig> configStore = new HashMap<>();
        private final Map<String, Bulkhead> bulkheadStore = new HashMap<>();
        private final Map<String, Semaphore> semaphoreStore = new HashMap<>();

        public Bulkhead createOrGet(BulkheadConfig config) {
            final String key = config.getKey();
            if (Objects.nonNull(bulkheadStore.get(key))) {
                return bulkheadStore.get(key);
            }

            // Initialize
            final Semaphore semaphore = new Semaphore(config.getMaxConcurrentCalls(), true);
            final Bulkhead bulkhead = new Bulkhead.Core(config.getKey(), new WeakReference<>(this));
            this.configStore.put(key, config);
            this.semaphoreStore.put(key, semaphore);
            this.bulkheadStore.put(key, bulkhead);
            return new Bulkhead.Core(config.getKey(), new WeakReference<>(this));
        }

        protected boolean tryAcquire(String key) {
            final Semaphore semaphore = semaphoreStore.get(key);
            final BulkheadConfig bulkheadConfig = configStore.get(key);
            try {
                if (semaphore.tryAcquire(bulkheadConfig.getMaxWaitDurationInMillis(), TimeUnit.MILLISECONDS)) {
                    return true;
                }
            } catch (InterruptedException e) {
                // TODO: Handle Better Later
                throw new BulkheadException(e, PermissionAuthorityType.BULK_HEAD);
            }
            return false;
        }

        @Override
        public boolean isPermitted(String key) throws PermissionAuthorityException {
            return tryAcquire(key);
        }

        @Override
        public PermissionAuthorityType getPermissionAuthorityType() {
            return PermissionAuthorityType.BULK_HEAD;
        }
    }
}
