package com.github.yadavanuj.confined.circuitbreaker;

import com.github.yadavanuj.confined.Confined;
import com.github.yadavanuj.confined.types.PermitType;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.commons.TestHelper;
import com.github.yadavanuj.confined.types.CircuitBreakerConfig;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.SlidingWindowType;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class CircuitBreakerIntegrationTests {
    private static final String SERVICE_KEY_FORMAT = "circuitbreaker:service%d";
    private final TestHelper helper = new TestHelper(SERVICE_KEY_FORMAT);
    private Confined confined;
    private List<Registry<CircuitBreakerConfig>> registries;

    @BeforeEach
    public void beforeEach() {
        confined = new Confined.Impl();
        registries = new ArrayList<>();
    }

    public static void main(String[] args) throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;
        int operationCount = concurrentCallCount * 2;
        @SuppressWarnings("unchecked") CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.builder()
                .operationName(getServiceKey(serviceId))
                .permitType(PermitType.CircuitBreaker)
                .failureRateThresholdPercentage(20)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .recordExceptions(new Class[]{RuntimeException.class, ConfinedException.class})
                .build();
        CircuitBreakerIntegrationTests instance = new CircuitBreakerIntegrationTests();
        instance.beforeEach();

        @SuppressWarnings("unchecked")
        Registry<CircuitBreakerConfig> registry = (Registry<CircuitBreakerConfig>) instance.confined.register(circuitBreakerConfig);
        instance.registries.add(registry);

        // Have more threads available
        ExecutorService service = Executors.newFixedThreadPool(operationCount);

        Map<String, Object> output = new HashMap<>();
        List<Callable<Void>> callables = new ArrayList<>();
        for (int i = 0; i < operationCount; i++) {
            int finalI = i;
            String shouldBeOutput = UUID.randomUUID().toString();
            output.put(String.format("itr-%d-shouldBeOutput", finalI), shouldBeOutput);

            // Increase sleep duration to force exception
            long sleepDurationInMillis = maxWaitDuration * 20;
            ConfinedSupplier<String> decoratedSupplier = instance.createSupplierWhereOddNumberOperationsThrow(finalI,
                    serviceId,
                    shouldBeOutput,
                    sleepDurationInMillis);

            callables.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        output.put(String.format("itr-%d-start", finalI), true);
                        output.put(String.format("itr-%d-result", finalI), decoratedSupplier.get());
                    } catch (ConfinedException e) {
                        e.printStackTrace();
                        output.put(String.format("itr-%d-exception", finalI), true);
                        output.put(String.format("itr-%d-exception-message", finalI), e.getMessage());
                    }
                    return null;
                }
            });

            try {
                service.invokeAll(callables);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callables.remove(0);
        }


        service.shutdown();
        System.out.println(output);
    }

    private ConfinedSupplier<String> createSupplierWhereOddNumberOperationsThrow(int iteration,
                                                                                 int serviceId,
                                                                                 String shouldBeOutput,
                                                                                 long sleepDurationInMillis) {
        Supplier<String> supplier = () -> shouldBeOutput;
        Supplier<String> sleepingSupplier = new Supplier<String>() {
            @Override
            public String get() {
                if (iteration % 2 != 0) {
                    return shouldBeOutput;
                } else {
                    System.out.println("Iteration " + iteration);
                    throw new RuntimeException("Failed");
                }
            }
        };
        return registries.get(0).decorate(getServiceKey(serviceId), sleepingSupplier);
    }

    private static String getServiceKey(int serviceId) {
        return String.format(SERVICE_KEY_FORMAT, serviceId);
    }
}
