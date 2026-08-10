package org.btuk.pcontrol.listener.custom;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class BoneMealUsageListener extends PhysicsListener {

    private final PControlTrigger triggerPlayersBoneMealUsage
        = this.data.getTriggersRegisty().valueOf("PLAYERS_BONE_MEAL_USAGE");
    protected final Set<Vector> fertilizedBlocks = new HashSet<>();

    public BoneMealUsageListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);

        data.server().getScheduler().runTaskTimer(
            data.getPlugin(),
            this.fertilizedBlocks::clear,
            1L, 1L
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void on(StructureGrowEvent event) {
        if (event.getPlayer() != null) {
            this.data.cancelIfDisabled(event, this.triggerPlayersBoneMealUsage);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void on(BlockGrowEvent event) {
        if (this.fertilizedBlocks.remove(event.getBlock().getLocation().toVector())) {
            this.data.cancelIfDisabled(event, this.triggerPlayersBoneMealUsage);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void on(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null) {
            throw new IllegalArgumentException("Block absent on " + PlayerInteractEvent.class.getSimpleName()
                + " with " + event.getAction().getClass().getSimpleName() + "." + event.getAction().name());
        }

        ItemStack usedItem = event.getItem();
        if (usedItem == null) return;
        if (!this.versionsAdapter.isBoneMealItem(usedItem)) return;

        if (!this.data.isActionAllowed(targetBlock.getWorld(), this.triggerPlayersBoneMealUsage)) {
            event.setUseItemInHand(Event.Result.DENY);
        }
        if (event.useItemInHand() == Event.Result.DENY) return;
        this.fertilizedBlocks.add(targetBlock.getLocation().toVector());
    }
}
