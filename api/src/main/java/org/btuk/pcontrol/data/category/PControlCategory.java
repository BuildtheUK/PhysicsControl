package org.btuk.pcontrol.data.category;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.text.CommonColor;
import org.btuk.pcontrol.text.Text;
import org.btuk.pcontrol.text.TextHelper;
import org.btuk.pcontrol.util.PCMaterial;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PControlCategory {

    private final String name;
    private final short slot;
    private final ItemStack icon;
    private final List<PControlTrigger> triggers = new ArrayList<>();

    PControlCategory(@Nonnull String name, int row, int column, @Nonnull Set<PCMaterial> icons) {
        this.name = name;

        this.slot = (short) ((row - 1) * 9 + column - 1);

        if (icons.isEmpty()) {
            this.icon = null;
        } else {
            if (icons.size() != 1) {
                new IllegalArgumentException(
                    "Multiple icons found for category " + this + ": " + icons
                ).printStackTrace();
            }
            this.icon = icons.iterator().next().createStack(1);
        }
    }

    public void addTrigger(@Nonnull PControlTrigger trigger) {
        this.triggers.add(trigger);
    }

    @Nonnull
    public List<PControlTrigger> getTriggers() {
        return Collections.unmodifiableList(this.triggers);
    }

    public void prepareIcon(@Nonnull PControlData data) {
        if (this.icon == null) return;

        ItemMeta meta = this.icon.getItemMeta();
        if (meta == null) throw new IllegalArgumentException();

        TextHelper helper = data.getTextHelper();

        helper.setStackName(meta,
            helper.create(data.getCategoryName(this), CommonColor.YELLOW));

        List<Text> lore = new ArrayList<>();
        for (PControlTrigger trigger : this.triggers) {
            if (!trigger.isAvailable()) continue;
            lore.add(helper.create(" - " + data.getTriggerName(trigger), CommonColor.YELLOW));
        }
        helper.setStackLore(meta, lore);

        meta.addItemFlags(ItemFlag.values());

        this.icon.setItemMeta(meta);
    }

    @Nonnull
    public String name() {
        return this.name;
    }

    public short getSlot() {
        return this.slot;
    }

    @Nonnull
    public ItemStack getIcon() {
        if (this.icon == null) throw new IllegalArgumentException("Unable to find icon of category " + this);
        return this.icon;
    }

    @Override
    @Nonnull
    public String toString() {
        return this.name;
    }
}
