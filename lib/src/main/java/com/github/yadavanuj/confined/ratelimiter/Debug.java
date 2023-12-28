package com.github.yadavanuj.confined.ratelimiter;

public class Debug {
    public static String SemaphoreAcquired = "Semaphore acquired. Cycle: %d";
    public static String StartingSlice = "Starting Slice. Acquired. Cycle: %d , Active Permissions: %s";
    public static String EndingSlice = "Ending Slice. Acquired. Cycle: %d , Active Permissions: %s";
    public static String AcquiringWithinWindow = "Acquired. Cycle: %d , Active Permissions: %s";
    public static String GoingToSleep = "Sleeping. Cycle: %d";

    public static void log(boolean isDebug, String message, int cycle) {
        if (isDebug) {
            System.out.printf((message) + "%n", cycle);
        }
    }

    public static void log(boolean isDebug, String message, int cycle, Long permissions) {
        if (isDebug) {
            System.out.printf((message) + "%n", cycle, permissions);
        }
    }
}
