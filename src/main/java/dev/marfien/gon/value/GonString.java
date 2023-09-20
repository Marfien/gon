package dev.marfien.gon.value;

import java.util.Objects;

public non-sealed class GonString implements GonValue<String> {

    private final String value;

    public GonString(String value) {
        this.value = Objects.requireNonNull(value, "Value cannot be null. Use GonValue.NULL instead.");
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public String toString() {
        return "\"%s\"".formatted(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GonString str)) return false;

        return Objects.equals(str.value, this.value);
    }

    @Override
    public int hashCode() {
        return 47^this.value.hashCode();
    }

}
