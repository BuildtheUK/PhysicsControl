package org.btuk.pcontrol.data;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.plugin.Plugin;
import org.btuk.pcontrol.VersionsAdapter;
import org.btuk.pcontrol.data.category.CategoriesRegistry;
import org.btuk.pcontrol.data.category.PControlCategory;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.data.trigger.TriggersRegistry;
import org.btuk.pcontrol.set.parser.TypesSetsParser;
import org.btuk.pcontrol.text.Text;
import org.btuk.pcontrol.text.TextHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.logging.Logger;

public interface PControlData {
    @Nonnull
    Plugin getPlugin();

    @Nonnull
    default Server server() {
        return this.getPlugin().getServer();
    }

    @Nonnull
    default Logger log() {
        return this.getPlugin().getLogger();
    }

    @Nonnull
    Set<EntityType> getRemovableProjectileTypes();

    @Nonnull
    Text getMessage(@Nonnull String key, @Nonnull String... placeholders);

    String getTriggerName(@Nonnull PControlTrigger trigger);

    @Nonnull
    String getCategoryName(@Nonnull PControlCategory category);

    boolean hasVersion(int majorVersion, int minorVersion, int patchVersion);

    boolean isVersion(int majorVersion, int minorVersion, int patchVersion);

    default <E extends BlockEvent & Cancellable> void cancelIfDisabled(@Nonnull E event, @Nonnull PControlTrigger trigger) {
        this.cancelIfDisabled(event, event.getBlock().getWorld(), trigger);
    }

    default <E extends WorldEvent & Cancellable> void cancelIfDisabled(@Nonnull E event, @Nonnull PControlTrigger trigger) {
        this.cancelIfDisabled(event, event.getWorld(), trigger);
    }

    default <E extends EntityEvent & Cancellable> void cancelIfDisabled(@Nonnull E event, @Nonnull PControlTrigger trigger) {
        this.cancelIfDisabled(event, event.getEntity().getWorld(), trigger);
    }

    void cancelIfDisabled(@Nonnull Cancellable event, @Nonnull World world, @Nonnull PControlTrigger trigger);

    boolean isActionAllowed(@Nonnull World world, @Nonnull PControlTrigger trigger);

    void announce(@Nullable World world, @Nonnull Text text);

    @Nonnull
    CustomTags getCustomTags();

    @Nonnull
    CategoriesRegistry getCategoriesRegistry();

    @Nonnull
    TriggersRegistry getTriggersRegisty();

    @Nonnull
    VersionsAdapter getVersionsAdapter();

    @Nonnull
    TextHelper getTextHelper();

    @Nonnull
    TypesSetsParser getTypesSetsParser();
}
