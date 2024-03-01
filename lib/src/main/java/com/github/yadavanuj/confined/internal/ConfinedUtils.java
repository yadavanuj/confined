package com.github.yadavanuj.confined.internal;

import com.github.yadavanuj.confined.types.ConfinedErrorCode;
import com.github.yadavanuj.confined.types.ConfinedException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ConfinedUtils {
    public static boolean acquirePermitExceptionally(Semaphore semaphore, long waitDurationInMillis) throws ConfinedException {
        return acquirePermitExceptionally(semaphore,waitDurationInMillis,null);
    }

        public static boolean acquirePermitExceptionally(Semaphore semaphore, long waitDurationInMillis, String name) throws ConfinedException {
        boolean result;
        try {
            result = semaphore.tryAcquire(waitDurationInMillis, TimeUnit.MILLISECONDS);
            if (!result) {
                throw new ConfinedException(ConfinedErrorCode.FailedToAcquirePermit,name);
            }
        } catch (InterruptedException e) {
            // TODO: Handle
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw new ConfinedException(ConfinedErrorCode.InterruptedWhileAcquiringPermit, name);
        }
        return true;
    }

    public static void sleepUninterruptedly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // TODO: Check
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static <R> Supplier<R> sleepingSupplier(Supplier<R> supplier, long sleepDurationInMillis) {
        return () -> {
            sleepUninterruptedly(sleepDurationInMillis);
            return supplier.get();
        };
    }
}
