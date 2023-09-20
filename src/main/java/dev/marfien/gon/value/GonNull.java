package dev.marfien.gon.value;

public final class GonNull implements GonValue<Object> {

    static final GonNull INSTANCE = new GonNull();

    private GonNull() {}

    @Override
    public Object get() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

    @Override
    public int hashCode() {
        return 47;
    }

}
