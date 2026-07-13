package it.fantasytoolkit.armourgenerator;

import java.util.List;
import java.util.Random;

import it.fantasytoolkitcore.core.model.Armour;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.buffdebuffgenerator.BuffDebuffGeneratorTool;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffDebuffResult;
import it.fantasytoolkit.armourgenerator.result.ArmourResult;

public final class ArmourGeneratorTool {

    private ArmourGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private Armour armour;
        private boolean randomArmour;
        private Rarity rarity;
        private Rarity maxRarity;
        private RarityTable rarityTable;
        private boolean randomRarity;
        private boolean noStatusEffect;

        private Builder() {
        }

        public Builder armour(Armour armour) {
            this.armour = armour;
            return this;
        }

        public Builder randomArmour() {
            this.randomArmour = true;
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

        public ArmourResult generate() {
            validateArmourSource();
            validateRaritySource();

            Random random = new Random();
            Armour resolvedArmour = resolveArmour(random);
            Rarity resolvedRarity = resolveRarity(random);
            BuffDebuffResult buffDebuffResult = resolveBuffDebuffResult(resolvedRarity);

            return ArmourResult.builder()
                    .armour(resolvedArmour)
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

        private void validateArmourSource() {
            int armourSourceCount = countTrue(armour != null, randomArmour);

            if (armourSourceCount > 1) {
                throw new IllegalStateException("Only one of armour or randomArmour can be used together");
            }
            if (armourSourceCount == 0) {
                throw new IllegalStateException("Armour must be set before generating an armour");
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
                        "One of rarity, maxRarity, rarityTable or randomRarity must be set before generating an armour");
            }
        }

        private Armour resolveArmour(Random random) {
            if (randomArmour) {
                Armour[] armours = Armour.values();
                return armours[random.nextInt(armours.length)];
            }
            return armour;
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
