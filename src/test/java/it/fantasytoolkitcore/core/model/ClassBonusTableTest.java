package it.fantasytoolkitcore.core.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClassBonusTableTest {

    @Test
    void buildsTableWithMultipleClassesAndBonuses() {
        ClassBonusTable table = ClassBonusTable.builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 1)
                .bonus(CharacterClass.MAGE, Characteristic.INTELLIGENCE, 2)
                .build();

        assertThat(table).isNotNull();
    }

    @Test
    void emptyTableBuildsSuccessfullyAndHasNoBonusesForAnyClass() {
        ClassBonusTable table = ClassBonusTable.builder().build();

        for (CharacterClass characterClass : CharacterClass.values()) {
            assertThat(table.bonusesFor(characterClass)).isEmpty();
        }
    }

    @Test
    void bonusesForReturnsConfiguredBonusesForAClass() {
        ClassBonusTable table = ClassBonusTable.builder()
                .bonus(CharacterClass.THIEF, Characteristic.AGILITY, 2)
                .bonus(CharacterClass.THIEF, Characteristic.LUCK, 1)
                .build();

        assertThat(table.bonusesFor(CharacterClass.THIEF)).containsExactlyInAnyOrder(
                new ClassBonusTable.CharacteristicBonus(Characteristic.AGILITY, 2),
                new ClassBonusTable.CharacteristicBonus(Characteristic.LUCK, 1));
    }

    @Test
    void bonusesForReturnsEmptyListForAClassWithNoEntries() {
        ClassBonusTable table = ClassBonusTable.builder()
                .bonus(CharacterClass.THIEF, Characteristic.AGILITY, 2)
                .build();

        assertThat(table.bonusesFor(CharacterClass.MAGE)).isEmpty();
    }

    @Test
    void buildThrowsWhenValueIsZero() {
        assertThatThrownBy(() -> ClassBonusTable.builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 0)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenValueIsNegative() {
        assertThatThrownBy(() -> ClassBonusTable.builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, -1)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenClassAndCharacteristicPairIsDuplicated() {
        assertThatThrownBy(() -> ClassBonusTable.builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 1)
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 2)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void withDefaultBonusesMatchesTheDocumentedDefaultsForEachClass() {
        ClassBonusTable table = ClassBonusTable.withDefaultBonuses();

        assertThat(table.bonusesFor(CharacterClass.WARRIOR)).containsExactlyInAnyOrderElementsOf(List.of(
                new ClassBonusTable.CharacteristicBonus(Characteristic.STRENGTH, 2),
                new ClassBonusTable.CharacteristicBonus(Characteristic.STAMINA, 1)));

        assertThat(table.bonusesFor(CharacterClass.MAGE)).containsExactlyInAnyOrderElementsOf(List.of(
                new ClassBonusTable.CharacteristicBonus(Characteristic.INTELLIGENCE, 2),
                new ClassBonusTable.CharacteristicBonus(Characteristic.CHARISMA, 1)));

        assertThat(table.bonusesFor(CharacterClass.THIEF)).containsExactlyInAnyOrderElementsOf(List.of(
                new ClassBonusTable.CharacteristicBonus(Characteristic.AGILITY, 2),
                new ClassBonusTable.CharacteristicBonus(Characteristic.LUCK, 1)));

        assertThat(table.bonusesFor(CharacterClass.RANGER)).containsExactlyInAnyOrderElementsOf(List.of(
                new ClassBonusTable.CharacteristicBonus(Characteristic.AGILITY, 2),
                new ClassBonusTable.CharacteristicBonus(Characteristic.RESISTANCE, 1)));
    }
}
