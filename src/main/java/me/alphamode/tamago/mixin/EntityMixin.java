package me.alphamode.tamago.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.alphamode.tamago.Tamago;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private float eyeHeight;

    @Shadow private EntityDimensions dimensions;

    @Shadow public abstract int getId();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireSizeEventOnConstructor(EntityType<?> variant, Level world, CallbackInfo ci) {
        Tamago.EntitySizeEvent event = new Tamago.EntitySizeEvent((Entity) (Object) this, Pose.STANDING, eyeHeight, dimensions);
        event.sendEvent();
        this.eyeHeight = event.eyeHeight;
        this.dimensions = event.dimensions;
    }

    @ModifyExpressionValue(
            method = "refreshDimensions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F"
            )
    )
    private float fireSizeEventOnRefresh(float newEyeHeight,
                                         @Local(ordinal = 0) EntityDimensions oldDimensions,
                                         @Local(ordinal = 0) Pose pose,
                                         @Local(ordinal = 1) EntityDimensions newDimensions) {
        Tamago.EntitySizeEvent event = new Tamago.EntitySizeEvent((Entity) (Object) this, pose, eyeHeight, newEyeHeight, oldDimensions, newDimensions);
        event.sendEvent();
        this.dimensions = event.dimensions;
        return event.eyeHeight;
    }
}
