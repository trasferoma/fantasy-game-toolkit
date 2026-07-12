package it.fantasytoolkit.buffdebuffgenerator.result;

import it.fantasytoolkitcore.core.model.Characteristic;

public record BuffElement(Characteristic characteristic, int value) implements StatusEffect {
}
