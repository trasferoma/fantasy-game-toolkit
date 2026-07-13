package it.fantasytoolkit.weapongenerator;

import java.util.List;
import java.util.Random;

import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkitcore.core.model.Weapon;
import it.fantasytoolkit.buffdebuffgenerator.BuffDebuffGeneratorTool;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffDebuffResult;
import it.fantasytoolkit.weapongenerator.result.WeaponResult;
import it.fantasytoolkit.weapongenerator.rules.AttackRange;
import it.fantasytoolkit.weapongenerator.rules.DefaultWeaponRules;
import it.fantasytoolkit.weapongenerator.rules.WeaponRules;

public final class WeaponGeneratorTool {

    private WeaponGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private Weapon weapon;
        private boolean randomWeapon;
        private Rarity rarity;
        private Rarity maxRarity;
        private RarityTable rarityTable;
        private boolean randomRarity;
        private boolean noStatusEffect;
        private WeaponRules rules = new DefaultWeaponRules();

        private Builder() {
        }

        public Builder weapon(Weapon weapon) {
            this.weapon = weapon;
            return this;
        }

        public Builder randomWeapon() {
            this.randomWeapon = true;
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

        public Builder rules(WeaponRules rules) {
            this.rules = rules;
            return this;
        }

        public WeaponResult generate() {
            validateWeaponSource();
            validateRaritySource();

            Random random = new Random();
            Weapon resolvedWeapon = resolveWeapon(random);
            Rarity resolvedRarity = resolveRarity(random);
            BuffDebuffResult buffDebuffResult = resolveBuffDebuffResult(resolvedRarity);
            AttackRange attackRange = rules.attackFor(resolvedRarity);
            int attack = randomValueInRange(attackRange.minValue(), attackRange.maxValue(), random);

            return WeaponResult.builder()
                    .weapon(resolvedWeapon)
                    .rarity(resolvedRarity)
                    .buffs(buffDebuffResult.buffs())
                    .debuffs(buffDebuffResult.debuffs())
                    .attack(attack)
                    .build();
        }

        private int randomValueInRange(int minValue, int maxValue, Random random) {
            return minValue + random.nextInt(maxValue - minValue + 1);
        }

        private BuffDebuffResult resolveBuffDebuffResult(Rarity resolvedRarity) {
            if (noStatusEffect) {
                return new BuffDebuffResult(List.of(), List.of());
            }
            return BuffDebuffGeneratorTool.building()
                    .rarity(resolvedRarity)
                    .generate();
        }

        private void validateWeaponSource() {
            int weaponSourceCount = countTrue(weapon != null, randomWeapon);

            if (weaponSourceCount > 1) {
                throw new IllegalStateException("Only one of weapon or randomWeapon can be used together");
            }
            if (weaponSourceCount == 0) {
                throw new IllegalStateException("Weapon must be set before generating a weapon");
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
                        "One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a weapon");
            }
        }

        private Weapon resolveWeapon(Random random) {
            if (randomWeapon) {
                Weapon[] weapons = Weapon.values();
                return weapons[random.nextInt(weapons.length)];
            }
            return weapon;
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
