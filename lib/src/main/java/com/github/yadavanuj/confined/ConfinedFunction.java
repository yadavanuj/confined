package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.types.ConfinedException;

public interface ConfinedFunction <I, O> {
    O apply(I input) throws ConfinedException;
}
