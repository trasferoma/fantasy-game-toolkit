package it.fantasytoolkit.jewelgenerator;

import java.util.List;
import java.util.Random;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.buffdebuffgenerator.BuffDebuffGeneratorTool;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffDebuffResult;
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
        private boolean randomRarity;
        private boolean noStatusEffect;

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

        public Builder randomRarity() {
            this.randomRarity = true;
            return this;
        }

        public Builder noStatusEffect() {
            this.noStatusEffect = true;
            return this;
        }

        public JewelResult generate() {
            validateJewelSource();
            validateRaritySource();

            Random random = new Random();
            Jewel resolvedJewel = resolveJewel(random);
            Rarity resolvedRarity = resolveRarity(random);
            BuffDebuffResult buffDebuffResult = resolveBuffDebuffResult(resolvedRarity);

            return JewelResult.builder()
                    .jewel(resolvedJewel)
                    .rarity(resolvedRarity)
                    .buffs(buffDebuffResult.buffs())
                    .debuffs(buffDebuffResult.debuffs())
                    .build();
        }

        private BuffDebuffResult resolveBuffDebuffResult(Rarity resolvedRarity) {
            if (noStatusEffect) {
                return new BuffDebuffResult(List.of(), List.of());
            }
            return BuffDebuffGeneratorTool.building()
                    .rarity(resolvedRarity)
                    .generate();
        }

        private void validateJewelSource() {
            int jewelSourceCount = countTrue(jewel != null, randomJewel);

            if (jewelSourceCount > 1) {
                throw new IllegalStateException("Only one of jewel or randomJewel can be used together");
            }
            if (jewelSourceCount == 0) {
                throw new IllegalStateException("Jewel must be set before generating a jewel");
            }
        }

        private void validateRaritySource() {
            int raritySourceCount = countRaritySources();
            if (raritySourceCount > 1) {
                throw new IllegalStateException(
                        "Only one of rarity, maxRarity, rarityTable or randomRarity can be used together");
            }
            if (raritySourceCount == 0) {
                throw new IllegalStateException(
                        "One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a jewel");
            }
        }

        private Jewel resolveJewel(Random random) {
            if (randomJewel) {
                Jewel[] jewels = Jewel.values();
                return jewels[random.nextInt(jewels.length)];
            }
            return jewel;
        }

        private int countRaritySources() {
            return countTrue(rarity != null, maxRarity != null, rarityTable != null, randomRarity);
        }

        private static int countTrue(boolean... conditions) {
            int count = 0;
            for (boolean condition : conditions) {
                if (condition) {
                    count++;
                }
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
            if (rarityTable != null) {
                return rarityTable.draw(random);
            }
            return randomRarityAcrossAll(random);
        }

        private Rarity randomRarityUpTo(Rarity max, Random random) {
            int index = random.nextInt(max.ordinal() + 1);
            return Rarity.values()[index];
        }

        private Rarity randomRarityAcrossAll(Random random) {
            Rarity[] rarities = Rarity.values();
            return rarities[random.nextInt(rarities.length)];
        }
    }
}
