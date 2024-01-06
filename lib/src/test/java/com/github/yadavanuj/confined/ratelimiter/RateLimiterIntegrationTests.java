package com.github.yadavanuj.confined.ratelimiter;

import com.github.yadavanuj.confined.Confined;
import com.github.yadavanuj.confined.types.PermitType;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.internal.ConfinedUtils;
import com.github.yadavanuj.confined.commons.TestHelper;
import com.github.yadavanuj.confined.types.RateLimiterConfig;
import com.github.yadavanuj.confined.internal.permits.ratelimiter.RateLimiterProperties;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class RateLimiterIntegrationTests {
    private static final String SERVICE_KEY_FORMAT = "ratelimiter:service%d";
    private final TestHelper helper = new TestHelper(SERVICE_KEY_FORMAT);
    private Confined confined;
    private List<Registry<RateLimiterConfig>> registries;

    @BeforeEach
    public void beforeEach() {
        confined = new Confined.Impl();
        registries = new ArrayList<>();
    }

    public static void main(String[] args) throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;
        int operationCount = concurrentCallCount * 4;

        RateLimiterIntegrationTests instance = new RateLimiterIntegrationTests();
        instance.beforeEach();

        RateLimiterProperties properties = RateLimiterProperties.builder()
                .key(instance.helper.getServiceKey(serviceId))
                .limitForPeriod(2)
                .limitRefreshPeriodInMillis(1000)
                .timeoutInMillis(100)
                .build();
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.builder()
                .operationName(instance.helper.getServiceKey(serviceId))
                .permitType(PermitType.RateLimiter)
                .permissionProvider((permitKey) -> true)
                .timeoutSlicingFactor(2)
                .properties(properties)
                .build();

        @SuppressWarnings("unchecked")
        Registry<RateLimiterConfig> registry = (Registry<RateLimiterConfig>) instance.confined.register(rateLimiterConfig);
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
            Supplier<String> sleepySupplier = ConfinedUtils.sleepingSupplier(() -> shouldBeOutput, sleepDurationInMillis);
            ConfinedSupplier<String> decoratedSupplier = instance.registries.get(0).decorate(instance.helper.getServiceKey(serviceId), sleepySupplier);

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
        }

        try {
            service.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        System.out.println(output);
    }
}
