package it.fantasytoolkit.armourgenerator.rules;

import java.util.EnumMap;
import java.util.Map;

import it.fantasytoolkitcore.core.model.Rarity;

public final class DefaultArmourRules implements ArmourRules {

    private static final Map<Rarity, DefenseRange> DEFENSE_RANGE_BY_RARITY = buildDefenseRangeByRarity();

    @Override
    public DefenseRange defenseFor(Rarity rarity) {
        return DEFENSE_RANGE_BY_RARITY.get(rarity);
    }

    private static Map<Rarity, DefenseRange> buildDefenseRangeByRarity() {
        Map<Rarity, DefenseRange> defenseRangeByRarity = new EnumMap<>(Rarity.class);

        defenseRangeByRarity.put(Rarity.COMMON, new DefenseRange(1, 2));
        defenseRangeByRarity.put(Rarity.UNCOMMON, new DefenseRange(2, 4));
        defenseRangeByRarity.put(Rarity.RARE, new DefenseRange(4, 7));
        defenseRangeByRarity.put(Rarity.EPIC, new DefenseRange(7, 11));
        defenseRangeByRarity.put(Rarity.LEGENDARY, new DefenseRange(11, 18));

        return defenseRangeByRarity;
    }
}
