package me.alphamode.tamago;

import com.google.common.collect.ImmutableMap;
import net.gudenau.minecraft.asm.api.v1.Identifier;
import net.gudenau.minecraft.asm.api.v1.RawTransformer;
import net.gudenau.minecraft.asm.api.v1.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.util.asm.ASM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PortingLibTransformer implements RawTransformer {
    public static final Map<String, String> REPLACEMENTS = of(
            "io/github/fabricators_of_create/porting_lib/util/FluidStack", "io/github/fabricators_of_create/porting_lib/fluids/FluidStack",
            "io/github/fabricators_of_create/porting_lib/entity/ExtraSpawnDataEntity", "io/github/fabricators_of_create/porting_lib/entity/IEntityAdditionalSpawnData",
            "io/github/fabricators_of_create/porting_lib/entity/events/living/LivingEntityLootEvents", "io/github/fabricators_of_create/porting_lib/entity/events/LivingEntityEvents",
            "io/github/fabricators_of_create/porting_lib/entity/events/living/LivingEntityEvents", "io/github/fabricators_of_create/porting_lib/entity/events/LivingEntityEvents",
            "io/github/fabricators_of_create/porting_lib/entity/events/EntityEvents$EntitySizeEvent", "me/alphamode/tamago/Tamago$EntitySizeEvent",
            "io/github/fabricators_of_create/porting_lib/entity/events/EntityEvents$Size", "me/alphamode/tamago/Tamago$Size",
            "io/github/fabricators_of_create/porting_lib/entity/events/EntityEvents$ProjectileImpact", "io/github/fabricators_of_create/porting_lib/entity/events/ProjectileImpactCallback",
            "io/github/fabricators_of_create/porting_lib/entity/events/player/PlayerTickEvents", "io/github/fabricators_of_create/porting_lib/entity/events/PlayerTickEvents",
            "io/github/fabricators_of_create/porting_lib/entity/events/player/PlayerEvents$BreakSpeed", "me/alphamode/tamago/Tamago$BreakSpeed",
            "io/github/fabricators_of_create/porting_lib/entity/events/PlayerEvents$BreakSpeed", "me/alphamode/tamago/Tamago$BreakSpeed",
            "io/github/fabricators_of_create/porting_lib/entity/events/player/PlayerEvents", "io/github/fabricators_of_create/porting_lib/entity/events/PlayerEvents",
            "io/github/fabricators_of_create/porting_lib/entity/events/LivingEntityEvents$NaturalSpawn", "me/alphamode/tamago/Tamago$NaturalSpawn",
            "io/github/fabricators_of_create/porting_lib/entity/events/EntityEvents$ShieldBlock", "me/alphamode/tamago/Tamago$ShieldBlock",
            "io/github/fabricators_of_create/porting_lib/event/BaseEvent", "io/github/fabricators_of_create/porting_lib/core/event/BaseEvent",
            "living/LivingEntityEvents$SetTarget", "MobEntitySetTargetCallback",
            "events/LivingEntityEvents$SetTarget", "events/MobEntitySetTargetCallback"
    );

    private static Map<String, String> of(String ...entries) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        for (int i = 0; i < entries.length; i = i + 2) {
            builder.put(entries[i], entries[i + 1]);
        }
        return builder.build();
    }
    public static Identifier ID = new Identifier("tamago", "porting-lib");
    @Override
    public Identifier getName() {
        return ID;
    }

    @Override
    public boolean handlesClass(String name, String transformedName) {
        return true;
    }

    @Override
    public byte[] transform(byte[] classNode, Transformer.Flags flags) {
        ClassReader reader = new ClassReader(classNode);
        ClassWriter writer = new ClassWriter(0);
        var portingLibVisitor = new PortingLibClassVisitor(writer);
        reader.accept(portingLibVisitor, 0);
//        for (var replacement : REPLACEMENTS.entrySet()) {
//
//            for (FieldNode field : classNode.fields) {
//                field.desc = field.desc.replace(replacement.getKey(), replacement.getValue());
//                if (field.signature != null)
//                    field.signature = field.signature.replace(replacement.getKey(), replacement.getValue());
//            }
//
//            for (MethodNode method : classNode.methods) {
//                if (method.signature != null)
//                    method.signature = method.signature.replace(replacement.getKey(), replacement.getValue());
//                if (method.desc.contains("util/FluidStack"))
//                    method.desc = method.desc.replace(replacement.getKey(), replacement.getValue());
//                for (AbstractInsnNode instruction : method.instructions) {
//                    if (instruction instanceof MethodInsnNode methodInsnNode) {
//                        methodInsnNode.desc = methodInsnNode.desc.replace(replacement.getKey(), replacement.getValue());
//                        methodInsnNode.owner = methodInsnNode.owner.replace(replacement.getKey(), replacement.getValue());
//                    }
//                    if (instruction instanceof FieldInsnNode fieldInsnNode) {
//                            fieldInsnNode.owner = fieldInsnNode.owner.replace(replacement.getKey(), replacement.getValue());
//                            fieldInsnNode.desc = fieldInsnNode.desc.replace(replacement.getKey(), replacement.getValue());
//
//                    }
//                    if (instruction instanceof TypeInsnNode typeInsnNode)
//                        if (typeInsnNode.desc.contains("util/FluidStack"))
//                            typeInsnNode.desc = typeInsnNode.desc.replace(replacement.getKey(), replacement.getValue());
//                }
//            }
//        }

        ClassNode node = new ClassNode();
        new ClassReader(writer.toByteArray()).accept(node, 0);
        node.methods.forEach(methodNode -> {
            methodNode.instructions.forEach(abstractInsnNode -> {
                if (abstractInsnNode instanceof FieldInsnNode fieldInsnNode) {
                    if (fieldInsnNode.name.equals("SET_TARGET")) {
                        fieldInsnNode.owner = fieldInsnNode.owner.replace("LivingEntityEvents", "MobEntitySetTargetCallback");
                        fieldInsnNode.name = "EVENT";
                    }
                }
            });
        });
        ClassWriter newWriter = new ClassWriter(ASM.API_VERSION);
        node.accept(newWriter);

        var zlazz = Path.of("classes/" + reader.getClassName() + ".class");
        zlazz.getParent().toFile().mkdirs();
        try {
            Files.write(zlazz, newWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return newWriter.toByteArray();
    }
}
