package co.paralleluniverse.vtime;

/**
 * Continuously-running system clock with a fixed epoch.
 * Provides real-time from a fixed time base
 * 
 * @author jleskovar
 */
public final class SystemClockFromEpoch extends Clock {
    private final long offset;

    public SystemClockFromEpoch(long epoch) {
        this.offset = epoch - SystemClock.instance().currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SystemClockFromEpoch";
    }

    @Override
    long System_currentTimeMillis() {
        return System.currentTimeMillis() + offset;
    }

    @Override
    long System_nanoTime() {
        return System.nanoTime() + (offset * 1_000_000);
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
