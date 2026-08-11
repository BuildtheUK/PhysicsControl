package org.btuk.pcontrol.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.EntityRules;

import javax.annotation.Nonnull;

public class EntityExplodeEventListener extends PhysicsListener {

    private final EntityRules rulesEntityExplodeEventBy = new EntityRules(
        this.data, EntityExplodeEvent.class, "exploding-by");

    public EntityExplodeEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesEntityExplodeEventBy);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;

        PControlTrigger trigger = this.rulesEntityExplodeEventBy.findTrigger(entity.getType());
        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
        }
    }
}
