package org.btuk.pcontrol.listener.entity;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityInteractEvent;
import org.btuk.pcontrol.PhysicsListener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.pair.EntityMaterialRules;
import org.btuk.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class EntityInteractEventListener extends PhysicsListener {

    private final MaterialRules rulesEntityInteractEventMaterial = new MaterialRules(
        this.data, EntityInteractEvent.class, "material");
    private final EntityMaterialRules rulesEntityInteractEventEntityMaterial = new EntityMaterialRules(
        this.data, EntityInteractEvent.class, "interacted-by", "material");

    public EntityInteractEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesEntityInteractEventMaterial);
        parser.registerParser(this.rulesEntityInteractEventEntityMaterial);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(EntityInteractEvent event) {
        Block interactedBlock = event.getBlock();
        Entity entity = event.getEntity();
        World world = interactedBlock.getWorld();
        Material material = interactedBlock.getType();

        PControlTrigger trigger = this.rulesEntityInteractEventEntityMaterial.findTrigger(entity.getType(), material);
        if (trigger == null) trigger = this.rulesEntityInteractEventMaterial.findTrigger(material);

        if (trigger != null) {
            this.data.cancelIfDisabled(event, world, trigger);
        } else {
            this.unrecognizedAction(event, interactedBlock.getLocation(), material + " (by entity " + entity.getType() + ")");
        }
    }
}
