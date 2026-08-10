package org.btuk.pcontrol.util;

import java.util.function.Supplier;

public interface SneakyThrowsSupplier<T> extends Supplier<T> {
    @Override
    default T get() {
        try {
            return this.supply();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to execute supplier", t);
        }
    }

    T supply() throws Throwable;
}
