import com.github.yadavanuj.confined.Confined;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.internal.ConfinedUtils;
import com.github.yadavanuj.confined.internal.permits.circuitbreaker.SlidingWindowType;
import com.github.yadavanuj.confined.types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class CircuitBreakerBulkHeadIntegrationTests {

    private static final String SERVICE_KEY_FORMAT = "bulkheadCB:service%d";
    private Confined confined;
    private List<Registry<? extends ConfinedConfig>> registries;

    @BeforeEach
    public void beforeEach() {
        confined = new Confined.Impl();
        registries = new ArrayList<>();
    }

    public static void main(String[] args) throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;

        CircuitBreakerBulkHeadIntegrationTests instance = new CircuitBreakerBulkHeadIntegrationTests();
        instance.beforeEach();
        instance.createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

        String shouldBeOutput = UUID.randomUUID().toString();
        Supplier<String> supplier = () -> {
            System.out.println("Started");
            System.out.println(System.currentTimeMillis());
            ConfinedUtils.sleepUninterruptedly(maxWaitDuration / 2 );// Both CB and BH registeries are getting with maxDuration. Avoiding the overlaps for 2 confined utils.
            return shouldBeOutput;
        };

        ConfinedSupplier<String> confinedSupplier = getConfinedSupplier(instance.registries, serviceId, supplier);
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

    private static ConfinedSupplier<String> getConfinedSupplier(List<Registry<? extends ConfinedConfig>> registries, int serviceId, Supplier<String> supplier) {
        ConfinedSupplier<String> confinedSupplier = registries.get(1).decorate(getServiceKey(serviceId),
                registries.get(0).decorate(getServiceKey(serviceId), supplier));
        return confinedSupplier;
    }
    private static ConfinedSupplier<String> getSleepingConfinedSupplier(List<Registry<? extends ConfinedConfig>> registries,
                                                                        int serviceId,
                                                                        long sleepDurationInMillis,
                                                                        Supplier<String> supplier) {
        Supplier<String> sleepingSupplier = ConfinedUtils.sleepingSupplier(supplier, sleepDurationInMillis);
        return getConfinedSupplier(registries, serviceId, sleepingSupplier);
    }

    @Test
    @DisplayName("Circuit Breaker followed by BH should decorate supplier and permit execution")
    public void testSupplierDecoration() throws ConfinedException {
        int serviceId = 1;
        int concurrentCallCount = 1;
        int maxWaitDuration = 100;
        createRegistry(serviceId, concurrentCallCount, maxWaitDuration);

        String shouldBeOutput = UUID.randomUUID().toString();
        ConfinedSupplier<String> decoratedSupplier = getConfinedSupplier(registries,serviceId,() -> shouldBeOutput);
        assertSupplier(decoratedSupplier, shouldBeOutput);
    }
    @Test
    @DisplayName("Circuit Breaker followed by BH with permits not available for the BH")
    public void testBHFailure()throws ConfinedException{
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
            ConfinedSupplier<String> decoratedSupplier =
                    getSleepingConfinedSupplier(this.registries, serviceId,
                    sleepDurationInMillis,
                    () -> shouldBeOutput);
            callables.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        output.put(String.format("itr-%d-start", finalI), true);
                        output.put(String.format("itr-%d-result", finalI), decoratedSupplier.get());
                    } catch (ConfinedException e) {
                        output.put(String.format("itr-%d-exception", finalI), true);
                        output.put(String.format("itr-%d-exception-type", finalI), e.getClassName());

                    }
                    return null;
                }
            });
        }

        try {
            service.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println(output);
            Assertions.assertTrue((Boolean) output.getOrDefault("itr-0-exception", false) || (Boolean) output.getOrDefault("itr-1-exception", false));
        }
    }

    private void createRegistry(int serviceId, int maxConcurrentCalls, int maxWaitDuration) throws ConfinedException {
        Registry<BulkHeadConfig> registry = createBHRegistry(serviceId, maxConcurrentCalls, maxWaitDuration);
        registries.add(registry);
        registries.add(createCBRegistry(serviceId));
    }

    private Registry<BulkHeadConfig> createBHRegistry(int serviceId, int maxConcurrentCalls, int maxWaitDuration) throws ConfinedException {
        BulkHeadConfig bulkHeadConfig = BulkHeadConfig.builder()
                .key(getServiceKey(serviceId))
                .permitType(PermitType.BulkHead)
                .maxConcurrentCalls(maxConcurrentCalls)
                .maxWaitDurationInMillis(maxWaitDuration)
                .build();

        @SuppressWarnings("unchecked")
        Registry<BulkHeadConfig> registry = (Registry<BulkHeadConfig>) confined.register(bulkHeadConfig);
        return registry;
    }


    private Registry<CircuitBreakerConfig> createCBRegistry(int serviceId) throws ConfinedException {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.builder()
                .operationName(getServiceKey(serviceId))
                .permitType(PermitType.CircuitBreaker)
                .failureRateThresholdPercentage(20)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .recordExceptions(new Class[]{RuntimeException.class, ConfinedException.class})
                .build();
        Registry<CircuitBreakerConfig> registry = (Registry<CircuitBreakerConfig>) confined.register(circuitBreakerConfig);
        return registry;

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
