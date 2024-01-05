package com.github.yadavanuj.confined.bulkhead;

import com.github.yadavanuj.confined.Confined;
import com.github.yadavanuj.confined.PermitType;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.commons.ConfinedException;
import com.github.yadavanuj.confined.commons.ConfinedSupplier;
import com.github.yadavanuj.confined.commons.ConfinedUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;

public class BulkHeadIntegrationTests {
    private static final String SERVICE_KEY_FORMAT = "bulkhead:service%d";
    private Confined confined;
    private List<Registry<BulkHeadConfig>> registries;

    @BeforeEach
    public void beforeEach() {
        confined = new Confined.Impl();
        registries = new ArrayList<>();
    }

    public static void main(String[] args) throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;

        BulkHeadIntegrationTests instance = new BulkHeadIntegrationTests();
        instance.beforeEach();
        instance.createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

        String shouldBeOutput = UUID.randomUUID().toString();
        Supplier<String> supplier = () -> {
            System.out.println("Started");
            System.out.println(System.currentTimeMillis());
            ConfinedUtils.sleepUninterruptedly(maxWaitDuration * 2);
            return shouldBeOutput;
        };

        ConfinedSupplier<String> confinedSupplier = instance.registries.get(0).decorate(getServiceKey(serviceId), supplier);
        ExecutorService service = Executors.newFixedThreadPool(concurrentCallCount * 2);

        for (int i = 0; i < concurrentCallCount * 2; i++) {
            service.submit(() -> {
                try {
                    String decorateSupplierOutput = confinedSupplier.get();
                    System.out.println(decorateSupplierOutput);
                    // Assertions.assertEquals(shouldBeOutput, decorateSupplierOutput);
                    System.out.println("Executed");
                    System.out.println(System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdown();
    }

    @Test
    @DisplayName("Bulk head should decorate supplier and permit execution")
    public void testSupplierDecoration() throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;
        createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

        String shouldBeOutput = UUID.randomUUID().toString();
        ConfinedSupplier<String> decoratedSupplier = createSupplier(getServiceKey(serviceId), shouldBeOutput);
        assertSupplier(decoratedSupplier, shouldBeOutput);
    }

    @Test
    @DisplayName("Should permit multiple simultaneous operations with single Bulk Head when limits are present and not throw exception")
    public void testMultipleOperations0() throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 2;
        int maxWaitDuration = 100;
        createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

        ExecutorService service = Executors.newFixedThreadPool(concurrentCallCount);

        Map<String, Object> output = new HashMap<>();
        List<Callable<Void>> callables = new ArrayList<>();

        for (int i = 0; i < concurrentCallCount; i++) {
            String shouldBeOutput = UUID.randomUUID().toString();
            ConfinedSupplier<String> decoratedSupplier = createSupplier(getServiceKey(serviceId), shouldBeOutput);

            service.submit(() -> {
                try {
                    assertSupplier(decoratedSupplier, shouldBeOutput);
                } catch (ConfinedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    @DisplayName("Should throw exception when permits are not available within wait time duration")
    public void testMultipleOperations1() throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;
        int operationCount = concurrentCallCount * 2;
        createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

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
            ConfinedSupplier<String> decoratedSupplier = createSleepingSupplier(getServiceKey(serviceId), shouldBeOutput, sleepDurationInMillis);
            callables.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        output.put(String.format("itr-%d-start", finalI), true);
                        output.put(String.format("itr-%d-result", finalI), decoratedSupplier.get());
                    } catch (ConfinedException e) {
                        output.put(String.format("itr-%d-exception", finalI), true);
                    }
                    return null;
                }
            });
        }

        try {
            service.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRegistry(int serviceId, int maxConcurrentCalls, int maxWaitDuration) throws ConfinedException {
        BulkHeadConfig bulkHeadConfig = BulkHeadConfig.builder()
                .key(getServiceKey(serviceId))
                .permitType(PermitType.BulkHead)
                .maxConcurrentCalls(maxConcurrentCalls)
                .maxWaitDurationInMillis(maxWaitDuration)
                .build();

        @SuppressWarnings("unchecked")
        Registry<BulkHeadConfig> registry = (Registry<BulkHeadConfig>) confined.register(bulkHeadConfig);
        registries.add(registry);
    }

    private ConfinedSupplier<String> createSupplier(String serviceKey, String shouldBeOutput) {
        Supplier<String> supplier = () -> shouldBeOutput;
        return registries.get(0).decorate(serviceKey, supplier);
    }

    private ConfinedSupplier<String> createSleepingSupplier(String serviceKey,
                                                            String shouldBeOutput,
                                                            long sleepDurationInMillis) {
        Supplier<String> supplier = () -> shouldBeOutput;
        Supplier<String> sleepingSupplier = ConfinedUtils.sleepingSupplier(supplier, sleepDurationInMillis);
        return registries.get(0).decorate(serviceKey, sleepingSupplier);
    }

    private void assertSupplier(ConfinedSupplier<String> decoratedSupplier,
                                String shouldBeOutput) throws ConfinedException {
        String decorateSupplierOutput = decoratedSupplier.get();
        Assertions.assertEquals(shouldBeOutput, decorateSupplierOutput);
    }

    private void assertSupplier(ConfinedSupplier<String> decoratedSupplier,
                                String shouldBeOutput,
                                Map<String, Object> output) throws ConfinedException {
        String decorateSupplierOutput = decoratedSupplier.get();
        Assertions.assertEquals(shouldBeOutput, decorateSupplierOutput);
    }

    private static String getServiceKey(int serviceId) {
        return String.format(SERVICE_KEY_FORMAT, serviceId);
    }
}
