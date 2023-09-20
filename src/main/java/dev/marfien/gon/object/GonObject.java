package dev.marfien.gon.object;

import dev.marfien.gon.value.GonValue;

import java.util.Map;
import java.util.Optional;

public abstract class GonObject {

    private final String name;
    private final String clazz;

    private Map<String, GonValue> attributes;

    protected GonObject(String name, String clazz, Map<String, GonValue> attributes) {
        this.name = name;
        this.clazz = clazz;
        this.attributes = Map.copyOf(attributes);
    }

    public final Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public final Optional<String> getClazz() {
        return Optional.ofNullable(this.clazz);
    }

    public final Map<String, GonValue> getAttributes() {
        return this.attributes;
    }
}
