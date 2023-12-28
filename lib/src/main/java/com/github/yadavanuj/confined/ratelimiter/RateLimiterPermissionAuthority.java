package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.PermissionAuthority;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public interface RateLimiterPermissionAuthority extends PermissionAuthority {
    boolean isPermitted(String key) throws PermissionAuthorityException;

    RateLimiter createOrGet(RateLimiterConfig config);

    static RateLimiterPermissionAuthority create() {
        return new Implementation();
    }

    @Getter
    @Setter
    @Builder
    public class AcquisitionState {
        @Builder.Default
        private int cycle = 0;
        @Builder.Default
        private long startTime = System.currentTimeMillis();
    }

    public class Implementation implements RateLimiterPermissionAuthority {
        private final RegistryContext context;
        public Implementation() {
            this.context = new RegistryContext();
        }
        @Override
        public RateLimiter createOrGet(RateLimiterConfig config) {
            final String key = config.getProperties().getKey();
            RateLimiter maybeLimiter = context.getLimiterStore().get(key);
            if (!Objects.isNull(maybeLimiter)) {
                return maybeLimiter;
            }

            // Initialize New
            final Semaphore semaphore = new Semaphore(1);
            maybeLimiter = new RateLimiter.Implementation(key, new WeakReference<>(this));

            // TODO: Permission creation should be more dynamic.
            // This will let execution to be little dumb.
            RateLimiterSlice slice = RateLimiterSlice.builder()
                    .key(key)
                    .config(config)
                    .activePermissions(config.getProperties().getLimitForPeriod())
                    .build();

            // Register
            context.getConfigStore().put(key, config);
            context.getSemaphoreStore().put(key, semaphore);
            context.getLimiterStore().put(key, maybeLimiter);
            context.getSliceStore().put(key, slice);

            return maybeLimiter;
        }

        @Override
        public boolean isPermitted(String key) throws PermissionAuthorityException {
            // TODO: Build / Verify state
            return context.tryAcquire(key, AcquisitionState.builder().build());
        }

        @Override
        public PermissionAuthorityType getPermissionAuthorityType() {
            return PermissionAuthorityType.RATE_LIMITER;
        }
    }
}
