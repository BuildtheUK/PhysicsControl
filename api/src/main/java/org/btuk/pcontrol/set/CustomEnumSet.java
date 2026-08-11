package org.btuk.pcontrol.set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CustomEnumSet<E extends Enum<E>, T> extends CustomSet<T> {
    private final Class<E> enumClass;

    public CustomEnumSet(@Nonnull Class<E> enumClass) {
        super(new HashSet<>());
        this.enumClass = enumClass;
    }

    @Nonnull
    public CustomEnumSet<E, T> addPrimitive(@Nonnull Collection<E> elements) {
        this.add(elements.stream()
            .map(e -> this.enumToElement(e, null))
            .collect(Collectors.toList())
        );
        return this;
    }

    @Nonnull
    public CustomEnumSet<E, T> addPrimitive(@Nonnull CustomSet<E> elements) {
        this.addPrimitive(elements.getValues());
        return this;
    }

    @SafeVarargs
    @Nonnull
    public final CustomEnumSet<E, T> addPrimitive(@Nonnull E... elements) {
        this.addPrimitive(Arrays.asList(elements));
        return this;
    }

    @Nonnull
    public CustomEnumSet<E, T> add(@Nonnull Predicate<E> filter) {
        this.add(Stream
            .of(this.enumClass.getEnumConstants())
            .filter(filter)
            .map(e -> this.enumToElement(e, null))
            .collect(Collectors.toList())
        );
        return this;
    }

    @Nonnull
    public CustomEnumSet<E, T> add(@Nonnull String... elementNames) {
        List<T> elements = new ArrayList<>();
        for (String elementName : elementNames) {
            E enumValue = org.btuk.pcontrol.util.JavaUtils.getEnum(this.enumClass, elementName);
            if (enumValue != null) {
                elements.add(this.enumToElement(enumValue, elementName));
            }
        }
        this.add(elements);
        return this;
    }

    @Nonnull
    public abstract T enumToElement(@Nonnull E enumValue, @Nullable String elementName);
}
