package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.PermissionAuthority;

public class BulkheadException extends PermissionAuthority.PermissionAuthorityException {
    public BulkheadException(PermissionAuthority.PermissionAuthorityType type) {
        super(type);
    }

    public BulkheadException(Throwable throwable, PermissionAuthority.PermissionAuthorityType type) {
        super(throwable, type);
    }

    public BulkheadException(String message, Throwable throwable, PermissionAuthority.PermissionAuthorityType type) {
        super(message, throwable, type);
    }

    public BulkheadException(String message, PermissionAuthority.PermissionAuthorityType type) {
        super(message, type);
    }
}
