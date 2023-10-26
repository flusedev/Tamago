package me.alphamode.tamago;

import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.*;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingEntityDamageEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.jetbrains.annotations.Nullable;

public class Tamago implements ModInitializer {
    @Override
    public void onInitialize() {
        EntityEvents.START_TRACKING_TAIL.register((tracking, player) -> AdditionalEntityTrackingEvents.AFTER_START_TRACKING.invoker().afterStartTracking(tracking, player));
        EntityEvents.TELEPORT.register(event -> {
            EntityMoveEvents.EntityTeleportEvent teleportEvent = new EntityMoveEvents.EntityTeleportEvent(event.getEntity(), event.getTargetX(), event.getTargetY(), event.getTargetZ());
            EntityMoveEvents.TELEPORT.invoker().onTeleport(teleportEvent);
            event.setCanceled(teleportEvent.isCancelled());
        });
        LivingEntityEvents.HURT.register((source, damaged, amount) -> {
            LivingEntityDamageEvents.HurtEvent hurtEvent = new LivingEntityDamageEvents.HurtEvent(damaged, source, amount);
            LivingEntityDamageEvents.HURT.invoker().onHurt(hurtEvent);
            return hurtEvent.damageAmount;
        });
        LivingEntityEvents.FALL.register(event -> {
            LivingEntityDamageEvents.FallEvent fallEvent = new LivingEntityDamageEvents.FallEvent((LivingEntity) event.getEntity(), event.getSource(), event.getDistance(), event.getDamageMultiplier());
            LivingEntityDamageEvents.FALL.invoker().onFall(fallEvent);
            event.setCanceled(fallEvent.isCanceled());
        });
        PlayerEvents.BREAK_SPEED.register(event -> {
            event.setNewSpeed(BREAK_SPEED.invoker().modifyBreakSpeed(event.getPlayer(), event.getState(), event.getPos(), event.getNewSpeed()));
        });

        SHIELD_BLOCK.register(event -> {
            var blockEvent = new io.github.fabricators_of_create.porting_lib.entity.events.ShieldBlockEvent(event.blocker, event.source, event.damageBlocked);
            blockEvent.sendEvent();
            event.setCanceled(blockEvent.isCanceled());
        });
    }

    /**
     * Fired when an entity's size changes. Allows for modification of dimensions and eye height.
     * Cancellation will stop later listeners from modifying values.
     */
    public static final Event<Size> SIZE = EventFactory.createArrayBacked(Size.class, callbacks -> event -> {
        for (Size callback : callbacks) {
            callback.modifySize(event);
        }
    });

    @FunctionalInterface
    public interface Size {
        void modifySize(EntitySizeEvent event);
    }

    public static class EntitySizeEvent extends BaseEvent {
        public final Entity entity;
        public final Pose pose;
        public final float originalEyeHeight;
        public final EntityDimensions originalDimensions;

        public float eyeHeight;
        public EntityDimensions dimensions;

        public EntitySizeEvent(Entity entity, Pose pose, float height, EntityDimensions dimensions) {
            this(entity, pose, height, height, dimensions, dimensions);
        }

        public EntitySizeEvent(Entity entity, Pose pose, float oldHeight, float newHeight, EntityDimensions oldDimensions, EntityDimensions newDimensions) {
            this.entity = entity;
            this.pose = pose;
            this.originalEyeHeight = oldHeight;
            this.eyeHeight = newHeight;
            this.originalDimensions = oldDimensions;
            this.dimensions = newDimensions;
        }

        @Override
        public void sendEvent() {
            SIZE.invoker().modifySize(this);
        }
    }

    /**
     * Fired while a player breaks a block. Modifies the result of {@link Player#getDestroySpeed(BlockState)}.
     * This event is chained; multiple listeners may modify the speed.
     */
    public static final Event<BreakSpeed> BREAK_SPEED = EventFactory.createArrayBacked(BreakSpeed.class, callbacks -> (player, state, pos, speed) -> {
        for(BreakSpeed callback : callbacks)
            speed = callback.modifyBreakSpeed(player, state, pos, speed);
        return speed;
    });

    @FunctionalInterface
    public interface BreakSpeed {
        /**
         * @return the modified break speed, or the original if unchanged
         */
        float modifyBreakSpeed(Player player, BlockState state, BlockPos pos, float speed);
    }

    /**
     * This event is fired when an attack is blocked with a shield. Cancelling it will prevent blocking.
     * Listeners have the ability to change the amount of damage blocked and determine whether the shield
     * item gets damaged or not.
     */
    public static final Event<ShieldBlock> SHIELD_BLOCK = EventFactory.createArrayBacked(ShieldBlock.class, callbacks -> event -> {
        for (ShieldBlock callback : callbacks) {
            callback.onShieldBlock(event);
        }
    });

    @FunctionalInterface
    public interface ShieldBlock {
        void onShieldBlock(ShieldBlockEvent event);
    }

    public static class ShieldBlockEvent extends BaseEvent {
        public final LivingEntity blocker;
        public final ItemStack shield;
        public final DamageSource source;

        public float damageBlocked;
        public boolean damageShield = true;

        public ShieldBlockEvent(LivingEntity blocker, ItemStack shield, DamageSource source, float damageBlocked) {
            this.blocker = blocker;
            this.shield = shield;
            this.source = source;
            this.damageBlocked = damageBlocked;
        }

        @Override
        public void sendEvent() {
            SHIELD_BLOCK.invoker().onShieldBlock(this);
        }
    }

    /**
     * Called when a mob is spawned naturally. Handled scenarios:
     * <ul>
     *     <li>Night-time mobs ({@link NaturalSpawner})</li>
     *     <li>Village and Witch Hut cats ({@link CatSpawner})</li>
     *     <li>Patrols ({@link PatrolSpawner})</li>
     *     <li>Phantoms ({@link PhantomSpawner})</li>
     *     <li>Village Sieges ({@link VillageSiege})</li>
     *     <li>Wandering Traders ({@link WanderingTraderSpawner})</li>
     * </ul>
     */
    public static final Event<NaturalSpawn> NATURAL_SPAWN = EventFactory.createArrayBacked(NaturalSpawn.class, callbacks -> (mob, x, y, z, level, spawner, type) -> {
        for(NaturalSpawn callback : callbacks) {
            TriState result = callback.canSpawnMob(mob, x, y, z, level, spawner, type);
            if (result != TriState.DEFAULT)
                return result;
        }
        return TriState.DEFAULT;
    });

    @FunctionalInterface
    public interface NaturalSpawn {
        /**
         * @param spawner the {@link CustomSpawner} that caused this spawn, or null if {@link NaturalSpawner}
         * @return {@link TriState#TRUE} to allow, {@link TriState#FALSE} to disallow, or {@link TriState#DEFAULT} otherwise
         */
        TriState canSpawnMob(Mob mob, double x, double y, double z, LevelAccessor level, @Nullable CustomSpawner spawner, MobSpawnType type);
    }
}