package it.fantasytoolkit.buffdebuffgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffDebuffResult;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkit.buffdebuffgenerator.rules.BuffCombination;
import it.fantasytoolkit.buffdebuffgenerator.rules.BuffDebuffRules;
import it.fantasytoolkit.buffdebuffgenerator.rules.DefaultBuffDebuffRules;
import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.Rarity;

public final class BuffDebuffGeneratorTool {

    private BuffDebuffGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {
        private Rarity rarity;
        private BuffDebuffRules rules = new DefaultBuffDebuffRules();

        private Builder() {
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder rules(BuffDebuffRules rules) {
            this.rules = rules;
            return this;
        }

        public BuffDebuffResult generate() {
            if (rarity == null) {
                throw new IllegalStateException("Rarity must be set before generating buffs and debuffs");
            }

            Random random = new Random();
            BuffCombination combination = pickCombination(random);
            List<BuffElement> buffs = buildBuffs(combination, random);

            List<DebuffElement> debuffs = List.of();

            return new BuffDebuffResult(buffs, debuffs);
        }

        private BuffCombination pickCombination(Random random) {
            List<BuffCombination> combinations = rules.combinationsFor(rarity);
            int index = random.nextInt(combinations.size());
            return combinations.get(index);
        }

        private List<BuffElement> buildBuffs(BuffCombination combination, Random random) {
            List<Characteristic> shuffledCharacteristics = new ArrayList<>(List.of(Characteristic.values()));
            Collections.shuffle(shuffledCharacteristics, random);

            List<BuffElement> buffs = new ArrayList<>();
            for (int i = 0; i < combination.count(); i++) {
                Characteristic characteristic = shuffledCharacteristics.get(i);
                int value = randomValueInRange(combination.minValue(), combination.maxValue(), random);
                buffs.add(new BuffElement(characteristic, value));
            }
            return buffs;
        }

        private int randomValueInRange(int minValue, int maxValue, Random random) {
            return minValue + random.nextInt(maxValue - minValue + 1);
        }
    }
}
