package dev.marfien.gon.value;

public sealed interface GonValue<T> permits GonBoolean, GonFloat, GonInt, GonNull, GonString {

    GonNull NULL = GonNull.INSTANCE;
    GonBoolean TRUE = GonBoolean.TRUE;
    GonBoolean FALSE = GonBoolean.FALSE;

    T get();

    String toString();

}
