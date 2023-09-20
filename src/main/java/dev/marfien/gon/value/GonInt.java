package dev.marfien.gon.value;

import java.util.Objects;

public non-sealed class GonInt implements GonValue<Long> {

    private final Long value;

    public GonInt(Long value) {
        this.value = Objects.requireNonNull(value, "Value cannot be null. Use GonValue.NULL instead.");
    }

    @Override
    public Long get() {
        return this.value;
    }

    @Override
    public String toString() {
        return Objects.toString(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GonInt i)) return false;

        return Objects.equals(i.value, this.value);
    }

    @Override
    public int hashCode() {
        return 47^this.value.hashCode();
    }

}