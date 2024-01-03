package com.github.yadavanuj.confined.commons;

import lombok.Getter;

@Getter
public enum ConfinedErrorCode {
    FailedToAcquirePermit("FailedToAcquirePermit"),
    InterruptedWhileAcquiringPermit("InterruptedWhileAcquiringPermit")
    ;

    private final String value;

    ConfinedErrorCode(String value) {
        this.value = value;
    }
}
