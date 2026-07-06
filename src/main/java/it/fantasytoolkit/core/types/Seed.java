package it.fantasytoolkit.core.types;

public record Seed(long value) {

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
