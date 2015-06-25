package co.paralleluniverse.vtime;

/**
 * A clock providing scaled time (slowed down or sped up) relative to another clock.
 * @author pron
 */
public final class ScaledClock extends Clock {
    private final Clock source;
    private final double scale;

    private final long startTime;
    private final long startNanos;

    /**
     * Constructs a {@code ScaledClock} of the a given clock.
     * A scale {@literal >} 1 would make this clock run faster relative to the given clock;
     * a scale {@literal <} 1 would make this clock run slower relative to the given clock.
     *
     * @param scale the scale by which the given clock's time is scaled; must be positive.
     */
    public ScaledClock(Clock source, double scale) {
        if (scale <= 0.0)
            throw new IllegalArgumentException("Scale must be positive; was " + scale);
        this.source = source;
        this.scale = scale;

        this.startTime = source.currentTimeMillis();
        this.startNanos = source.nanoTime();
    }

    /**
     * Constructs a {@code ScaledClock} of the {@link SystemClock system clock}.
     * A scale {@literal >} 1 would make this clock run faster relative to the system clock;
     * a scale {@literal <} 1 would make this clock run slower relative to the system clock.
     * <p>
     * Same as calling {@link #ScaledClock(Clock, double) ScaledClock(SystemClock.instance(), scale)}.
     * 
     * @param scale the scale by which the system clock's time is scaled; must be positive.
     */
    public ScaledClock(double scale) {
        this(SystemClock.instance(), scale);
    }

    @Override
    public String toString() {
        return "ScaledClock@" + Integer.toHexString(System.identityHashCode(this)) + "{source=" + source + " scale=" + scale + '}';
    }

    @Override
    long System_currentTimeMillis() {
        return startTime + (long) ((source.System_currentTimeMillis() - startTime) * scale);
    }

    @Override
    long System_nanoTime() {
        return startNanos + (long) ((source.System_nanoTime() - startNanos) * scale); // we use startNanos just to keep the scaled number smaller
    }

    @Override
    void Object_wait(Object obj, long timeout) throws InterruptedException {
        source.Object_wait(obj, (long) (timeout / scale));
    }

    @Override
    void Thread_sleep(long millis) throws InterruptedException {
        source.Thread_sleep((long) (millis / scale));
    }

    @Override
    void Unsafe_park(boolean isDeadline, long timeout) {
        if (!isDeadline)
            source.Unsafe_park(isDeadline, (long) (timeout / scale));
        else
            source.Unsafe_park(isDeadline, source.currentTimeMillis() + (long) ((timeout - currentTimeMillis()) / scale));
    }
}
