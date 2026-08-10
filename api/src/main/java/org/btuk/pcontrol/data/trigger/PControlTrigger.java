package org.btuk.pcontrol.data.trigger;

import org.bukkit.inventory.ItemStack;
import org.btuk.pcontrol.data.category.PControlCategory;
import org.btuk.pcontrol.util.PCMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class PControlTrigger {
    private final String name;
    private final PControlCategory category;
    private boolean triggerAvailable = false;
    private final boolean realtime;
    private final boolean defaults;
    private final ItemStack icon;

    PControlTrigger(@Nonnull String name, @Nonnull PControlCategory category, boolean realtime, boolean defaults, @Nonnull Set<PCMaterial> icons) {
        this.name = name;
        this.category = category;
        this.realtime = realtime;
        this.defaults = defaults;

        if (icons.isEmpty()) {
            this.icon = null;
        } else {
            if (icons.size() != 1) {
                new IllegalArgumentException(
                    "Multiple icons found for trigger " + this + ": " + icons
                ).printStackTrace();
            }
            this.icon = icons.iterator().next().createStack(1);
        }
    }

    @Nonnull
    public String name() {
        return this.name;
    }

    @Nonnull
    public PControlCategory getCategory() {
        return this.category;
    }

    public void markAvailable() {
        this.triggerAvailable = true;
    }

    public boolean isAvailable() {
        return this.triggerAvailable;
    }

    public boolean isRealtime() {
        return this.realtime;
    }

    public boolean getDefaultValue() {
        return this.defaults;
    }

    @Nullable
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    @Nonnull
    public String toString() {
        return this.name;
    }
}
