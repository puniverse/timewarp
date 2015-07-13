package co.paralleluniverse.vtime;

/**
 * Continuously-running system clock with a fixed epoch.
 * Provides real-time from a fixed time base
 * 
 * @author jleskovar
 */
public final class FixedEpochClock extends Clock {
    private final long offset;
    private final Clock baseClock;

    public FixedEpochClock(long epoch) {
        this(SystemClock.instance(), epoch);
    }

    public FixedEpochClock(Clock baseClock, long epoch) {
        this.baseClock = baseClock;
        this.offset = epoch - baseClock.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "FixedEpochClock";
    }

    @Override
    long System_currentTimeMillis() {
        return baseClock.currentTimeMillis() + offset;
    }

    @Override
    long System_nanoTime() {
        return baseClock.nanoTime() + (offset * 1_000_000);
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
