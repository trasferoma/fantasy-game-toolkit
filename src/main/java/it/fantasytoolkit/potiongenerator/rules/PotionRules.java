package it.fantasytoolkit.potiongenerator.rules;

import it.fantasytoolkitcore.core.model.Rarity;

public interface PotionRules {

    RegenerationRange regenerationFor(Rarity rarity);
}
