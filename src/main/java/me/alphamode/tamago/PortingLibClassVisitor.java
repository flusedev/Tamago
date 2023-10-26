package me.alphamode.tamago;

import org.objectweb.asm.*;
import org.spongepowered.asm.util.asm.ASM;

public class PortingLibClassVisitor extends ClassVisitor {



    protected PortingLibClassVisitor(ClassVisitor visitor) {
        super(ASM.API_VERSION, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            if (signature != null) {
                signature = signature.replace(replacement.getKey(), replacement.getValue());
            }
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = interfaces[i].replace(replacement.getKey(), replacement.getValue());
            }
            superName = superName.replace(replacement.getKey(), replacement.getValue());
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return super.visitModule(name, access, version);
    }

    @Override
    public void visitNestHost(String nestHost) {
        super.visitNestHost(nestHost);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            if (descriptor != null)
                descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            owner = owner.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            if (signature != null)
                signature = signature.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            if (signature != null)
                signature = signature.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            if (signature != null)
                signature = signature.replace(replacement.getKey(), replacement.getValue());
        }
        return new PortingLibMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), name, descriptor);
    }
}