package it.fantasytoolkit.jewelgenerator;

import java.util.Random;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.jewelgenerator.result.JewelResult;

public final class JewelGeneratorTool {

    private JewelGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private Jewel jewel;
        private boolean randomJewel;
        private Rarity rarity;
        private Rarity maxRarity;
        private RarityTable rarityTable;

        private Builder() {
        }

        public Builder jewel(Jewel jewel) {
            this.jewel = jewel;
            return this;
        }

        public Builder randomJewel() {
            this.randomJewel = true;
            return this;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder maxRarity(Rarity maxRarity) {
            this.maxRarity = maxRarity;
            return this;
        }

        public Builder rarityTable(RarityTable rarityTable) {
            this.rarityTable = rarityTable;
            return this;
        }

        public JewelResult generate() {
            Random random = new Random();
            Jewel resolvedJewel = resolveJewel(random);

            int raritySourceCount = countRaritySources();
            if (raritySourceCount > 1) {
                throw new IllegalStateException("Only one of rarity, maxRarity or rarityTable can be used together");
            }
            if (raritySourceCount == 0) {
                throw new IllegalStateException(
                        "One of rarity, maxRarity or rarityTable must be set before generating a jewel");
            }

            Rarity resolvedRarity = resolveRarity(random);

            return JewelResult.builder()
                    .jewel(resolvedJewel)
                    .rarity(resolvedRarity)
                    .build();
        }

        private Jewel resolveJewel(Random random) {
            if (randomJewel) {
                Jewel[] jewels = Jewel.values();
                return jewels[random.nextInt(jewels.length)];
            }
            if (jewel == null) {
                throw new IllegalStateException("Jewel must be set before generating a jewel");
            }
            return jewel;
        }

        private int countRaritySources() {
            int count = 0;
            if (rarity != null) {
                count++;
            }
            if (maxRarity != null) {
                count++;
            }
            if (rarityTable != null) {
                count++;
            }
            return count;
        }

        private Rarity resolveRarity(Random random) {
            if (rarity != null) {
                return rarity;
            }
            if (maxRarity != null) {
                return randomRarityUpTo(maxRarity, random);
            }
            return rarityTable.draw(random);
        }

        private Rarity randomRarityUpTo(Rarity max, Random random) {
            int index = random.nextInt(max.ordinal() + 1);
            return Rarity.values()[index];
        }
    }
}
