package it.fantasytoolkit.buffdebuffgenerator.rules;

import java.util.List;

import it.fantasytoolkitcore.core.model.Rarity;

public interface BuffDebuffRules {

    List<BuffCombination> combinationsFor(Rarity rarity);
}
