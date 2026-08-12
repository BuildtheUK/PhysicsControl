package org.btuk.pcontrol.listener.player;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class PlayerInteractEventListener extends PhysicsListener {
    private final MaterialRules rulesPlayerInteractEventPhysicalMaterial = new MaterialRules(
        this.data, PlayerInteractEvent.class, "physical-material");
    private final MaterialRules rulesPlayerInteractEventClickedMaterial = new MaterialRules(
        this.data, PlayerInteractEvent.class, "right-clicked-material");

    public PlayerInteractEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesPlayerInteractEventPhysicalMaterial);
        parser.registerParser(this.rulesPlayerInteractEventClickedMaterial);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        Block interactedBlock = event.getClickedBlock();
        if (interactedBlock == null) return;
        if (event.getAction() == Action.PHYSICAL) {
            if (event.getBlockFace() != BlockFace.SELF) return;
            Material material = interactedBlock.getType();

            PControlTrigger trigger = this.rulesPlayerInteractEventPhysicalMaterial.findTrigger(material);

            if (trigger != null) {
                this.data.cancelIfDisabled(event, world, trigger);
            } else {
                this.unrecognizedAction(event, interactedBlock.getLocation(), material + " (by player " + player.getName() + ")");
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material clickedType = interactedBlock.getType();
            PControlTrigger trigger = this.rulesPlayerInteractEventClickedMaterial.findTrigger(clickedType);
            if (trigger != null) {
                this.data.cancelIfDisabled(event, world, trigger);
            }
        }
    }
}
