package it.fantasytoolkit.core.types;

public record Seed (String value){
    public static Seed of (String value) {
        return new Seed(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
