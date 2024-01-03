package com.github.yadavanuj.confined.commons;

@FunctionalInterface
public interface ConfinedSupplier<T> {
    T get() throws ConfinedException;
}
