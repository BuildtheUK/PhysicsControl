package org.btuk.pcontrol.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class ProjectileHitEventListener extends PhysicsListener {

    private final boolean supportProjectileHitEventGetHitBlock
        = this.data.hasVersion(1, 11, 0);
    private final PControlTrigger triggerBlockHitProjectilesRemoving
        = !this.supportProjectileHitEventGetHitBlock ? null
        : this.data.getTriggersRegisty().valueOf("BLOCK_HIT_PROJECTILES_REMOVING");

    private final PControlTrigger triggerWindChargeBlockInteractions
        = this.data.getTriggersRegisty().valueOf("WIND_CHARGE_BLOCK_INTERACTIONS");

    public ProjectileHitEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(ProjectileHitEvent event) {
        if (!this.supportProjectileHitEventGetHitBlock) return;
        if (event.getHitBlock() == null) return; // Since 1.11
        Entity entity = event.getEntity();

        if (entity.getType().name().contains("WIND_CHARGE")) {
            this.data.cancelIfDisabled(event, triggerWindChargeBlockInteractions);
            return;
        }

        if (!this.data.getRemovableProjectileTypes().contains(entity.getType())) return;
        if (!this.data.isActionAllowed(entity.getWorld(), this.triggerBlockHitProjectilesRemoving)) return;
        entity.remove();
    }
}
