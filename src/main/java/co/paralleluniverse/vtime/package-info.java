/**
 * <h1>Time Warp</h1>
 * <h2>Virtual Time for the JVM</h2>
 *
 * This library lets you create virtual-time clocks and install them as the JVM's clock to help with testing.
 * A {@link Clock virtual-time clock} will modify the operation of
 * {@link System#currentTimeMillis()}, {@link System#nanoTime()}, {@link Thread#sleep(long) Thread.sleep},
 * {@link Object#wait(long)} and any other operation relying on timeouts.
 *
 * <p>
 * To use this library, its JAR must be loaded as a Java agent, <i>as well as</i> put on the bootstrap class path.
 * <p>
 * Then, use one of the provided {@link Clock clock} classes: {@link SystemClock}, {@link ScaledClock}, or {@link ManualClock}, and install
 * the clock using the {@link VirtualClock} class.
 *
 * <pre>
 * Copyright (c) 2015, Parallel Universe Software Co.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * </pre>
 */
package co.paralleluniverse.vtime;
