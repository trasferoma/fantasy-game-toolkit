package it.fantasytoolkit.dicelauncher.result;

import java.util.List;

public record DiceRoll(int numberOfDice, int numberOfFaces, String code, List<Integer> results, int subtotal) {
}
