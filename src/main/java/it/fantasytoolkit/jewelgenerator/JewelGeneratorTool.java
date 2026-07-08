package it.fantasytoolkit.jewelgenerator;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkit.jewelgenerator.result.JewelResult;

public final class JewelGeneratorTool {

    private JewelGeneratorTool() {
    }

    public static Builder jewel(Jewel jewel) {
        return new Builder().jewel(jewel);
    }

    public static Builder rarity(Rarity rarity) {
        return new Builder().rarity(rarity);
    }

    public static final class Builder {
        private Jewel jewel;
        private Rarity rarity;

        private Builder() {
        }

        public Builder jewel(Jewel jewel) {
            this.jewel = jewel;
            return this;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public JewelResult generate() {
            if (jewel == null) {
                throw new IllegalStateException("Jewel must be set before generating a jewel");
            }
            if (rarity == null) {
                throw new IllegalStateException("Rarity must be set before generating a jewel");
            }

            return JewelResult.builder()
                    .jewel(jewel)
                    .rarity(rarity)
                    .build();
        }
    }
}
