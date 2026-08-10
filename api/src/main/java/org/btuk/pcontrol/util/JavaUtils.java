package org.btuk.pcontrol.util;

import javax.annotation.Nonnull;

public class JavaUtils {
    public static <E extends Enum<E>> E getEnum(@Nonnull Class<E> clazz, @Nonnull String name) {
        try {
            return Enum.valueOf(clazz, name);
        } catch (IllegalArgumentException var3) {
            return null;
        }
    }
}
