package me.alphamode.tamago.mixin;

import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PortingHooks.class, remap = false)
public class PortingLibHooksMixin {
    @Inject(
            method = "onBlockBreakEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/fabricators_of_create/porting_lib/event/common/BlockEvents$BreakEvent;isCanceled()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void vein$supportArchBlockEvent(Level world, GameType gameType, ServerPlayer entityPlayer, BlockPos pos, CallbackInfoReturnable<Integer> cir, boolean preCancelEvent, ItemStack itemstack, BlockState state, BlockEvents.BreakEvent event) {
        if (BlockEvent.BREAK.invoker().breakBlock(world, pos, state, entityPlayer, new IntValue() {
            @Override
            public void accept(int value) {
                event.setExpToDrop(value);
            }

            @Override
            public int getAsInt() {
                return event.getExpToDrop();
            }
        }).isFalse()) {
            event.setCanceled(true);
        }
    }
}