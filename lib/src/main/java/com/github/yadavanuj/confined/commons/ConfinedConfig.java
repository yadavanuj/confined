package com.github.yadavanuj.confined.commons;

import com.github.yadavanuj.confined.PermitType;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class ConfinedConfig {
    @NonNull
    private PermitType permitType;
}

