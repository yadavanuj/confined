package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.types.ConfinedException;

@FunctionalInterface
public interface ConfinedSupplier<T> {
    T get() throws ConfinedException;
}
