package com.github.yadavanuj.confined.types;

import lombok.Getter;

@Getter
public enum ConfinedErrorCode {
    FailedToAcquirePermit("FailedToAcquirePermit"),
    InterruptedWhileAcquiringPermit("InterruptedWhileAcquiringPermit"),
    FailedToInstantiateRegistry("FailedToInstantiateRegistry"),
    FailureWhileExecutingOperation("FailureWhileExecutingOperation"),
    RegistryNotFound("RegistryNotFound")
    ;

    private final String value;

    ConfinedErrorCode(String value) {
        this.value = value;
    }
}
