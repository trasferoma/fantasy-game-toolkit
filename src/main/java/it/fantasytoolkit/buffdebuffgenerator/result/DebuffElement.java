package it.fantasytoolkit.buffdebuffgenerator.result;

import it.fantasytoolkitcore.core.model.Characteristic;

public record DebuffElement(Characteristic characteristic, int value) implements StatusEffect {
}
