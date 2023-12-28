package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.PermissionAuthority;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * An instance of RateLimiter which uses {@link RateLimiterPermissionAuthority} to acquire permission.
 * It merely carries reference to the underlying rate-limiter using {@link #getKey()}.
 */
public interface RateLimiter extends PermissionAuthority {
    String getKey();

    class Implementation implements RateLimiter {
        private final String key;
        private final WeakReference<RateLimiterPermissionAuthority> registryRef;

        public Implementation(String key, WeakReference<RateLimiterPermissionAuthority> registryRef) {
            this.key = key;
            this.registryRef = registryRef;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public boolean isPermitted(String key) throws PermissionAuthorityException {
            try {
                final RateLimiterPermissionAuthority maybeRegistryRef = registryRef.get();
                if (Objects.nonNull(maybeRegistryRef)) {
                    return maybeRegistryRef.isPermitted(key);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            // TODO: throw better
            return false;
        }

        @Override
        public PermissionAuthorityType getPermissionAuthorityType() {
            return PermissionAuthorityType.RATE_LIMITER;
        }
    }
}
