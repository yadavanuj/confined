package com.github.yadavanuj.confined.internal.permits.circuitbreaker;

public class CircuitBreakerException extends RuntimeException {
    public CircuitBreakerException(Exception exception) {
        super(exception);
    }
}
