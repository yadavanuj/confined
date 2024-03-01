package com.github.yadavanuj.confined.types.test;

import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfinedExceptionTest {

    @Test
    void testConstructorWithErrorCode() {
        // Given
        ConfinedErrorCode errorCode = ConfinedErrorCode.FailedToAcquirePermit;

        // When
        ConfinedException exception = new ConfinedException(errorCode);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
        assertNull(exception.getClassName());
    }

    @Test
    void testConstructorWithErrorCodeAndThrowable() {
        // Given
        ConfinedErrorCode errorCode = ConfinedErrorCode.FailureWhileExecutingOperation;
        Throwable throwable = new RuntimeException("Test exception");

        // When
        ConfinedException exception = new ConfinedException(errorCode, throwable);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(throwable, exception.getCause());
        assertNull(exception.getClassName());
    }

    @Test
    void testConstructorWithErrorCodeAndClassName() {
        // Given
        ConfinedErrorCode errorCode = ConfinedErrorCode.FailedToAcquirePermit;
        String className = "TestClass";

        // When
        ConfinedException exception = new ConfinedException(errorCode, className);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
        assertEquals(className, exception.getClassName());
    }

    @Test
    void testConstructorWithErrorCodeClassNameAndThrowable() {
        // Given
        ConfinedErrorCode errorCode = ConfinedErrorCode.FailureWhileExecutingOperation;
        String className = "TestClass";
        Throwable throwable = new RuntimeException("Test exception");

        // When
        ConfinedException exception = new ConfinedException(errorCode, className, throwable);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(throwable, exception.getCause());
        assertEquals(className, exception.getClassName());
    }
}
