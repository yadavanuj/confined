package com.github.yadavanuj.confined.tests;

import com.github.yadavanuj.confined.ConfinedFunction;
import com.github.yadavanuj.confined.ConfinedSupplier;
import com.github.yadavanuj.confined.Registry;
import com.github.yadavanuj.confined.types.ConfinedConfig;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class RegistryTest {

    @Test
    void testDecorateSupplier_Success() throws ConfinedException {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();

        Supplier<Integer> supplier = () -> 42;

        // When
        ConfinedSupplier<Integer> confinedSupplier = registry.decorate(key, supplier);

        // Then
        assertNotNull(confinedSupplier);
        assertEquals(42, confinedSupplier.get());
    }

    @Test
    void testDecorateSupplier_Failure() {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Supplier<Integer> supplier = () -> {
            throw new RuntimeException("Test Exception");
        };

        // When
        ConfinedSupplier<Integer> confinedSupplier = registry.decorate(key, supplier);

        // Then
        assertThrows(ConfinedException.class, confinedSupplier::get);
    }

    @Test
    void testNestedDecorateSupplier_Success() throws ConfinedException {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();

        Supplier<Integer> supplier = () -> 42;

        // When
        ConfinedSupplier<Integer> confinedSupplier = registry.decorate(key,registry.decorate(key, supplier));

        // Then
        assertNotNull(confinedSupplier);
        assertEquals(42, confinedSupplier.get());
    }

    @Test
    void testNestedDecorateSupplier_Failure() {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Supplier<Integer> supplier = () -> {
            throw new RuntimeException("Test Exception");
        };

        // When
        ConfinedSupplier<Integer> confinedSupplier = registry.decorate(key,registry.decorate(key, supplier));

        // Then
        assertThrows(ConfinedException.class, confinedSupplier::get);
    }

    @Test
    void testDecorateFunction_Success() throws ConfinedException {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Function<String, Integer> function = Integer::parseInt;

        // When
        ConfinedFunction<String, Integer> confinedFunction = registry.decorate(key, function);

        // Then
        assertNotNull(confinedFunction);
        assertEquals(42, confinedFunction.apply("42"));
    }

    @Test
    void testDecorateFunction_Failure() {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Function<String, Integer> function = s -> {
            throw new RuntimeException("Test Exception");
        };

        // When
        ConfinedFunction<String, Integer> confinedFunction = registry.decorate(key, function);

        // Then
        assertThrows(ConfinedException.class, () -> confinedFunction.apply("42"));
    }


    @Test
    void testNestedDecorateFunction_Success() throws ConfinedException {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Function<String, Integer> function = Integer::parseInt;

        // When
        ConfinedFunction<String, Integer> confinedFunction = registry.decorate(key, registry.decorate(key, function));

        // Then
        assertNotNull(confinedFunction);
        assertEquals(42, confinedFunction.apply("42"));
    }

    @Test
    void testNestedDecorateFunction_Failure() {
        // Given
        String key = "test-key";
        Registry.BaseRegistry<ConfinedConfigMock> registry = new ConfinedRegistryMock();
        Function<String, Integer> function = s -> {
            throw new RuntimeException("Test Exception");
        };

        // When
        ConfinedFunction<String, Integer> confinedFunction = registry.decorate(key, registry.decorate(key, function));

        // Then
        assertThrows(ConfinedException.class, () -> confinedFunction.apply("42"));
    }

    static class ConfinedConfigMock extends ConfinedConfig {
        protected ConfinedConfigMock(ConfinedConfigBuilder<?, ?> b) {
            super(b);
        }
        // Mock implementation of ConfinedConfig
    }

    static class ConfinedRegistryMock extends Registry.BaseRegistry<ConfinedConfigMock> {
        @Override
        protected boolean onAcquire(String key) throws ConfinedException {
            return true;
        }

        @Override
        protected void onRelease(String key) {
            // No action needed for test
        }

        @Override
        public PermitType permitType() {
            return null;
        }

        @Override
        public String getName() {
            return "Mock Registry";
        }
    }
}
