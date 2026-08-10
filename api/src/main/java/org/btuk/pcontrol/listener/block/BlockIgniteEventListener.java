package org.btuk.pcontrol.listener.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class BlockIgniteEventListener extends PhysicsListener {

    private final PControlTrigger triggerPlayersFlintUsage
        = this.data.getTriggersRegisty().valueOf("PLAYERS_FLINT_USAGE");
    private final PControlTrigger triggerFireSpreading
        = this.data.getTriggersRegisty().valueOf("FIRE_SPREADING");

    public BlockIgniteEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            this.data.cancelIfDisabled(event, this.triggerPlayersFlintUsage);
        } else {
            this.data.cancelIfDisabled(event, this.triggerFireSpreading);
        }
    }
}
