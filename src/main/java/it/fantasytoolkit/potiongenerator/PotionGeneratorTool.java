package it.fantasytoolkit.potiongenerator;

import java.util.List;
import java.util.Random;

import it.fantasytoolkitcore.core.model.PotionType;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.buffdebuffgenerator.BuffDebuffGeneratorTool;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkit.potiongenerator.result.PotionResult;
import it.fantasytoolkit.potiongenerator.rules.DefaultPotionRules;
import it.fantasytoolkit.potiongenerator.rules.PotionRules;
import it.fantasytoolkit.potiongenerator.rules.RegenerationRange;

public final class PotionGeneratorTool {

    private PotionGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private PotionType type;
        private boolean randomType;
        private Rarity rarity;
        private Rarity maxRarity;
        private RarityTable rarityTable;
        private boolean randomRarity;
        private PotionRules rules = new DefaultPotionRules();

        private Builder() {
        }

        public Builder type(PotionType type) {
            this.type = type;
            return this;
        }

        public Builder randomType() {
            this.randomType = true;
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

        public Builder rules(PotionRules rules) {
            this.rules = rules;
            return this;
        }

        public PotionResult generate() {
            validateTypeSource();
            validateRaritySource();

            Random random = new Random();
            PotionType resolvedType = resolveType(random);
            Rarity resolvedRarity = resolveRarity(random);

            return switch (resolvedType) {
                case BUFF -> buffPotion(resolvedRarity);
                case DEBUFF -> debuffPotion(resolvedRarity);
                case HEALTH_REGENERATION, MANA_REGENERATION -> regenerationPotion(resolvedType, resolvedRarity, random);
            };
        }

        private PotionResult buffPotion(Rarity rarity) {
            BuffElement buffElement = generateBuffElement(rarity);

            return PotionResult.builder()
                    .type(PotionType.BUFF)
                    .rarity(rarity)
                    .buff(buffElement)
                    .build();
        }

        private PotionResult debuffPotion(Rarity rarity) {
            BuffElement buffElement = generateBuffElement(rarity);
            DebuffElement debuffElement = new DebuffElement(buffElement.characteristic(), buffElement.value());

            return PotionResult.builder()
                    .type(PotionType.DEBUFF)
                    .rarity(rarity)
                    .debuff(debuffElement)
                    .build();
        }

        private PotionResult regenerationPotion(PotionType type, Rarity rarity, Random random) {
            RegenerationRange regenerationRange = rules.regenerationFor(rarity);
            int value = randomValueInRange(regenerationRange.minValue(), regenerationRange.maxValue(), random);

            return PotionResult.builder()
                    .type(type)
                    .rarity(rarity)
                    .value(value)
                    .build();
        }

        private BuffElement generateBuffElement(Rarity rarity) {
            List<BuffElement> buffs = BuffDebuffGeneratorTool.building()
                    .rarity(rarity)
                    .generate()
                    .buffs();
            return buffs.get(0);
        }

        private int randomValueInRange(int minValue, int maxValue, Random random) {
            return minValue + random.nextInt(maxValue - minValue + 1);
        }

        private void validateTypeSource() {
            int typeSourceCount = countTrue(type != null, randomType);

            if (typeSourceCount > 1) {
                throw new IllegalStateException("Only one of type or randomType can be used together");
            }
            if (typeSourceCount == 0) {
                throw new IllegalStateException("Type must be set before generating a potion");
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
                        "One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a potion");
            }
        }

        private PotionType resolveType(Random random) {
            if (randomType) {
                PotionType[] types = PotionType.values();
                return types[random.nextInt(types.length)];
            }
            return type;
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
