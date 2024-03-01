package com.github.yadavanuj.confined.internal.permits.circuitbreaker.test;

import com.github.yadavanuj.confined.ConfinedFunction;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.CircuitBreakerRegistry;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.SlidingWindowType;
import com.github.yadavanuj.confined.types.CircuitBreakerConfig;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CircuitBreakerRegistryTest {

    @Test
    void testOnAcquire_Success() throws ConfinedException {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistryMock circuitBreakerRegistry = new CircuitBreakerRegistryMock(config);

        // When
        boolean result = circuitBreakerRegistry.testAcquire("testKey");

        // Then
        assertEquals(true, result);
    }

    @Test
    void testOnRelease() {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistryMock circuitBreakerRegistry = new CircuitBreakerRegistryMock(config);

        // When
        circuitBreakerRegistry.testRelease("testKey");

        // Then: Perform necessary assertions
    }

    @Test
    void testPermitType() {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistry circuitBreakerRegistry = new CircuitBreakerRegistry(config);

        // When
        PermitType permitType = circuitBreakerRegistry.permitType();

        // Then
        assertEquals(PermitType.CircuitBreaker, permitType);
    }

    @Test
    void testGetName() {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistry circuitBreakerRegistry = new CircuitBreakerRegistry(config);

        // When
        String name = circuitBreakerRegistry.getName();

        // Then
        assertEquals("CB", name);
    }


    @Test
    void testDecorateSupplier() throws ConfinedException {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistryMock circuitBreakerRegistry = new CircuitBreakerRegistryMock(config);

        // When
        ConfinedSupplier<Integer> decoratedSupplier = circuitBreakerRegistry.decorate("testKey", (Supplier<Integer>) () -> 42);

        // Then
        assertNotNull(decoratedSupplier);
        assertEquals(42, decoratedSupplier.get());
    }

    @Test
    void testDecorateFunction() throws ConfinedException {
        // Given
        CircuitBreakerConfig config = getCircuitBreakerConfig();
        CircuitBreakerRegistryMock circuitBreakerRegistry = new CircuitBreakerRegistryMock(config);

        // When
        Function<String, Integer> function = Integer::parseInt;

        // When
        ConfinedFunction<String, Integer> decoratedFunction = circuitBreakerRegistry.decorate("testKey", function);

        // Then
        assertNotNull(decoratedFunction);
        assertEquals(42, decoratedFunction.apply("42"));
    }

    CircuitBreakerConfig getCircuitBreakerConfig(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.builder()
                .operationName("testKey")
                .permitType(PermitType.CircuitBreaker)
                .failureRateThresholdPercentage(20)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .recordExceptions(new Class[]{RuntimeException.class, ConfinedException.class})
                .build();
        return circuitBreakerConfig;
    }

    static class CircuitBreakerRegistryMock extends CircuitBreakerRegistry {
        public CircuitBreakerRegistryMock(CircuitBreakerConfig config) {
            super(config);
        }

        public boolean testAcquire(String key) throws ConfinedException {
            return acquire(key);
        }

        public void testRelease(String key) {
            release(key);
        }

    }
}
