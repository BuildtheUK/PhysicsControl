package org.btuk.pcontrol.set;

import org.bukkit.TreeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class TreeTypesSet extends CustomEnumSet<TreeType, TreeType> {

    @Nonnull
    public static Set<TreeType> createPrimitive(@Nonnull String setName, @Nonnull Logger logger, @Nonnull Consumer<TreeTypesSet> consumer) {
        TreeTypesSet result = new TreeTypesSet();
        try {
            consumer.accept(result);
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            logger.warning("Unable to fill set " + setName + ". " +
                "Tree type " + e.getMessage() + " not found. Plugin may not work correctly");
        }
        return Collections.unmodifiableSet(result.getValues());
    }

    private TreeTypesSet() {
        super(TreeType.class);
    }

    @Nonnull
    @Override
    public TreeType enumToElement(@Nonnull TreeType enumValue, @Nullable String elementName) {
        return enumValue;
    }
}
