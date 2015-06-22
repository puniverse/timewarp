package co.paralleluniverse.vtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.Type;

public final class JavaAgent {
    private static final String PACKAGE = Clock_.class.getPackage().getName().replace('.', '/');
    private static final String CLOCK = Type.getInternalName(Clock_.class);

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.err.println("NOTE: VIRTUAL TIME IN EFFECT");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    if (className.startsWith(PACKAGE))
                        return null;
                    return instrumentClass(classfileBuffer);
                } catch (Throwable t) {
                    System.err.println("WARNING: Virtual time instrumentation failed for class " + Type.getType(classBeingRedefined).getClassName() + ": " + t);
                    throw t; // same effect as returning null
                }
            }
        });
    }

    private static byte[] instrumentClass(byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);

        ClassVisitor cv = new ClassVisitor(ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (!captureTimeCall(owner, name, desc))
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }

                    private boolean captureTimeCall(String owner, String name, String desc) {
                        switch (owner) {
                            case "java/lang/Object":
                                if ("wait".equals(name) && !desc.startsWith("()"))
                                    return callClockMethod("Object_wait", "(Ljava/lang/Object;" + desc.substring(1));
                                break;
                            case "java/lang/System":
                                switch (name) {
                                    case "nanoTime":
                                        return callClockMethod("System_nanoTime", desc);
                                    case "currentTimeMillis":
                                        return callClockMethod("System_currentTimeMillis", desc);
                                }
                                break;
                            case "java/lang/Thread":
                                if ("sleep".equals(name))
                                    return callClockMethod("Thread_sleep", desc);
                                break;
                            case "sun/misc/Unafe":
                                if ("park".equals(name))
                                    return callClockMethod("Unsafe_park", desc);
                                break;
                        }
                        return false;
                    }

                    private boolean callClockMethod(String name, String desc) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, CLOCK, name, desc, false);
                        return true;
                    }
                };
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private JavaAgent() {
    }
}
