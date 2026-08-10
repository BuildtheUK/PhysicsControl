package org.btuk.pcontrol.listener.block;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.pair.MaterialMaterialRules;
import org.btuk.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class BlockGrowEventListener extends PhysicsListener {

    private final MaterialMaterialRules rulesBlockGrowEventFromTo = new MaterialMaterialRules(
        this.data, BlockGrowEvent.class, "from", "to");
    private final MaterialRules rulesBlockGrowEventTo = new MaterialRules(
        this.data, BlockGrowEvent.class, "to");

    public BlockGrowEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesBlockGrowEventFromTo);
        parser.registerParser(this.rulesBlockGrowEventTo);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(BlockGrowEvent event) {
        Material from = event.getBlock().getType();
        Material to = event.getNewState().getType();

        PControlTrigger trigger = this.rulesBlockGrowEventFromTo.findTrigger(from, to);
        if (trigger == null) trigger = this.rulesBlockGrowEventTo.findTrigger(to);

        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
        } else {
            this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to);
        }
    }
}
