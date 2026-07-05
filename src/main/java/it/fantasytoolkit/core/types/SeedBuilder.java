package it.fantasytoolkit.core.types;

import java.util.concurrent.ThreadLocalRandom;

public final class SeedBuilder {

    private SeedBuilder() {
    }

    public static SeedBuilder newSeed() {
        return new SeedBuilder();
    }

    public Seed build() {
        return new Seed(generateRandomValue());
    }

    private long generateRandomValue() {
        return ThreadLocalRandom.current().nextLong();
    }
}
