/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.paralleluniverse.vtime;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A clock that only progresses when its time is manually advanced by calls to {@link #advance(long, TimeUnit) advance}.
 *
 * @author pron
 */
public final class ManualClock extends Clock {
    private final Queue<Scheduled> waiters = new ConcurrentSkipListPriorityQueue<>();
    private final long startTime;
    private volatile long nanos;

    /**
     * Creates a new {@code ManualClock} instance.
     *
     * @param startTime the initial time which will be returned by {@code System.currentTimeMillis()}.
     */
    public ManualClock(long startTime) {
        if (startTime < 0)
            throw new IllegalArgumentException("startTime must be >= 0; was " + startTime);
        this.startTime = startTime;
        this.nanos = 0;
    }

    /**
     * Creates a new {@code ManualClock} instance.
     * <p>
     * Same as {@link #ManualClock(long) ManualClock(System.currentTimeMillis()}.
     */
    public ManualClock() {
        this(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "ManualClock@" + Integer.toHexString(System.identityHashCode(this)) + "{startTime=" + startTime + " nanos=" + nanos + '}';
    }

    /**
     * Advances this clock's time by the given duration.
     * 
     * @param duration the time duration
     * @param unit     the time duration's unit
     */
    public synchronized void advance(long duration, TimeUnit unit) {
        if (duration <= 0)
            throw new IllegalArgumentException("Duration must be positive; was " + duration);

        this.nanos += unit.toNanos(duration);

        for (;;) {
            Scheduled s = waiters.peek();
            if (s == null || s.deadline < nanos)
                break;
            // at this point we know there are runnable waiters
            // new ones won't be added because we've already advanced nanos
            waiters.poll().wakeup();
        }
    }

    @Override
    long System_currentTimeMillis() {
        return startTime + TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    @Override
    long System_nanoTime() {
        return nanos;
    }

    @Override
    void Object_wait(Object obj, long timeout) throws InterruptedException {
        if (timeout <= 0)
            obj.wait(timeout);
        else {
            final long deadline = nanos + TimeUnit.MILLISECONDS.toNanos(timeout);
            try {
                InterruptScheduled s = interrupt(deadline, Thread.currentThread());
                waiters.add(s);
                obj.wait();

                synchronized (this) {
                    if (deadline < nanos)
                        s.disable();
                    else // advance was called between obj.wait and this synchronized block
                        Thread.interrupted();
                }
            } catch (InterruptedException e) {
                handleInterrupted(deadline, e);
            }
        }
    }

    @Override
    void Thread_sleep(long millis) throws InterruptedException {
        if (millis <= 0)
            Thread.sleep(millis);
        else {
            final long deadline = nanos + TimeUnit.MILLISECONDS.toNanos(millis);
            try {
                waiters.add(interrupt(deadline, Thread.currentThread()));
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                handleInterrupted(deadline, e);
            }
        }
    }

    @Override
    void Unsafe_park(boolean isDeadline, long timeout) {
        if (timeout <= 0)
            park(isDeadline, nanos);
        else {
            final long deadline = nanos + (isDeadline ? TimeUnit.MILLISECONDS.toNanos(timeout - System_currentTimeMillis()) : timeout);
            waiters.add(unpark(deadline, Thread.currentThread()));
            if (nanos < deadline)
                park(false, 0L);
        }
    }

    private void handleInterrupted(long deadline, InterruptedException e) throws InterruptedException {
        if (nanos < deadline)
            throw e;
        Thread.interrupted();
    }

    private abstract static class Scheduled implements Comparable<Scheduled> {
        final long deadline;
        final Thread thread;

        public Scheduled(long deadline, Thread thread) {
            this.deadline = deadline;
            this.thread = thread;
        }

        @Override
        public int compareTo(Scheduled o) {
            return signum(deadline - o.deadline);
        }

        public abstract void wakeup();
    }

    static int signum(long x) {
        long y = (x & 0x7fffffffffffffffL) + 0x7fffffffffffffffL;
        return (int) ((x >> 63) | (y >>> 63));
    }

    private Scheduled unpark(long deadline, Thread t) {
        return new Scheduled(deadline, t) {
            @Override
            public void wakeup() {
                LockSupport.unpark(thread);
            }
        };
    }

    private InterruptScheduled interrupt(long deadline, Thread t) {
        return new InterruptScheduled(deadline, t);
    }

    private static class InterruptScheduled extends Scheduled {
        private volatile boolean disabled;

        public InterruptScheduled(long deadline, Thread thread) {
            super(deadline, thread);
        }

        public void disable() {
            disabled = true;
        }

        @Override
        public void wakeup() {
            if (!disabled)
                thread.interrupt();
        }
    }
}
