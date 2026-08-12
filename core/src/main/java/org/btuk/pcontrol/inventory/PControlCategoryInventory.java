package org.btuk.pcontrol.inventory;

import org.bukkit.World;
import org.btuk.pcontrol.PControlDataBukkit;
import org.btuk.pcontrol.data.category.PControlCategory;

import javax.annotation.Nonnull;

public class PControlCategoryInventory extends PControlInventory {
    public PControlCategoryInventory(@Nonnull PControlDataBukkit data, @Nonnull World world) {
        super(
            data,
            world,
            3,
            data.getMessage("category-inventory-title", "%world%", world.getName())
        );
        for (PControlCategory category : data.getCategoriesRegistry().values()) {
            this.setItem(category.getSlot(), category.getIcon(), player ->
                player.openInventory(data.getInventory(category, world).getInventory()));
        }
    }
}
