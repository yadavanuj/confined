package com.github.yadavanuj.confined.types;

import com.github.yadavanuj.confined.internal.permits.circuitbreaker.SlidingWindowType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class CircuitBreakerConfig extends ConfinedConfig {
    private String operationName;
    /**
     * Configures the failure rate threshold in percentage. If the failure rate is equal to or
     * greater than the threshold, the CircuitBreaker transitions to open and starts short-circuiting
     * calls. The threshold must be greater than 0 and not greater than 100. Default value is 50 percentage.
     * build() will throw:
     * IllegalArgumentException – if failureRateThreshold <= 0 || failureRateThreshold > 100
     */
    @Builder.Default
    private float failureRateThresholdPercentage = 50;

    /**
     * Configures the number of permitted calls when the CircuitBreaker is half open.
     * The size must be greater than 0. Default size is 10.
     * build() Throws:
     * IllegalArgumentException – if permittedNumberOfCallsInHalfOpenState < 1
     */
    @Builder.Default
    private int permittedNumberOfCallsInHalfOpenState = 10;

    /**
     * Configures the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.
     * slidingWindowSize configures the size of the sliding window. Sliding window can either be count-based or
     * time-based, specified by {@link #slidingWindowType}. {@link #minimumNumberOfCalls} configures the minimum
     * number of calls which are required (per sliding window period) before the CircuitBreaker can calculate the
     * error rate. For example, if {@link #minimumNumberOfCalls} is 10, then at least 10 calls must be recorded,
     * before the failure rate can be calculated. If only 9 calls have been recorded, the CircuitBreaker will not
     * transition to open, even if all 9 calls have failed.
     * If slidingWindowSize is 100 and {@link #slidingWindowType} is COUNT_BASED, the last 100 calls are recorded
     * and aggregated. If slidingWindowSize is 10 and {@link #slidingWindowType} is TIME_BASED, the calls of the
     * last 10 seconds are recorded and aggregated. The slidingWindowSize must be greater than 0. The
     * {@link #minimumNumberOfCalls} must be greater than 0. If the {@link #slidingWindowType} is COUNT_BASED, the
     * {@link #minimumNumberOfCalls} may not be greater than slidingWindowSize. If a greater value is provided,
     * {@link #minimumNumberOfCalls} will be equal to slidingWindowSize. If the {@link #slidingWindowType} is
     * TIME_BASED, the {@link #minimumNumberOfCalls} may be any amount. Default slidingWindowSize is 20,
     * {@link #minimumNumberOfCalls} is 20 and {@link #slidingWindowType} is COUNT_BASED.
     * build() Throws:
     * IllegalArgumentException – if slidingWindowSize < 1 || minimumNumberOfCalls < 1
     */
    @Builder.Default
    private int slidingWindowSize = 20;

    /**
     * Configures the type of the sliding window which is used to record the outcome of calls
     * when the CircuitBreaker is closed. Sliding window can either be count-based or time-based.
     * Default slidingWindowType is COUNT_BASED.
     */
    @Builder.Default
    private SlidingWindowType slidingWindowType = SlidingWindowType.COUNT_BASED;

    /**
     * Configures the minimum number of calls which are required (per sliding window period)
     * before the CircuitBreaker can calculate the error rate. For example, if minimumNumberOfCalls is 10,
     * then at least 10 calls must be recorded, before the failure rate can be calculated. If only 9 calls
     * have been recorded, the CircuitBreaker will not transition to open, even if all 9 calls have failed.
     * Default minimumNumberOfCalls is 20
     * build() Throws:
     * IllegalArgumentException – if minimumNumberOfCalls < 1
     */
    @Builder.Default
    private int minimumNumberOfCalls = 20;

    /**
     * Configures a threshold in percentage. The CircuitBreaker considers a call as slow when the call duration
     * is greater than {@link #slowCallThresholdInMillis}. When the percentage of slow calls is equal to or
     * greater than the threshold, the CircuitBreaker transitions to open and starts short-circuiting calls.
     * The threshold must be greater than 0 and not greater than 100. Default value is 60 percentage which
     * means that all recorded calls must be slower than {@link #slowCallThresholdInMillis}.
     * build() Throws:
     * IllegalArgumentException – if slowCallRateThreshold <= 0 || slowCallRateThreshold > 10
     */
    @Builder.Default
    private float slowCallRateThresholdPercentage = 60;

    /**
     * Configures the threshold in milliseconds above which calls are considered as slow and increase
     * the slow calls' percentage. Default value is 2 seconds.
     */
    @Builder.Default
    private int slowCallThresholdInMillis = 1000;

    /**
     * Configures CircuitBreaker with a fixed wait duration which controls how long the
     * CircuitBreaker should stay in Half Open state, before it switches to open. This is an
     * optional parameter.
     * By default, CircuitBreaker will stay in Half Open state until {@code minimumNumberOfCalls} is
     * completed with either success or failure.
     * build() Throws IllegalArgumentException if {@code maxWaitDurationInHalfOpenStateInMillis < 0}
     */
    @Builder.Default
    private long maxWaitInHalfOpenStateInMillis = 0;

    /**
     * Configures an interval function with a fixed wait duration which controls how long the
     * CircuitBreaker should stay open, before it switches to half open. Default value is 60
     * seconds.
     * build() throws IllegalArgumentException if {@code waitDurationInOpenState.toMillis() < 1}
     */
    @Builder.Default
    private long waitInOpenStateInMillis = 60;

    private Class<? extends Throwable>[] recordExceptions;
    private Class<? extends Throwable>[] ignoreExceptions;

    /**
     * If true, the library will log additional output for debugging purposes.
     */
    @Builder.Default
    private boolean debug = false;
}
