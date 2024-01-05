package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.PermitType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RegistryStore {
    public static final RegistryStore INSTANCE = new RegistryStore();
    private final Map<String, io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry> registries = new HashMap<>();
    private final PermitType permitType = PermitType.CircuitBreaker;
    public static RegistryStore getInstance() {
        return INSTANCE;
    }
}