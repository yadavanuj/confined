
package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Policy;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
class RegistryStore {
    private static final RegistryStore INSTANCE = new RegistryStore();
    private final Map<String, Policy> policies;
    private final Map<String, BulkHeadConfig> configurations;
    private final Policy.PolicyType policyType = Policy.PolicyType.BulkHead;

    RegistryStore(Map<String, Policy> policies, Map<String, BulkHeadConfig> configurations) {
        this.policies = policies;
        this.configurations = configurations;
    }

    RegistryStore() {
        this(new HashMap<>(), new HashMap<>());
    }

    public static RegistryStore getInstance() {
        return INSTANCE;
    }
}