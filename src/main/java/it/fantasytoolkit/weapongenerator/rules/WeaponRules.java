package it.fantasytoolkit.weapongenerator.rules;

import it.fantasytoolkitcore.core.model.Rarity;

public interface WeaponRules {

    AttackRange attackFor(Rarity rarity);
}
