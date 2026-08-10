package org.btuk.pcontrol.listener.block;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.pair.MaterialMaterialRules;
import org.btuk.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class EntityBlockFormEventListener extends PhysicsListener {

    private final MaterialMaterialRules rulesEntityBlockFormEventFromTo = new MaterialMaterialRules(
        this.data, EntityBlockFormEvent.class, "from", "to");
    private final MaterialRules rulesEntityBlockFormEventTo = new MaterialRules(
        this.data, EntityBlockFormEvent.class, "to");

    public EntityBlockFormEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesEntityBlockFormEventFromTo);
        parser.registerParser(this.rulesEntityBlockFormEventTo);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(EntityBlockFormEvent event) {
        Material from = event.getBlock().getType();
        Material to = event.getNewState().getType();

        PControlTrigger trigger = this.rulesEntityBlockFormEventFromTo.findTrigger(from, to);
        if (trigger == null) trigger = this.rulesEntityBlockFormEventTo.findTrigger(to);

        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
        } else {
            this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to);
        }
    }
}
