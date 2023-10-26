package me.alphamode.tamago.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.alphamode.tamago.Tamago;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // shield blocking

    @Shadow public abstract ItemStack getUseItem();

    @ModifyExpressionValue(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"
            )
    )
    private boolean fireShieldBlockEvent(boolean isBlocked,
                                         DamageSource source, float amount,
                                         @Share("ShieldBlockEvent") LocalRef<Tamago.ShieldBlockEvent> sharedEvent) {
        if (!isBlocked)
            return false;
        Tamago.ShieldBlockEvent event = new Tamago.ShieldBlockEvent((LivingEntity) (Object) this, getUseItem(), source, amount);
        sharedEvent.set(event); // save to check if the shield gets damaged later
        Tamago.SHIELD_BLOCK.invoker().onShieldBlock(event);
        return !event.isCanceled();
    }

    @WrapWithCondition(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"
            )
    )
    private boolean checkHurtShield(LivingEntity self, float shieldDamage,
                                    DamageSource source, float amount,
                                    @Share("ShieldBlockEvent") LocalRef<Tamago.ShieldBlockEvent> sharedEvent) {
        Tamago.ShieldBlockEvent event = sharedEvent.get();
        return event == null || event.damageShield;
    }

    @ModifyVariable(
            method = "hurt",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"
                    )
            ),
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 2 // floats: amount, f, g
    )
    private float modifyBlockedAmount(float originalStored,
                                      DamageSource source, float amount,
                                      @Share("ShieldBlockEvent") LocalRef<Tamago.ShieldBlockEvent> sharedEvent) {
        Tamago.ShieldBlockEvent event = sharedEvent.get();
        return event == null ? originalStored : event.damageBlocked;
    }

    @ModifyExpressionValue(
            method = "hurt",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"
                    )
            ),
            at = @At(value = "CONSTANT",args = "floatValue=0")
    )
    private float modifyDamage(float newDamage, // should be 0
                               DamageSource source, float amount,
                               @Share("ShieldBlockEvent") LocalRef<Tamago.ShieldBlockEvent> sharedEvent) {
        Tamago.ShieldBlockEvent event = sharedEvent.get();
        float blocked = event == null ? amount : event.damageBlocked;
        return amount - blocked;
    }
}
