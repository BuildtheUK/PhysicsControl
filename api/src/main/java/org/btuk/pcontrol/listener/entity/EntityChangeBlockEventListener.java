package org.btuk.pcontrol.listener.entity;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.pair.EntityMaterialRules;
import org.btuk.pcontrol.rules.pair.MaterialMaterialRules;
import org.btuk.pcontrol.rules.single.EntityRules;
import org.btuk.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class EntityChangeBlockEventListener extends PhysicsListener {

    private final MaterialMaterialRules rulesEntityChangeBlockEventFromTo = new MaterialMaterialRules(
        this.data, EntityChangeBlockEvent.class, "from", "to");
    private final MaterialRules rulesEntityChangeBlockEventTo = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "to");
    private final MaterialRules rulesFallingEntityChangeBlockEventBy = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "falling-by");
    private final MaterialRules rulesFallingEntityChangeBlockEventFrom = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "falling-from");
    private final EntityMaterialRules rulesNonFallingEntityChangeBlockEventByFrom = new EntityMaterialRules(
        this.data, EntityChangeBlockEvent.class, "non-falling-by", "non-falling-from");
    private final EntityRules rulesNonFallingEntityChangeBlockEventBy = new EntityRules(
        this.data, EntityChangeBlockEvent.class, "non-falling-by");

    public EntityChangeBlockEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesEntityChangeBlockEventFromTo);
        parser.registerParser(this.rulesEntityChangeBlockEventTo);
        parser.registerParser(this.rulesFallingEntityChangeBlockEventBy);
        parser.registerParser(this.rulesFallingEntityChangeBlockEventFrom);
        parser.registerParser(this.rulesNonFallingEntityChangeBlockEventByFrom);
        parser.registerParser(this.rulesNonFallingEntityChangeBlockEventBy);
    }

    // Prevent client bug with disappearing blocks on start falling (fixed on paper 1.16.5, spigot 1.19.4 and client 1.18.2)
    private final boolean fixBlocksGravity = !this.data.hasVersion(1, 19, 4);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void on(EntityChangeBlockEvent event) {
        Material from = event.getBlock().getType();
        Material to = event.getTo();

        boolean updateBlockOnCancel = false;

        PControlTrigger trigger = this.rulesEntityChangeBlockEventFromTo.findTrigger(from, to);
        if (trigger == null) trigger = this.rulesEntityChangeBlockEventTo.findTrigger(to);

        if (trigger == null) {
            if (event.getEntity() instanceof FallingBlock) {
                Material by = this.versionsAdapter.getFallingBlockMaterial((FallingBlock) event.getEntity());

                trigger = this.rulesFallingEntityChangeBlockEventBy.findTrigger(from);
                if (trigger == null) trigger = this.rulesFallingEntityChangeBlockEventFrom.findTrigger(by);

                if (trigger == null) {
                    this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to + " (by falling " + by + ")");
                    return;
                }
                if (this.fixBlocksGravity) {
                    updateBlockOnCancel = true;
                }
            } else {
                EntityType by = event.getEntity().getType();

                trigger = this.rulesNonFallingEntityChangeBlockEventByFrom.findTrigger(by, from);
                if (trigger == null) trigger = this.rulesNonFallingEntityChangeBlockEventBy.findTrigger(by);
                // TODO It is necessary to implement a smart system of destruction and restoration
                //  of water lilies so that there are no problems with movement

                if (trigger == null) {
                    this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to + " (by " + by + ")");
                }
            }
        }

        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
        }

        if (event.isCancelled() && updateBlockOnCancel) {
            event.getBlock().getState().update(false, false);
        }
    }
}
