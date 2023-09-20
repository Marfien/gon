package dev.marfien.gon.object;

import dev.marfien.gon.value.GonValue;

import java.util.Map;

public class EmptyGonObject extends GonObject {

    public EmptyGonObject(String name, String clazz, Map<String, GonValue> attributes) {
        super(name, clazz, attributes);
    }
}
