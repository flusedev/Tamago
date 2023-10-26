package me.alphamode.tamago;

import net.gudenau.minecraft.asm.api.v1.AsmInitializer;
import net.gudenau.minecraft.asm.api.v1.AsmRegistry;

public class TamagoASM implements AsmInitializer {
    @Override
    public void onInitializeAsm() {
        AsmRegistry.getInstance().registerRawTransformer(new PortingLibTransformer());
    }
}
