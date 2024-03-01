package com.github.yadavanuj.confined.types;

import lombok.Getter;

@Getter
public class ConfinedException extends Exception {
    private final ConfinedErrorCode errorCode;

    private String className;

    public ConfinedException(ConfinedErrorCode errorCode) {
        super(errorCode.getValue());
        this.errorCode = errorCode;
    }

    public ConfinedException(ConfinedErrorCode errorCode, Throwable throwable) {
        super(errorCode.getValue(), throwable);
        this.errorCode = errorCode;
    }

    public ConfinedException(ConfinedErrorCode errorCode, String className) {
        this(errorCode);
        this.className = className;
    }

    public ConfinedException( ConfinedErrorCode errorCode, String className,Throwable cause) {
        this(errorCode, cause);
        this.className = className;
    }
}
