package it.fantasytoolkit.dicelauncher.result;

import java.util.List;

import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record DiceRollResult(List<DiceRoll> rolls, int total) implements GeneratedElementResult {
    public static DiceRollResult.Builder builder() {
        return new DiceRollResult.Builder();
    }

    public static final class Builder {
        private List<DiceRoll> rolls;
        private int total;

        private Builder() {
        }

        public DiceRollResult.Builder rolls(List<DiceRoll> rolls) {
            this.rolls = rolls;
            return this;
        }

        public DiceRollResult.Builder total(int total) {
            this.total = total;
            return this;
        }

        public DiceRollResult build() {
            return new DiceRollResult(rolls, total);
        }
    }
}
