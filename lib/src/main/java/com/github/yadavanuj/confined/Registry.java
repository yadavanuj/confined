package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.types.ConfinedConfig;
import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;
import com.github.yadavanuj.confined.types.PermitType;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Registry<C extends ConfinedConfig> {
    PermitType permitType();
    <R> ConfinedSupplier<R> decorate(String key, Supplier<R> supplier);
    <R> ConfinedSupplier<R> decorate(String key, ConfinedSupplier<R> supplier);// TODO Implement ()-> decorate()
    <I, O> ConfinedFunction<I, O> decorate(String key, Function<I, O> func);

    <I, O> ConfinedFunction<I, O> decorate(String key, ConfinedFunction<I, O> func);// TODO

    String getName();
    abstract class BaseRegistry<C extends ConfinedConfig> implements Registry<C> {
        protected abstract boolean onAcquire(String key) throws ConfinedException;

        protected abstract void onRelease(String key);

         public abstract String getName();

        protected String getPermitKey(String key) {
            Objects.requireNonNull(key);

//            final String[] parts = key.split(":");
//            if (parts.length < 2 || parts[1].isEmpty()) {
//                throw new RuntimeException("PolicyKey is required.");
//            }

            return key;
        }

        protected boolean acquire(String key) throws ConfinedException {
            String permitKey = this.getPermitKey(key);
            return this.onAcquire(permitKey);
        }

        protected void release(String key) {
            String permitKey = this.getPermitKey(key);
            this.onRelease(permitKey);
        }

        public <R> ConfinedSupplier<R> decorate(String key, Supplier<R> supplier) {
            return new ConfinedSupplier<R>() {
                @Override
                public R get() throws ConfinedException {
                    if (BaseRegistry.this.acquire(key)) {
                        R result;
                        try {
                            result = supplier.get();
                        } catch (Exception e) {
                            throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation, getName(), e);
                        } finally {
                            BaseRegistry.this.release(key);
                        }
                        return result;
                    }
                    throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit, getName());
                }
            };
        }

        public <R> ConfinedSupplier<R> decorate(String key, ConfinedSupplier<R> supplier) {
            return new ConfinedSupplier<R>() {
                @Override
                public R get() throws ConfinedException {
                    if (BaseRegistry.this.acquire(key)) {
                        R result;
                        try {
                            result = supplier.get();
                        } catch(ConfinedException e){ throw e;}
                        catch (Exception e) {
                            throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation,getName(), e);
                        } finally {
                            BaseRegistry.this.release(key);
                        }
                        return result;
                    }
                    throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
                }
            };
        }

        public <I, O> ConfinedFunction<I, O> decorate(String key, Function<I, O> func) {
            return new ConfinedFunction<I, O>() {
                @Override
                public O apply(I input) throws ConfinedException {
                    if (BaseRegistry.this.acquire(key)) {
                        O output;
                        try {
                            output = func.apply(input);
                        } catch (Exception e) {
                            throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation, getName(),e);
                        } finally {
                            BaseRegistry.this.release(key);
                        }
                        return output;
                    }
                    throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
                }
            };
        }

        public <I, O> ConfinedFunction<I, O> decorate(String key, ConfinedFunction<I, O> func) {
            return new ConfinedFunction<I, O>() {
                @Override
                public O apply(I input) throws ConfinedException {
                    if (BaseRegistry.this.acquire(key)) {
                        O output;
                        try {
                            output = func.apply(input);
                        } catch (ConfinedException e ){
                            throw e;
                        }
                        catch (Exception e) {
                            throw new ConfinedException(ConfinedErrorCode.FailureWhileExecutingOperation, e);
                        } finally {
                            BaseRegistry.this.release(key);
                        }
                        return output;
                    }
                    throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit);
                }
            };
        }
    }
}
