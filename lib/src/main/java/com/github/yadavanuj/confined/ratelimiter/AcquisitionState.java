package com.github.yadavanuj.confined.ratelimiter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AcquisitionState {
    @Builder.Default
    private int cycle = 0;
    @Builder.Default
    private long startTime = System.currentTimeMillis();
}
