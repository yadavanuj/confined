package com.github.yadavanuj.confined.types;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class ConfinedConfig {
    @NonNull
    private PermitType permitType;
}

