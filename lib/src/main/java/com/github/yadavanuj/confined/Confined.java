package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.commons.ConfinedConfig;
import com.github.yadavanuj.confined.commons.ConfinedException;

public interface Confined {
    Registry<? extends ConfinedConfig> register(ConfinedConfig config) throws ConfinedException;

    static Confined init(){
        return new Impl();
    }

    public class Impl implements Confined {
        private final RegistryProvider registryProvider;
        public Impl(RegistryProvider registryProvider) {
            this.registryProvider = registryProvider;
        }

        public Impl() {
            this(new RegistryProvider());
        }

        @Override
        public Registry<? extends ConfinedConfig> register(ConfinedConfig config) throws ConfinedException {
            return this.registryProvider.create(config);
        }
    }
}
