package co.paralleluniverse.vtime;

import java.lang.instrument.Instrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class JavaAgent {
    private static final String PACKAGE = Clock_.class.getPackage().getName().replace('.', '/');
    private static final String CLOCK = Type.getInternalName(Clock_.class);

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.err.println("NOTE: VIRTUAL TIME IN EFFECT");
        instrumentation.addTransformer(new ASMClassFileTransformer() {

            @Override
            protected boolean filter(String className) {
                return className.startsWith(PACKAGE);
            }

            @Override
            protected ClassVisitor createVisitor(ClassVisitor next) {
                return new ClassVisitor(Opcodes.ASM5, next) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
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
            }
        });
    }

    private JavaAgent() {
    }
}
