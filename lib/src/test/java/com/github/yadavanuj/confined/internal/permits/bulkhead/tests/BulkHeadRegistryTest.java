package com.github.yadavanuj.confined.internal.permits.bulkhead.tests;

import com.github.yadavanuj.confined.internal.permits.bulkhead.BulkHeadRegistry;
import com.github.yadavanuj.confined.types.BulkHeadConfig;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class BulkHeadRegistryTest {

    @Test
    void testOnAcquire_Success() throws ConfinedException,NoSuchMethodException {
        // Given
        BulkHeadConfig config = getBulkHeadConfig();
        BulkHeadRegistryMock bulkHeadRegistry = new BulkHeadRegistryMock(config);

        // When
        boolean result = bulkHeadRegistry.testAcquire("test");

        // Then
        assertTrue(result);
    }

//    @Test
    void testOnAcquire_Failure() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        // Given
        BulkHeadConfig config = getBulkHeadConfig();
        BulkHeadRegistryMock bulkHeadRegistry = new BulkHeadRegistryMock(config);

        // When
        assertThrows(ConfinedException.class, () -> bulkHeadRegistry.testAcquire("test"));
    }

    @Test
    void testOnRelease() throws NoSuchMethodException, NoSuchFieldException {
        // Given
        BulkHeadConfig config = getBulkHeadConfig();
        BulkHeadRegistryMock bulkHeadRegistry = new BulkHeadRegistryMock(config);

        // When
        bulkHeadRegistry.testRelease("test");

        // Then
        assertEquals(2, bulkHeadRegistry.testGetSemaphore().availablePermits());
    }

    @Test
    void testPermitType() {
        // Given
        BulkHeadConfig config = getBulkHeadConfig();
        BulkHeadRegistry bulkHeadRegistry = new BulkHeadRegistry(config);

        // When
        PermitType permitType = bulkHeadRegistry.permitType();

        // Then
        assertEquals(PermitType.BulkHead, permitType);
    }

    @Test
    void testGetName() {
        // Given
        BulkHeadConfig config = getBulkHeadConfig();
        BulkHeadRegistry bulkHeadRegistry = new BulkHeadRegistry(config);

        // When
        String name = bulkHeadRegistry.getName();

        // Then
        assertEquals("BH", name);
    }

    private BulkHeadConfig getBulkHeadConfig(){
        BulkHeadConfig bulkHeadConfig = BulkHeadConfig.builder()
                .key("test-key")
                .permitType(PermitType.BulkHead)
                .maxConcurrentCalls(1)
                .maxWaitDurationInMillis(100)
                .build();
return bulkHeadConfig;
    }

static class BulkHeadRegistryMock extends BulkHeadRegistry{

    public BulkHeadRegistryMock(BulkHeadConfig config) {
        super(config);
    }

    public boolean testAcquire(String key) throws ConfinedException{
        return acquire(key);
    }

    public void testRelease(String key) {
        release(key);
    }

    public Semaphore testGetSemaphore(){return getSemaphore();}
}
}



