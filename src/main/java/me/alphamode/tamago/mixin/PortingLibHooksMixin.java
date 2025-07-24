package me.alphamode.tamago.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PortingHooks.class)
public class PortingLibHooksMixin {
    @Inject(
            method = "onBlockBreakEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/GameType;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Z)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/fabricators_of_create/porting_lib/event/common/BlockEvents$BreakEvent;isCanceled()Z",
                    ordinal = 0
            )
    )
    private static void vein$supportArchBlockEvent(Level world, GameType gameType, ServerPlayer entityPlayer, BlockPos pos, boolean canAttackBlock,
                                                   CallbackInfoReturnable<Integer> cir,
                                                   @Local BlockState state,
                                                   @Local BlockEvents.BreakEvent event) {
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