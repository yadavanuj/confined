package com.github.yadavanuj.confined.internal.permits.ratelimiter;

import com.github.yadavanuj.confined.types.PermitType;
import com.github.yadavanuj.confined.types.RateLimiterConfig;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.types.ConfinedException;

import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class RateLimiterRegistry extends Registry.BaseRegistry<RateLimiterConfig> {
    private final RegistryStore store;

    public RateLimiterRegistry(RateLimiterConfig config) {
        this(config, RegistryStore.getInstance());
    }

    RateLimiterRegistry(RateLimiterConfig config, RegistryStore store) {
        this.store = store;
        this.initialize(config);
    }

    @Override
    protected boolean onAcquire(String key) throws ConfinedException {
        return store.acquire(key);
    }

    @Override
    protected void onRelease(String key) {
        store.release(key);
    }

    private void initialize(RateLimiterConfig config) {
        final String permitKey = this.getPermitKey(config.getOperationName());

        // TODO: Permission creation should be more dynamic.
        // This will let execution to be little dumb.
        final RateLimiterSlice slice = RateLimiterSlice.builder()
                .key(permitKey)
                .config(config)
                .activePermissions(config.getProperties().getLimitForPeriod())
                .build();

        // Register
        store.getConfigurations().put(permitKey, config);
        store.getSemaphores().put(permitKey, new Semaphore(config.getProperties().getLimitForPeriod()));
        store.getSlices().put(permitKey, slice);
    }

    @Override
    public PermitType permitType() {
        return store.getPermitType();
    }

    @Override
    public <T, R> Function<T, R> decorate(String key, Function<T, R> func) {
        return null;
    }
}
