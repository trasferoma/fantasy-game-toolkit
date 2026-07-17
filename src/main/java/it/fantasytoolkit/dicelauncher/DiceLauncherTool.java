package it.fantasytoolkit.dicelauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.fantasytoolkit.dicelauncher.result.DiceRoll;
import it.fantasytoolkit.dicelauncher.result.DiceRollResult;

public final class DiceLauncherTool {

    private DiceLauncherTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private final List<DiceGroupSpec> groups = new ArrayList<>();

        private Builder() {
        }

        public Builder dice(int numberOfDice, int numberOfFaces) {
            return dice(numberOfDice, numberOfFaces, null);
        }

        public Builder dice(int numberOfDice, int numberOfFaces, String code) {
            validateDiceGroup(numberOfDice, numberOfFaces);
            groups.add(new DiceGroupSpec(numberOfDice, numberOfFaces, code));
            return this;
        }

        public DiceRollResult roll() {
            validateGroups();

            Random random = new Random();
            List<DiceRoll> rolls = new ArrayList<>();
            int total = 0;

            for (DiceGroupSpec group : groups) {
                DiceRoll diceRoll = rollGroup(group, random);
                rolls.add(diceRoll);
                total += diceRoll.subtotal();
            }

            return DiceRollResult.builder()
                    .rolls(List.copyOf(rolls))
                    .total(total)
                    .build();
        }

        private DiceRoll rollGroup(DiceGroupSpec group, Random random) {
            List<Integer> results = new ArrayList<>();
            int subtotal = 0;

            for (int i = 0; i < group.numberOfDice(); i++) {
                int value = 1 + random.nextInt(group.numberOfFaces());
                results.add(value);
                subtotal += value;
            }

            return new DiceRoll(group.numberOfDice(), group.numberOfFaces(), group.code(), List.copyOf(results),
                    subtotal);
        }

        private void validateDiceGroup(int numberOfDice, int numberOfFaces) {
            if (numberOfDice < 1) {
                throw new IllegalArgumentException("numberOfDice must be at least 1");
            }
            if (numberOfFaces < 2) {
                throw new IllegalArgumentException("numberOfFaces must be at least 2");
            }
        }

        private void validateGroups() {
            if (groups.isEmpty()) {
                throw new IllegalStateException("At least one dice group must be added before rolling");
            }
        }

        private record DiceGroupSpec(int numberOfDice, int numberOfFaces, String code) {
        }
    }
}
