package it.fantasytoolkitcore.core.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassBonusTable {

    private final Map<CharacterClass, List<CharacteristicBonus>> bonusesByClass;

    private ClassBonusTable(Map<CharacterClass, List<CharacteristicBonus>> bonusesByClass) {
        this.bonusesByClass = bonusesByClass;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ClassBonusTable withDefaultBonuses() {
        return builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 2)
                .bonus(CharacterClass.WARRIOR, Characteristic.STAMINA, 1)
                .bonus(CharacterClass.MAGE, Characteristic.INTELLIGENCE, 2)
                .bonus(CharacterClass.MAGE, Characteristic.CHARISMA, 1)
                .bonus(CharacterClass.THIEF, Characteristic.AGILITY, 2)
                .bonus(CharacterClass.THIEF, Characteristic.LUCK, 1)
                .bonus(CharacterClass.RANGER, Characteristic.AGILITY, 2)
                .bonus(CharacterClass.RANGER, Characteristic.RESISTANCE, 1)
                .build();
    }

    public List<CharacteristicBonus> bonusesFor(CharacterClass characterClass) {
        return bonusesByClass.getOrDefault(characterClass, List.of());
    }

    public record CharacteristicBonus(Characteristic characteristic, int value) {
    }

    private record Entry(CharacterClass characterClass, Characteristic characteristic, int value) {
    }

    private record ClassCharacteristicKey(CharacterClass characterClass, Characteristic characteristic) {
    }

    public static final class Builder {

        private final List<Entry> entries = new ArrayList<>();

        private Builder() {
        }

        public Builder bonus(CharacterClass characterClass, Characteristic characteristic, int value) {
            entries.add(new Entry(characterClass, characteristic, value));
            return this;
        }

        public ClassBonusTable build() {
            validateValuesArePositive();
            validateNoDuplicatePairs();

            return new ClassBonusTable(groupByClass());
        }

        private void validateValuesArePositive() {
            for (Entry entry : entries) {
                if (entry.value() <= 0) {
                    throw new IllegalStateException(
                            "Class bonus value must be positive but was: " + entry.value()
                                    + " for: " + entry.characterClass() + " " + entry.characteristic());
                }
            }
        }

        private void validateNoDuplicatePairs() {
            Set<ClassCharacteristicKey> seenPairs = new HashSet<>();
            for (Entry entry : entries) {
                ClassCharacteristicKey key = new ClassCharacteristicKey(entry.characterClass(),
                        entry.characteristic());
                if (!seenPairs.add(key)) {
                    throw new IllegalStateException(
                            "Duplicate class bonus in table for: " + entry.characterClass() + " "
                                    + entry.characteristic());
                }
            }
        }

        private Map<CharacterClass, List<CharacteristicBonus>> groupByClass() {
            return entries.stream()
                    .collect(Collectors.groupingBy(Entry::characterClass, () -> new EnumMap<>(CharacterClass.class),
                            Collectors.mapping(Builder::toCharacteristicBonus, Collectors.toUnmodifiableList())));
        }

        private static CharacteristicBonus toCharacteristicBonus(Entry entry) {
            return new CharacteristicBonus(entry.characteristic(), entry.value());
        }
    }
}
