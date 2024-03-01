package com.github.yadavanuj.confined.tests;

import com.github.yadavanuj.confined.internal.ConfinedUtils;
import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ConfinedUtilsTest {

    @Test
    void testAcquirePermitExceptionally_Success() throws ConfinedException {
        // Given
        Semaphore semaphore = new Semaphore(1);
        long waitDurationInMillis = 100;

        // When
        boolean result = ConfinedUtils.acquirePermitExceptionally(semaphore, waitDurationInMillis);

        // Then
        assertTrue(result);
    }

    @Test
    void testAcquirePermitExceptionally_Failure() {
        // Given
        Semaphore semaphore = new Semaphore(0);
        long waitDurationInMillis = 100;

        // When
        ConfinedException exception = assertThrows(ConfinedException.class, () ->
                ConfinedUtils.acquirePermitExceptionally(semaphore, waitDurationInMillis));

        // Then
        assertEquals(ConfinedErrorCode.FailedToAcquirePermit, exception.getErrorCode());
    }

    @Test
    void testAcquirePermitExceptionally_InterruptedException() {
        // Given
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquireUninterruptibly();
        long waitDurationInMillis = 100;

        // When
        Thread.currentThread().interrupt();
        ConfinedException exception = assertThrows(ConfinedException.class, () ->
                ConfinedUtils.acquirePermitExceptionally(semaphore, waitDurationInMillis));

        // Then
        assertEquals(ConfinedErrorCode.InterruptedWhileAcquiringPermit, exception.getErrorCode());
//        assertTrue(Thread.interrupted());
    }

    @Test
    void testAcquirePermitExceptionally_NoResults() {
        // Given
        Semaphore semaphore = new Semaphore(0);
        long waitDurationInMillis = 100;

        // When
        ConfinedException exception = assertThrows(ConfinedException.class, () ->
                ConfinedUtils.acquirePermitExceptionally(semaphore, waitDurationInMillis));

        // Then
        assertEquals(ConfinedErrorCode.FailedToAcquirePermit, exception.getErrorCode());
    }

    @Test
    void testSleepUninterruptedly() {
        // When
        long startTime = System.currentTimeMillis();
        ConfinedUtils.sleepUninterruptedly(100);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(endTime - startTime >= 100);
    }

    @Test
    void testSleepingSupplier() {
        // Given
        long sleepDurationInMillis = 100;
        Supplier<String> supplier = () -> "Hello";

        // When
        Supplier<String> sleepingSupplier = ConfinedUtils.sleepingSupplier(supplier, sleepDurationInMillis);
        long startTime = System.currentTimeMillis();
        String result = sleepingSupplier.get();
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals("Hello", result);
        assertTrue(endTime - startTime >= 100);
    }
}
