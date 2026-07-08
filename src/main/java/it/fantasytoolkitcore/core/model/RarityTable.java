package it.fantasytoolkitcore.core.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public final class RarityTable {

    private static final int TOTAL_WEIGHT = 100;

    private final List<Entry> entries;

    private RarityTable(List<Entry> entries) {
        this.entries = entries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Rarity draw(Random random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulativeWeight = 0;

        for (Entry entry : entries) {
            cumulativeWeight += entry.weight();
            if (roll < cumulativeWeight) {
                return entry.rarity();
            }
        }

        throw new IllegalStateException("No rarity could be drawn for roll: " + roll);
    }

    private record Entry(Rarity rarity, int weight) {
    }

    public static final class Builder {

        private final List<Entry> entries = new ArrayList<>();

        private Builder() {
        }

        public Builder entry(Rarity rarity, int weight) {
            entries.add(new Entry(rarity, weight));
            return this;
        }

        public RarityTable build() {
            validateWeightsArePositive();
            validateRaritiesAreUnique();
            validateWeightsSumToTotal();

            return new RarityTable(List.copyOf(entries));
        }

        private void validateWeightsArePositive() {
            for (Entry entry : entries) {
                if (entry.weight() <= 0) {
                    throw new IllegalStateException(
                            "Rarity weight must be positive but was: " + entry.weight() + " for: " + entry.rarity());
                }
            }
        }

        private void validateRaritiesAreUnique() {
            EnumSet<Rarity> seenRarities = EnumSet.noneOf(Rarity.class);
            for (Entry entry : entries) {
                if (!seenRarities.add(entry.rarity())) {
                    throw new IllegalStateException("Duplicate rarity in table: " + entry.rarity());
                }
            }
        }

        private void validateWeightsSumToTotal() {
            int totalWeight = entries.stream()
                    .mapToInt(Entry::weight)
                    .sum();
            if (totalWeight != TOTAL_WEIGHT) {
                throw new IllegalStateException("Rarity weights must sum to 100 but was: " + totalWeight);
            }
        }
    }
}
