package com.github.yadavanuj.confined.circuitbreaker;

public class CircuitBreakerException extends RuntimeException {
    public CircuitBreakerException(Exception exception) {
        super(exception);
    }
}
