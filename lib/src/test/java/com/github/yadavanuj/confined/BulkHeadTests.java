package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.bulkhead.BulkHead;
import com.github.yadavanuj.confined.bulkhead.BulkHeadConfig;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class BulkHeadTests {
    public static void main(String[] args) {
        BulkHeadConfig bulkHeadConfig = BulkHeadConfig.builder()
                .key("bulkhead:service1")
                .operationName("bulkhead:service1")
                .maxConcurrentCalls(1)
                .maxWaitDurationInMillis(100)
                .build();

        Confined confined = new Confined.ConfinedImpl();
        Registry<BulkHead, BulkHeadConfig> registry = confined.register(bulkHeadConfig);

        ExecutorService service = Executors.newFixedThreadPool(5);
        Supplier<String> testSupplier = () -> UUID.randomUUID().toString();

        Supplier<String> decoratedSupplier = registry.decorate("bulkhead:service1", testSupplier);
        System.out.println(decoratedSupplier.get());
    }
}
