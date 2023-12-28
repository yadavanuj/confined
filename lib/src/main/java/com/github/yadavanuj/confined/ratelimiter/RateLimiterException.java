package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.PermissionAuthority;

public class RateLimiterException extends PermissionAuthority.PermissionAuthorityException {
    public RateLimiterException(PermissionAuthority.PermissionAuthorityType type) {
        super(type);
    }

    public RateLimiterException(Throwable throwable, PermissionAuthority.PermissionAuthorityType type) {
        super(throwable, type);
    }

    public RateLimiterException(String message, Throwable throwable, PermissionAuthority.PermissionAuthorityType type) {
        super(message, throwable, type);
    }

    public RateLimiterException(String message, PermissionAuthority.PermissionAuthorityType type) {
        super(message, type);
    }
}
