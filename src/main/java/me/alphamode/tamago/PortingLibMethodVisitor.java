package me.alphamode.tamago;

import org.objectweb.asm.*;
import org.spongepowered.asm.util.asm.ASM;

public class PortingLibMethodVisitor extends MethodVisitor {
    private final String name, desc;
    protected PortingLibMethodVisitor(MethodVisitor methodVisitor, String name, String desc) {
        super(ASM.API_VERSION, methodVisitor);
        this.name = name;
        this.desc = desc;
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            owner = owner.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitMethodInsn(opcode, owner, name, descriptor);
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
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (name.equals("BREAK_SPEED") && owner.contains("player/PlayerEvents")) {
            owner = "me/alphamode/tamago/Tamago";
        }
        if (name.equals("SHIELD_BLOCK") && owner.contains("events/EntityEvents")) {
            owner = "me/alphamode/tamago/Tamago";
        }
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            owner = owner.replace(replacement.getKey(), replacement.getValue());
        }
        if (owner.equals("io/github/fabricators_of_create/porting_lib/entity/events/EntityEvents") && name.equals("SIZE"))
            owner = "me/alphamode/tamago/Tamago";
        if (name.equals("PROJECTILE_IMPACT")) {
            owner = "io/github/fabricators_of_create/porting_lib/entity/events/ProjectileImpactCallback";
            name = "EVENT";
        }
        if (name.equals("NATURAL_SPAWN"))
            owner = "me/alphamode/tamago/Tamago";
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            owner = owner.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        String newDescriptor = descriptor;
        if (name.equals("onPlayerTickEnd") && descriptor.contains("PlayerTickEvents$End"))
            name = "onEndOfPlayerTick";
        Handle newBootstrapMethodHandle = bootstrapMethodHandle;
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            newDescriptor = newDescriptor.replace(replacement.getKey(), replacement.getValue());
            Object[] newArgs = new Object[bootstrapMethodArguments.length];
            for (int i = 0; i < bootstrapMethodArguments.length; i++) {
                if (bootstrapMethodArguments[i] instanceof Type type)
                    newArgs[i] = Type.getType(type.getDescriptor().replace(replacement.getKey(), replacement.getValue()));
                else if (bootstrapMethodArguments[i] instanceof Handle handle)
                    newArgs[i] = new Handle(handle.getTag(), handle.getOwner().replace(replacement.getKey(), replacement.getValue()), handle.getName(), handle.getDesc().replace(replacement.getKey(), replacement.getValue()), handle.isInterface());
                else
                    newArgs[i] = bootstrapMethodArguments[i];
            }
            bootstrapMethodArguments = newArgs;
            newBootstrapMethodHandle = new Handle(newBootstrapMethodHandle.getTag(), newBootstrapMethodHandle.getOwner().replace(replacement.getKey(), replacement.getValue()), newBootstrapMethodHandle.getName(), newBootstrapMethodHandle.getDesc().replace(replacement.getKey(), replacement.getValue()), newBootstrapMethodHandle.isInterface());
        }
        super.visitInvokeDynamicInsn(name, newDescriptor, newBootstrapMethodHandle, bootstrapMethodArguments);
    }



    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
            if (signature != null)
                signature = signature.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            descriptor = descriptor.replace(replacement.getKey(), replacement.getValue());
        }
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            type = type.replace(replacement.getKey(), replacement.getValue());
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        Object[] newLocal = new Object[local.length];
        Object[] newStack = new Object[stack.length];
        for (var replacement : PortingLibTransformer.REPLACEMENTS.entrySet()) {
            for (int i = 0; i < local.length; i++) {
                if (local[i] instanceof String str) {
                    local[i] = str.replace(replacement.getKey(), replacement.getValue());
                }
        }
            for (int i = 0; i < stack.length; i++) {
                if (stack[i] instanceof String str)
                    stack[i] = str.replace(replacement.getKey(), replacement.getValue());
            }
        }

        super.visitFrame(type, numLocal, local, numStack, stack);
    }
}