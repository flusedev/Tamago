package io.github.fabricators_of_create.porting_lib.entity.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class AdditionalEntityTrackingEvents {
    public static final Event<AfterStartTracking> AFTER_START_TRACKING = EventFactory.createArrayBacked(AfterStartTracking.class, callbacks -> (entity, player) -> {
        for (AfterStartTracking e : callbacks)
            e.afterStartTracking(entity, player);
    });

    @FunctionalInterface
    public interface AfterStartTracking {
        void afterStartTracking(Entity entity, ServerPlayer player);
    }
}
