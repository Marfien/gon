package dev.marfien.gon.object;

import dev.marfien.gon.value.GonValue;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NestedGonObject extends GonObject {

    private final List<GonObject> nestedObjects;
    private final Map<String, GonObject> nestedObjectsByKey = new HashMap<>();

    public NestedGonObject(String name, String clazz, Map<String, GonValue> attributes, List<GonObject> nestedObjects) {
        super(name, clazz, attributes);
        this.nestedObjects = List.copyOf(nestedObjects);

        for (GonObject nestedObject : nestedObjects) {
            nestedObject.getName().ifPresent(key -> {
                if (nestedObjectsByKey.containsKey(key))
                    // TODO specify in witch object
                    throw new IllegalArgumentException(
                            "There is already a nested gon object present with the name '%s'".formatted(key));

                this.nestedObjectsByKey.put(key, nestedObject);
            });
        }
    }

    public List<GonObject> getNestedObjects() {
        return this.nestedObjects;
    }

    public GonObjectFilter filter() {
        return filter(Predicate::and);
    }

    public GonObjectFilter filter(GonObjectFilter.PredicateCombinator<GonObject> combinator) {
        return this.new GonObjectFilter(combinator);
    }

    public class GonObjectFilter {

        private final PredicateCombinator<GonObject> combinator;

        // default value with no effect on the result.
        private Predicate<GonObject> filter = obj -> true;

        private GonObjectFilter(PredicateCombinator<GonObject> combinator) {
            this.combinator = Objects.requireNonNull(combinator, "Combinator must not be null.");
        }

        public Optional<GonObject> byKey(String key) {
            return Optional.ofNullable(nestedObjectsByKey.get(key))
                    .filter(this.filter);
        }

        public Collection<GonObject> all() {
            return nestedObjects.stream()
                    .filter(this.filter)
                    .toList();
        }

        public Optional<GonObject> first() {
            for (GonObject nestedObject : nestedObjects) {
                if (!this.filter.test(nestedObject)) continue;

                return Optional.of(nestedObject);
            }

            return Optional.empty();
        }

        public Optional<GonObject> atPosition(int pos) {
            if (pos < 0 || pos >= nestedObjects.size()) return Optional.empty();
            GonObject obj = nestedObjects.get(pos);

            if (this.filter.test(obj)) return Optional.of(obj);
            return Optional.empty();
        }

        public GonObjectFilter matchKey(String regex) {
            return this.matchKey(Pattern.compile(regex));
        }

        public GonObjectFilter matchKey(Pattern regexPattern) {
            return this.with(obj ->
                    obj.getName()
                            // if not present it is empty and will return empty
                            .filter(key -> regexPattern.matcher(key).matches())
                            .isPresent());
        }

        public GonObjectFilter withAttribute(String attribute) {
            return this.with(obj -> obj.getAttributes().containsKey(attribute));
        }

        public GonObjectFilter withAttribute(String attribute, GonValue value) {
            return this.withAttributeMatching(attribute, attributeValue -> Objects.equals(attributeValue, value));
        }

        public GonObjectFilter withAttributeMatching(String attribute, Predicate<GonValue> predicate) {
            return this.with(obj -> predicate.test(obj.getAttributes().get(attribute)));
        }

        public GonObjectFilter withClass(String clazz) {
            return this.with(obj -> Objects.equals(obj.getClazz().orElse(null), clazz));
        }

        public GonObjectFilter with(Predicate<GonObject> filter) {
            this.filter = this.combinator.combine(this.filter, filter);
            return this;
        }

        @FunctionalInterface
        public interface PredicateCombinator<T> {

            Predicate<T> combine(Predicate<T> t1, Predicate<T> t2);

        }
    }
}
