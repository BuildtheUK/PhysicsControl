package org.btuk.pcontrol.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.EntityRules;

import javax.annotation.Nonnull;

public class ProjectileHitEventListener extends PhysicsListener {

    private final boolean supportProjectileHitEventGetHitBlock
        = this.data.hasVersion(1, 11, 0);
    private final PControlTrigger triggerBlockHitProjectilesRemoving
        = !this.supportProjectileHitEventGetHitBlock ? null
        : this.data.getTriggersRegisty().valueOf("BLOCK_HIT_PROJECTILES_REMOVING");

    private final EntityRules rulesProjectileHitEventBy = new EntityRules(
        this.data, ProjectileHitEvent.class, "projectile-by");

    public ProjectileHitEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesProjectileHitEventBy);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(ProjectileHitEvent event) {
        Entity entity = event.getEntity();

        PControlTrigger trigger = this.rulesProjectileHitEventBy.findTrigger(entity.getType());
        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
            if (event.isCancelled()) {
                entity.remove();
            }
            return;
        }

        if (!this.supportProjectileHitEventGetHitBlock) return;
        if (event.getHitBlock() == null) return; // Since 1.11

        if (!this.data.getRemovableProjectileTypes().contains(entity.getType())) return;
        if (!this.data.isActionAllowed(entity.getWorld(), this.triggerBlockHitProjectilesRemoving)) return;
        entity.remove();
    }
}
