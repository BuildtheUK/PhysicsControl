package org.btuk.pcontrol.listener.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class BlockBurnEventListener extends PhysicsListener {

    private final PControlTrigger triggerFireSpreading
        = this.data.getTriggersRegisty().valueOf("FIRE_SPREADING");

    public BlockBurnEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(BlockBurnEvent event) {
        this.data.cancelIfDisabled(event, this.triggerFireSpreading);
    }
}
