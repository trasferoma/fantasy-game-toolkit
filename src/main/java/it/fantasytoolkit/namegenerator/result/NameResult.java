package it.fantasytoolkit.namegenerator.result;

import it.fantasytoolkit.core.pojo.GeneratedElementResult;

public record NameResult(String name) implements GeneratedElementResult {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public NameResult build() {
            return new NameResult(name);
        }
    }
}
