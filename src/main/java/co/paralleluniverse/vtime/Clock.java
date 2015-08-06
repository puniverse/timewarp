/*
 * Copyright (c) 2015, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

/**
 * Encapsulates the behavior of all JDK time-related operations.
 *
 * Installing a clock via the {@link VirtualClock} class, will modify the operation of
 * {@link System#currentTimeMillis()}, {@link System#nanoTime()}, {@link Thread#sleep(long) Thread.sleep},
 * {@link Object#wait(long)} and any other operation relying on timeouts.
 *
 * @author pron
 */
public abstract class Clock {
    // for now, allow implementations in this package only
    Clock() {
    }

    /**
     * Returns this clock's current time in milliseconds.
     *
     * @return the difference, measured in milliseconds, between
     *         this clock's current time and midnight, January 1, 1970 UTC.
     */
    public final long currentTimeMillis() {
        return System_currentTimeMillis();
    }

    /**
     * Returns the current value of this clock's
     * high-resolution time source, in nanoseconds.
     *
     * <p>
     * This method can only be used to measure elapsed time and is
     * not related to any other notion of system or wall-clock time.
     * The value returned represents nanoseconds since some fixed but
     * arbitrary <i>origin</i> time.
     *
     * <p>
     * The values returned by this method become meaningful only when
     * the difference between two such values, obtained from the same clock,
     * is computed.
     *
     * @return the current value of this clock's
     *         high-resolution time source, in nanoseconds
     * @see System#nanoTime()
     */
    public final long nanoTime() {
        return System_nanoTime();
    }

    abstract long System_currentTimeMillis();

    abstract long System_nanoTime();

    abstract void Object_wait(Object obj, long timeout) throws InterruptedException;

    void Object_wait(Object obj, long timeout, int nanos) throws InterruptedException {
        Object_wait(obj, toMillis(timeout, nanos));
    }

    abstract void Thread_sleep(long millis) throws InterruptedException;

    void Thread_sleep(long millis, int nanos) throws InterruptedException {
        Thread_sleep(toMillis(millis, nanos));
    }

    abstract void Unsafe_park(sun.misc.Unsafe unsafe, boolean isAbsolute, long timeout);

    private static long toMillis(long millis, int nanos) {
        if (millis < 0)
            throw new IllegalArgumentException("timeout value is negative");
        if (nanos < 0 || nanos > 999999)
            throw new IllegalArgumentException("nanosecond timeout value out of range");
        if (nanos >= 500000 || (nanos != 0 && millis == 0))
            millis++;
        return millis;
    }

    /**
     * Calls the actual {@code UNSAFE.park(isDeadline, timeout)}.
     */
    static void park(sun.misc.Unsafe unsafe, boolean isAbsolute, long timeout) {
        unsafe.park(isAbsolute, timeout);
//        if (isDeadline)
//            LockSupport.parkUntil(timeout);
//        else if (timeout > 0)
//            LockSupport.parkNanos(timeout);
//        else
//            LockSupport.park();
    }
}
