/*
 * Copyright (c) 2015, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

/**
 * The system clock.
 * This clock provides "real" time, as perceived by this running JVM.
 * 
 * @author pron
 */
public final class SystemClock extends Clock {
    private static final SystemClock INSTANCE = new SystemClock();

    public static Clock instance() {
        return INSTANCE;
    }

    private SystemClock() {
    }

    @Override
    public String toString() {
        return "SystemClock";
    }

    @Override
    long System_currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    long System_nanoTime() {
        return System.nanoTime();
    }

    @Override
    void Object_wait(Object obj, long timeout) throws InterruptedException {
        obj.wait(timeout);
    }

    @Override
    void Thread_sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Override
    void Unsafe_park(sun.misc.Unsafe unsafe, boolean isAbsolute, long timeout) {
        park(unsafe, isAbsolute, timeout);
    }
}
