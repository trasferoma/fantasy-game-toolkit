package it.fantasytoolkit.jewelgenerator.result;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record JewelResult(Jewel jewel, Rarity rarity) implements GeneratedElementResult {
    public static JewelResult.Builder builder() {
        return new JewelResult.Builder();
    }

    public static final class Builder {
        private Jewel jewel;
        private Rarity rarity;

        private Builder() {
        }

        public JewelResult.Builder jewel(Jewel jewel) {
            this.jewel = jewel;
            return this;
        }

        public JewelResult.Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public JewelResult build() {
            return new JewelResult(jewel, rarity);
        }
    }
}
