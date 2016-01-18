/*
 * Copyright (c) 2015-2016, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

import java.lang.instrument.Instrumentation;

public final class JavaAgent {

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.err.println("NOTE: VIRTUAL TIME IN EFFECT");
        instrumentation.addTransformer(new VirtualTimeClassTransformer());
        
        if (agentArguments != null && !agentArguments.isEmpty()) {
            final double scale = 1.0 / Integer.parseInt(agentArguments);
            System.err.println("SCALING CLOCK by " + scale);
            VirtualClock.setGlobal(new ScaledClock(scale));
        }
    }

    private JavaAgent() {
    }
}
