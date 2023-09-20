package dev.marfien.gon.object;

import dev.marfien.gon.value.GonValue;

import java.util.Map;

public class SingleValueGonObject extends GonObject {

    private final GonValue value;

    public SingleValueGonObject(String name, String clazz, Map<String, GonValue> attributes, GonValue value) {
        super(name, clazz, attributes);
        this.value = value;
    }

    public GonValue getValue() {
        return this.value;
    }
}
