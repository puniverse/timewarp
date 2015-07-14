package co.paralleluniverse.vtime;

/**
 * Clock instance that has a fixed epoch. By default will return a continuously-running 
 * system clock from a fixed time base.
 * 
 * @author jleskovar
 */
public final class FixedEpochClock extends Clock {
    private final long offset;
    private final Clock baseClock;
    private final long epoch;

    /**
     * Constructs a {@code FixedEpochClock} from the {@link SystemClock system clock}.
     * The {@code epoch} must be specified as milliseconds since midnight, January 1, 1970 UTC.
     * The returned {@code FixedEpochClock} will behave as if system time had started from the given {@code epoch}
     * <p>
     * Same as calling {@link #FixedEpochClock(Clock, long) FixedEpochClock(SystemClock.instance(), epoch)}.
     *
     * @param epoch the time that will be used as the epoch, in milliseconds since midnight, January 1, 1970 UTC
     */
    public FixedEpochClock(long epoch) {
        this(SystemClock.instance(), epoch);
    }

    /**
     * Constructs a {@code FixedEpochClock} from the specified {@code Clock}.
     * The {@code epoch} must be specified as milliseconds since midnight, January 1, 1970 UTC.
     * The returned {@code FixedEpochClock} will behave as if the specified {@code baseClock} had been started from
     * the given {@code epoch}.
     *
     * @param baseClock the base {@link Clock clock} whose epoch will be based from the specified epoch
     * @param epoch the time that will be used as the epoch, in milliseconds since midnight, January 1, 1970 UTC
     */
    public FixedEpochClock(Clock baseClock, long epoch) {
        this.baseClock = baseClock;
        this.epoch = epoch;
        this.offset = epoch - baseClock.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "FixedEpochClock@" + Integer.toHexString(System.identityHashCode(this)) +
                "{baseClock=" + baseClock + " epoch=" + epoch + '}';
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
        baseClock.Object_wait(obj, timeout);
    }

    @Override
    void Thread_sleep(long millis) throws InterruptedException {
        baseClock.Thread_sleep(millis);
    }

    @Override
    void Unsafe_park(sun.misc.Unsafe unsafe, boolean isAbsolute, long timeout) {
        baseClock.Unsafe_park(unsafe, isAbsolute, timeout);
    }
}
