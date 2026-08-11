package org.btuk.pcontrol.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.EntityRules;

import javax.annotation.Nonnull;

public class ExplosionPrimeEventListener extends PhysicsListener {

    private final EntityRules rulesExplosionPrimeEventBy = new EntityRules(
        this.data, ExplosionPrimeEvent.class, "exploding-by");

    public ExplosionPrimeEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesExplosionPrimeEventBy);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;

        PControlTrigger trigger = this.rulesExplosionPrimeEventBy.findTrigger(entity.getType());
        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
            if (event.isCancelled()) {
                entity.remove();
            }
        }
    }
}
