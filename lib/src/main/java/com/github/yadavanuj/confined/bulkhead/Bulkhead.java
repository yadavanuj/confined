package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.PermissionAuthority;

import java.lang.ref.WeakReference;

public interface Bulkhead {
    String getKey();
    boolean acquire() throws PermissionAuthority.PermissionAuthorityException;

    public class Core implements Bulkhead {
        private final String key;
        private final WeakReference<BulkheadPermissionAuthority> registryRef;

        public Core(String key, WeakReference<BulkheadPermissionAuthority> registryRef) {
            this.key = key;
            this.registryRef = registryRef;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public boolean acquire() throws BulkheadException {

            return false;
        }
    }
}
