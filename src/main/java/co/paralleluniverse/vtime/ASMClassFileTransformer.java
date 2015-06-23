package co.paralleluniverse.vtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

abstract class ASMClassFileTransformer implements ClassFileTransformer {
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (filter(className))
                return null;
            return instrumentClass(classfileBuffer);
        } catch (Throwable t) {
            System.err.println("WARNING: Instrumentation by " + getClass().getName() + " failed for class " + Type.getType(classBeingRedefined).getClassName() + ": " + t);
            throw t; // same effect as returning null
        }
    }
    
    protected abstract boolean filter(String className);
    
    protected abstract ClassVisitor createVisitor(ClassVisitor next);
    
    protected byte[] instrumentClass(byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = createVisitor(cw);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
