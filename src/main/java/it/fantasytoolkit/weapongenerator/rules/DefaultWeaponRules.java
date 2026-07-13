package it.fantasytoolkit.weapongenerator.rules;

import java.util.EnumMap;
import java.util.Map;

import it.fantasytoolkitcore.core.model.Rarity;

public final class DefaultWeaponRules implements WeaponRules {

    private static final Map<Rarity, AttackRange> ATTACK_RANGE_BY_RARITY = buildAttackRangeByRarity();

    @Override
    public AttackRange attackFor(Rarity rarity) {
        return ATTACK_RANGE_BY_RARITY.get(rarity);
    }

    private static Map<Rarity, AttackRange> buildAttackRangeByRarity() {
        Map<Rarity, AttackRange> attackRangeByRarity = new EnumMap<>(Rarity.class);

        attackRangeByRarity.put(Rarity.COMMON, new AttackRange(1, 3));
        attackRangeByRarity.put(Rarity.UNCOMMON, new AttackRange(3, 6));
        attackRangeByRarity.put(Rarity.RARE, new AttackRange(6, 10));
        attackRangeByRarity.put(Rarity.EPIC, new AttackRange(10, 15));
        attackRangeByRarity.put(Rarity.LEGENDARY, new AttackRange(15, 25));

        return attackRangeByRarity;
    }
}
