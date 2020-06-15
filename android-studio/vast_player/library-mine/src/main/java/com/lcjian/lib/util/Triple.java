package com.lcjian.lib.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

public class Triple<A, B, C> {

    public final @Nullable
    A first;
    public final @Nullable
    B second;
    public final @Nullable
    C third;

    public Triple(@Nullable A a, @Nullable B b, @Nullable C c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triple)) {
            return false;
        }
        Triple<?, ?, ?> p = (Triple<?, ?, ?>) o;
        return ObjectsCompat.equals(p.first, first)
                && ObjectsCompat.equals(p.second, second)
                && ObjectsCompat.equals(p.third, third);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }

    @NonNull
    @Override
    public String toString() {
        return "Triple{" + first + " " + second + " " + third + "}";
    }

    @NonNull
    public static <A, B, C> Triple<A, B, C> create(@Nullable A a, @Nullable B b, @Nullable C c) {
        return new Triple<A, B, C>(a, b, c);
    }
}
