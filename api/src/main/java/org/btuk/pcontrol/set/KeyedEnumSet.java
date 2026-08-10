package org.btuk.pcontrol.set;

import org.bukkit.Keyed;
import org.bukkit.Tag;

import javax.annotation.Nonnull;

public abstract class KeyedEnumSet<E extends Enum<E> & Keyed, T> extends CustomEnumSet<E, T> {
    public KeyedEnumSet(@Nonnull Class<E> enumClass) {
        super(enumClass);
    }

    @Nonnull
    public CustomEnumSet<E, T> addPrimitive(@Nonnull Tag<E> tag) {
        return this.addPrimitive(tag.getValues());
    }
}
