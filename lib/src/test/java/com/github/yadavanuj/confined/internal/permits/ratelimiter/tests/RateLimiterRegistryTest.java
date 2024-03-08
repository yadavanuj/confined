package com.github.yadavanuj.confined.internal.permits.ratelimiter.tests;

import com.github.yadavanuj.confined.internal.permits.ratelimiter.RateLimiterRegistry;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;
import com.github.yadavanuj.confined.types.RateLimiterConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterRegistryTest {

//    @Test
    void testOnAcquire_Success() throws ConfinedException {
        RateLimiterConfig config = getRateLimiterConfig();
        RateLimiterRegistryMock rateLimiterRegistry = new RateLimiterRegistryMock(config);

        boolean result = rateLimiterRegistry.testAcquire("test");

        assertTrue(result);
    }

//    @Test
    void testOnAcquire_Failure() {
        RateLimiterConfig config = getRateLimiterConfig();
        RateLimiterRegistryMock rateLimiterRegistry =
                new RateLimiterRegistryMock(config);

        assertThrows(ConfinedException.class, () -> rateLimiterRegistry.testAcquire("test"));
    }

    @Test
    void testOnRelease() {
        RateLimiterConfig config = getRateLimiterConfig();
        RateLimiterRegistryMock rateLimiterRegistry =
                new RateLimiterRegistryMock(config);

        rateLimiterRegistry.testRelease("test");

        // Assuming the semaphore permits are adjusted accordingly, perform necessary assertions
    }

    @Test
    void testPermitType() {
        RateLimiterConfig config = getRateLimiterConfig();
        RateLimiterRegistry rateLimiterRegistry =
                new RateLimiterRegistry(config);

        PermitType permitType = rateLimiterRegistry.permitType();

        assertEquals(PermitType.RateLimiter, permitType);
    }

    @Test
    void testGetName() {
        RateLimiterConfig config = getRateLimiterConfig();
        RateLimiterRegistry rateLimiterRegistry = new RateLimiterRegistry(config);

        String name = rateLimiterRegistry.getName();

        assertEquals("RL", name);
    }


    RateLimiterConfig getRateLimiterConfig(){
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.builder()
                .operationName("test")
                .permitType(PermitType.RateLimiter)
                .permissionProvider((permitKey) -> true)
                .timeoutSlicingFactor(2)
                .properties(Mockito.mock())
                .build();
        return rateLimiterConfig;
    }

    static class RateLimiterRegistryMock extends RateLimiterRegistry {
        public RateLimiterRegistryMock(RateLimiterConfig config) {
            super(config);
        }
        public boolean testAcquire(String key) throws ConfinedException{
            return acquire(key);
        }

        public void testRelease(String key) {
            release(key);
        }

        public Semaphore testGetSemaphore(String key){return getSemaphore(key);}

    }
}
