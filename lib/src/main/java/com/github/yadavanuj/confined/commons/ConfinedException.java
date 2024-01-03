package com.github.yadavanuj.confined.commons;

import lombok.Getter;

@Getter
public class ConfinedException extends Exception {
    private final ConfinedErrorCode errorCode;

    public ConfinedException(ConfinedErrorCode errorCode) {
        super(errorCode.getValue());
        this.errorCode = errorCode;
    }
}
