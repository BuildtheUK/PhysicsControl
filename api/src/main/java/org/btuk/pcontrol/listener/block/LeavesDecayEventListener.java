package org.btuk.pcontrol.listener.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class LeavesDecayEventListener extends PhysicsListener {

    private final PControlTrigger triggerLeavesDecay
        = this.data.getTriggersRegisty().valueOf("LEAVES_DECAY");

    public LeavesDecayEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(LeavesDecayEvent event) {
        this.data.cancelIfDisabled(event, this.triggerLeavesDecay);
    }
}
