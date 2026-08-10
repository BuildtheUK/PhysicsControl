package org.btuk.pcontrol.listener.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.MaterialRules;
import org.btuk.pcontrol.util.LocationUtils;

import javax.annotation.Nonnull;

public class BlockPhysicsEventListener extends PhysicsListener {
    private static final boolean DEBUG = false;

    private final MaterialRules rulesBlockPhysicsEventFrom = new MaterialRules(
        this.data, BlockPhysicsEvent.class, "from");

    public BlockPhysicsEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesBlockPhysicsEventFrom);
    }

    @Override
    protected void unregisterUnavailableTriggers() {
    }

    @SuppressWarnings("ConcatenationWithEmptyString")
    @EventHandler(ignoreCancelled = true)
    private void on(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Block source = event.getSourceBlock();

        if (DEBUG) {
            this.debugAction(event, block.getLocation(), ""
                + "block=" + block.getType() + ";"
                + "source=" + source.getType() + ";"
                + "changed=" + event.getChangedType() + ";"
            );
        }

        PControlTrigger trigger;
        Block sourceRelative;

        BlockFace faceUp = BlockFace.UP;
        sourceRelative = source.getRelative(faceUp);
        if (DEBUG) {
            this.debugAction(event, sourceRelative.getLocation(), ""
                + "face=" + faceUp + ";"
                + "sourceRelative=" + sourceRelative.getType() + ";"
            );
        }
        trigger = this.rulesBlockPhysicsEventFrom.findTrigger(sourceRelative.getType());
        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
            return;
        }

        for (BlockFace face : LocationUtils.NSWE_FACES) {
            sourceRelative = source.getRelative(face);
            if (DEBUG) {
                this.debugAction(event, sourceRelative.getLocation(), ""
                    + "face=" + face + ";"
                    + "sourceRelative=" + sourceRelative.getType() + ";"
                );
            }
            if (!this.versionsAdapter.isFacingAt(sourceRelative, face)) continue;
            trigger = this.rulesBlockPhysicsEventFrom.findTrigger(sourceRelative.getType());
            if (trigger != null) {
                this.data.cancelIfDisabled(event, trigger);
                return;
            }
        }
    }
}
