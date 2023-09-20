package dev.marfien.gon.value;

import java.util.Objects;

public non-sealed class GonBoolean implements GonValue<Boolean> {

    static final GonBoolean TRUE = new GonBoolean(Boolean.TRUE);
    static final GonBoolean FALSE = new GonBoolean(Boolean.FALSE);

    private final Boolean value;

    private GonBoolean(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GonBoolean bool)) return false;

        return Objects.equals(bool.value, this.value);
    }

    @Override
    public int hashCode() {
        return 47^this.value.hashCode();
    }

}
