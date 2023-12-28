package com.github.yadavanuj.confined;

import lombok.Getter;

import java.util.Objects;
import java.util.function.Supplier;

public interface PermissionAuthority {
    boolean isPermitted(String key) throws PermissionAuthorityException;
    PermissionAuthorityType getPermissionAuthorityType();

    static String getOperation(Supplier<String> keySupplier) {
        Objects.requireNonNull(keySupplier);

        final String name = keySupplier.get();
        Objects.requireNonNull(name);

        final String[] parts = name.split(":");
        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new IllegalArgumentException("Operation Name is required.");
        }

        return parts[1];
    }

    public static enum PermissionAuthorityType {
        CIRCUIT_BREAKER,
        BULK_HEAD,
        RATE_LIMITER
    }

    @Getter
    public static class PermissionAuthorityException extends RuntimeException {
        private final PermissionAuthorityType type;
        public PermissionAuthorityException(PermissionAuthorityType type) {
            super();
            this.type = type;
        }
        public PermissionAuthorityException(Throwable throwable, PermissionAuthorityType type) {
            super(throwable);
            this.type = type;
        }

        public PermissionAuthorityException(String message, Throwable throwable, PermissionAuthorityType type) {
            super(message, throwable);
            this.type = type;
        }

        public PermissionAuthorityException(String message, PermissionAuthorityType type) {
            super(message);
            this.type = type;
        }
    }
}
