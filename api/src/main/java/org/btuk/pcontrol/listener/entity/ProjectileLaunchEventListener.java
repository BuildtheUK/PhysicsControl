package org.btuk.pcontrol.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import javax.annotation.Nonnull;

import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.EntityRules;

public class ProjectileLaunchEventListener extends PhysicsListener {

    private final EntityRules rulesProjectileLaunchedEvent = new EntityRules(
        this.data, ProjectileLaunchEvent.class, "projectile-launched"
    );

    public ProjectileLaunchEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesProjectileLaunchedEvent);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();
        PControlTrigger trigger = this.rulesProjectileLaunchedEvent.findTrigger(entity.getType());
        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
            if (event.isCancelled()) {
                entity.remove();
            }
        }
    }
}
