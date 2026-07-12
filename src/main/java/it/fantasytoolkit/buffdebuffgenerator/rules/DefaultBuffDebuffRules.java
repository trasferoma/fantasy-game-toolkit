package it.fantasytoolkit.buffdebuffgenerator.rules;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import it.fantasytoolkitcore.core.model.Rarity;

public final class DefaultBuffDebuffRules implements BuffDebuffRules {

    private static final Map<Rarity, List<BuffCombination>> COMBINATIONS_BY_RARITY = buildCombinationsByRarity();

    @Override
    public List<BuffCombination> combinationsFor(Rarity rarity) {
        return COMBINATIONS_BY_RARITY.get(rarity);
    }

    private static Map<Rarity, List<BuffCombination>> buildCombinationsByRarity() {
        Map<Rarity, List<BuffCombination>> combinationsByRarity = new EnumMap<>(Rarity.class);

        combinationsByRarity.put(Rarity.COMMON, List.of(
                new BuffCombination(1, 1, 2)));

        combinationsByRarity.put(Rarity.UNCOMMON, List.of(
                new BuffCombination(1, 3, 4),
                new BuffCombination(2, 1, 2)));

        combinationsByRarity.put(Rarity.RARE, List.of(
                new BuffCombination(1, 5, 6),
                new BuffCombination(2, 3, 4),
                new BuffCombination(3, 1, 2)));

        combinationsByRarity.put(Rarity.EPIC, List.of(
                new BuffCombination(1, 7, 8),
                new BuffCombination(2, 5, 6),
                new BuffCombination(3, 3, 4),
                new BuffCombination(4, 1, 2)));

        combinationsByRarity.put(Rarity.LEGENDARY, List.of(
                new BuffCombination(1, 9, 10),
                new BuffCombination(2, 7, 8),
                new BuffCombination(3, 5, 6),
                new BuffCombination(4, 3, 4),
                new BuffCombination(5, 1, 2)));

        return combinationsByRarity;
    }
}
