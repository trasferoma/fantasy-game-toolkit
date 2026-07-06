package it.fantasytoolkit.namegenerator.result;

import it.fantasytoolkit.core.pojo.GeneratedElementResult;
import it.fantasytoolkit.core.types.Seed;

public record NameResult(String name, Seed seed) implements GeneratedElementResult {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private Seed seed;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder seed(Seed seed) {
            this.seed = seed;
            return this;
        }

        public NameResult build() {
            return new NameResult(name, seed);
        }
    }
}
