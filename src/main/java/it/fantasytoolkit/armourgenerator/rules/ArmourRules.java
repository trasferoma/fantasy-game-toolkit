package it.fantasytoolkit.armourgenerator.rules;

import it.fantasytoolkitcore.core.model.Rarity;

public interface ArmourRules {

    DefenseRange defenseFor(Rarity rarity);
}
