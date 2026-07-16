package it.fantasytoolkitcore.core.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class RaceBonusTable {

    private final Map<Race, List<CharacteristicBonus>> bonusesByRace;

    private RaceBonusTable(Map<Race, List<CharacteristicBonus>> bonusesByRace) {
        this.bonusesByRace = bonusesByRace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RaceBonusTable withDefaultBonuses() {
        return builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 1)
                .bonus(Race.HUMAN, Characteristic.AGILITY, 1)
                .bonus(Race.HUMAN, Characteristic.INTELLIGENCE, 1)
                .bonus(Race.ELF, Characteristic.AGILITY, 2)
                .bonus(Race.ELF, Characteristic.INTELLIGENCE, 1)
                .bonus(Race.ORC, Characteristic.STRENGTH, 2)
                .bonus(Race.ORC, Characteristic.RESISTANCE, 1)
                .bonus(Race.UNDEAD, Characteristic.RESISTANCE, 2)
                .bonus(Race.UNDEAD, Characteristic.STAMINA, 1)
                .build();
    }

    public List<CharacteristicBonus> bonusesFor(Race race) {
        return bonusesByRace.getOrDefault(race, List.of());
    }

    public record CharacteristicBonus(Characteristic characteristic, int value) {
    }

    private record Entry(Race race, Characteristic characteristic, int value) {
    }

    private record RaceCharacteristicKey(Race race, Characteristic characteristic) {
    }

    public static final class Builder {

        private final List<Entry> entries = new ArrayList<>();

        private Builder() {
        }

        public Builder bonus(Race race, Characteristic characteristic, int value) {
            entries.add(new Entry(race, characteristic, value));
            return this;
        }

        public RaceBonusTable build() {
            validateValuesArePositive();
            validateNoDuplicatePairs();

            return new RaceBonusTable(groupByRace());
        }

        private void validateValuesArePositive() {
            for (Entry entry : entries) {
                if (entry.value() <= 0) {
                    throw new IllegalStateException(
                            "Race bonus value must be positive but was: " + entry.value()
                                    + " for: " + entry.race() + " " + entry.characteristic());
                }
            }
        }

        private void validateNoDuplicatePairs() {
            Set<RaceCharacteristicKey> seenPairs = new HashSet<>();
            for (Entry entry : entries) {
                RaceCharacteristicKey key = new RaceCharacteristicKey(entry.race(), entry.characteristic());
                if (!seenPairs.add(key)) {
                    throw new IllegalStateException(
                            "Duplicate race bonus in table for: " + entry.race() + " " + entry.characteristic());
                }
            }
        }

        private Map<Race, List<CharacteristicBonus>> groupByRace() {
            return entries.stream()
                    .collect(Collectors.groupingBy(Entry::race, () -> new EnumMap<>(Race.class),
                            Collectors.mapping(Builder::toCharacteristicBonus, Collectors.toUnmodifiableList())));
        }

        private static CharacteristicBonus toCharacteristicBonus(Entry entry) {
            return new CharacteristicBonus(entry.characteristic(), entry.value());
        }
    }
}
