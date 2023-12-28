package com.github.yadavanuj.confined.ratelimiter;

import lombok.Getter;

@Getter
public enum RefillStrategy {
    LAZY("lazy"),
    GREEDY("greedy");
    private final String value;

    RefillStrategy(String value) {
        this.value = value;
    }
}
