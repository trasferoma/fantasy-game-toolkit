package it.fantasytoolkit.potiongenerator.rules;

import java.util.EnumMap;
import java.util.Map;

import it.fantasytoolkitcore.core.model.Rarity;

public final class DefaultPotionRules implements PotionRules {

    private static final Map<Rarity, RegenerationRange> REGENERATION_RANGE_BY_RARITY = buildRegenerationRangeByRarity();

    @Override
    public RegenerationRange regenerationFor(Rarity rarity) {
        return REGENERATION_RANGE_BY_RARITY.get(rarity);
    }

    private static Map<Rarity, RegenerationRange> buildRegenerationRangeByRarity() {
        Map<Rarity, RegenerationRange> regenerationRangeByRarity = new EnumMap<>(Rarity.class);

        regenerationRangeByRarity.put(Rarity.COMMON, new RegenerationRange(5, 10));
        regenerationRangeByRarity.put(Rarity.UNCOMMON, new RegenerationRange(10, 20));
        regenerationRangeByRarity.put(Rarity.RARE, new RegenerationRange(20, 35));
        regenerationRangeByRarity.put(Rarity.EPIC, new RegenerationRange(35, 55));
        regenerationRangeByRarity.put(Rarity.LEGENDARY, new RegenerationRange(55, 90));

        return regenerationRangeByRarity;
    }
}
