package dev.marfien.gon.value;

import java.util.Objects;

public non-sealed class GonFloat implements GonValue<Double> {

    private final Double value;

    public GonFloat(Double value) {
        this.value = Objects.requireNonNull(value, "Value cannot be null. Use GonValue.NULL instead.");
    }

    @Override
    public Double get() {
        return this.value;
    }

    @Override
    public String toString() {
        return Objects.toString(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GonFloat f)) return false;

        return Objects.equals(f.value, this.value);
    }

    @Override
    public int hashCode() {
        return 47^this.value.hashCode();
    }

}
