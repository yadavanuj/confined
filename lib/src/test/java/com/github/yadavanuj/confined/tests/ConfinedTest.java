package com.github.yadavanuj.confined.tests;

import com.github.yadavanuj.confined.Confined;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.internal.permits.bulkhead.BulkHeadRegistry;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.CircuitBreakerRegistry;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.SlidingWindowType;
import com.github.yadavanuj.confined.internal.permits.ratelimiter.RateLimiterRegistry;
import com.github.yadavanuj.confined.types.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ConfinedTest {

    @Test
    void testInit_DefaultConstructor() {
        // When
        Confined confined = Confined.init();

        // Then
        assertNotNull(confined);
    }

    @Test
    void testInit_CustomConstructor() {
        // Given
        Confined.RegistryProvider registryProvider = new Confined.RegistryProvider();

        // When
        Confined confined = new Confined.Impl(registryProvider);

        // Then
        assertNotNull(confined);
    }

    @Test
    void testRegister_BulkHead() throws ConfinedException {
        // Given
        Confined confined = Confined.init();
        ConfinedConfig config = getBulkHeadConfig();

        // When
        Registry<? extends ConfinedConfig> registry = confined.register(config);

        // Then
        assertNotNull(registry);
        System.out.println(registry.getClass());
        assertTrue(registry instanceof BulkHeadRegistry);
        assertEquals(PermitType.BulkHead, registry.permitType());
    }

    @Test
    void testRegister_CircuitBreaker() throws ConfinedException {
        // Given
        Confined confined = Confined.init();
        ConfinedConfig config = getCircuitBreakerConfig();

        // When
        Registry<? extends ConfinedConfig> registry = confined.register(config);

        // Then
        assertNotNull(registry);
        assertTrue(registry instanceof CircuitBreakerRegistry);
        assertEquals(PermitType.CircuitBreaker, registry.permitType());
    }

    @Test
    void testRegister_RateLimiter() throws ConfinedException {
        // Given
        Confined confined = Confined.init();
        ConfinedConfig config = getRateLimiterConfig();

        // When
        Registry<? extends ConfinedConfig> registry = confined.register(config);

        // Then
        assertNotNull(registry);
        assertTrue(registry instanceof RateLimiterRegistry);
        assertEquals(PermitType.RateLimiter, registry.permitType());
    }


    BulkHeadConfig getBulkHeadConfig(){
        BulkHeadConfig bulkHeadConfig = BulkHeadConfig.builder()
                .key("test-key-bh")
                .permitType(PermitType.BulkHead)
                .maxConcurrentCalls(1)
                .maxWaitDurationInMillis(100)
                .build();
return bulkHeadConfig;
    }
    CircuitBreakerConfig getCircuitBreakerConfig(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.builder()
                .operationName("test-key-cb")
                .permitType(PermitType.CircuitBreaker)
                .failureRateThresholdPercentage(20)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .recordExceptions(new Class[]{RuntimeException.class, ConfinedException.class})
                .build();
        return circuitBreakerConfig;
    }
    RateLimiterConfig getRateLimiterConfig(){
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.builder()
                .operationName("test-key-rl")
                .permitType(PermitType.RateLimiter)
                .permissionProvider((permitKey) -> true)
                .timeoutSlicingFactor(2)
                .properties(Mockito.mock())
                .build();
        return rateLimiterConfig;
    }
}
