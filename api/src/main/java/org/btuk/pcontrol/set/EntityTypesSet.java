package org.btuk.pcontrol.set;

import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class EntityTypesSet extends KeyedEnumSet<EntityType, EntityType> {

    @Nonnull
    public static Set<EntityType> createPrimitive(@Nonnull String setName, @Nonnull Logger logger, @Nonnull Consumer<EntityTypesSet> consumer) {
        EntityTypesSet result = new EntityTypesSet();
        try {
            consumer.accept(result);
        } catch (NoSuchFieldError e) {
            logger.warning("Unable to fill set " + setName + ". " +
                "Entity type " + e.getMessage() + " not found. Plugin may not work correctly");
        }
        return Collections.unmodifiableSet(result.getValues());
    }

    private EntityTypesSet() {
        super(EntityType.class);
    }

    @Nonnull
    @Override
    public EntityType enumToElement(@Nonnull EntityType enumValue, @Nonnull String elementName) {
        return enumValue;
    }
}
