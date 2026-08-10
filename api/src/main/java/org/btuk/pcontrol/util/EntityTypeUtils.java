package org.btuk.pcontrol.util;

import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EntityTypeUtils {

    @Nonnull
    public static Set<EntityType> matchEntityTypes(@Nullable Consumer<String> onFail, @Nonnull String... names) {
        Set<EntityType> result = new HashSet<>();
        for (String name : names) {
            EntityType element = JavaUtils.getEnum(EntityType.class, name);
            if (element == null) {
                if (onFail != null) onFail.accept(name);
            } else {
                result.add(element);
            }
        }
        return result;
    }

}
