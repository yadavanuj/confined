package com.github.yadavanuj.confined.commons;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ConfinedLogger {
    void log(String message);
    void log(Supplier<Boolean> condition, String message);

    static ConfinedLogger from(ConfinedLoggerConfig config) {
        return new Implementation(config);
    }

    @Builder
    @Getter
    public class ConfinedLoggerConfig {
        @Builder.Default
        private boolean debug = false;
        @Builder.Default
        @NonNull
        private Consumer<String> loggingConsumer = System.out::println;
    }

    public class Implementation implements ConfinedLogger {
        private final ConfinedLoggerConfig config;

        public Implementation(ConfinedLoggerConfig config) {
            this.config = config;
        }

        @Override
        public void log(String message) {
            if (config.isDebug()) {
                config.getLoggingConsumer().accept(message);
            }
        }

        @Override
        public void log(Supplier<Boolean> condition, String message) {
            if (condition.get()) {
                config.getLoggingConsumer().accept(message);
            }
        }
    }
}
