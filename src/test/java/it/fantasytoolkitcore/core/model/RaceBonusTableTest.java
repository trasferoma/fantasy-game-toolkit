package it.fantasytoolkitcore.core.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RaceBonusTableTest {

    @Test
    void buildsTableWithMultipleRacesAndBonuses() {
        RaceBonusTable table = RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 1)
                .bonus(Race.ELF, Characteristic.AGILITY, 2)
                .build();

        assertThat(table).isNotNull();
    }

    @Test
    void emptyTableBuildsSuccessfullyAndHasNoBonusesForAnyRace() {
        RaceBonusTable table = RaceBonusTable.builder().build();

        for (Race race : Race.values()) {
            assertThat(table.bonusesFor(race)).isEmpty();
        }
    }

    @Test
    void bonusesForReturnsConfiguredBonusesForARace() {
        RaceBonusTable table = RaceBonusTable.builder()
                .bonus(Race.ORC, Characteristic.STRENGTH, 2)
                .bonus(Race.ORC, Characteristic.RESISTANCE, 1)
                .build();

        assertThat(table.bonusesFor(Race.ORC)).containsExactlyInAnyOrder(
                new RaceBonusTable.CharacteristicBonus(Characteristic.STRENGTH, 2),
                new RaceBonusTable.CharacteristicBonus(Characteristic.RESISTANCE, 1));
    }

    @Test
    void bonusesForReturnsEmptyListForARaceWithNoEntries() {
        RaceBonusTable table = RaceBonusTable.builder()
                .bonus(Race.ORC, Characteristic.STRENGTH, 2)
                .build();

        assertThat(table.bonusesFor(Race.ELF)).isEmpty();
    }

    @Test
    void buildThrowsWhenValueIsZero() {
        assertThatThrownBy(() -> RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 0)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenValueIsNegative() {
        assertThatThrownBy(() -> RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, -1)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenRaceAndCharacteristicPairIsDuplicated() {
        assertThatThrownBy(() -> RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 1)
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 2)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void withDefaultBonusesMatchesTheDocumentedDefaultsForEachRace() {
        RaceBonusTable table = RaceBonusTable.withDefaultBonuses();

        assertThat(table.bonusesFor(Race.HUMAN)).containsExactlyInAnyOrderElementsOf(List.of(
                new RaceBonusTable.CharacteristicBonus(Characteristic.STRENGTH, 1),
                new RaceBonusTable.CharacteristicBonus(Characteristic.AGILITY, 1),
                new RaceBonusTable.CharacteristicBonus(Characteristic.INTELLIGENCE, 1)));

        assertThat(table.bonusesFor(Race.ELF)).containsExactlyInAnyOrderElementsOf(List.of(
                new RaceBonusTable.CharacteristicBonus(Characteristic.AGILITY, 2),
                new RaceBonusTable.CharacteristicBonus(Characteristic.INTELLIGENCE, 1)));

        assertThat(table.bonusesFor(Race.ORC)).containsExactlyInAnyOrderElementsOf(List.of(
                new RaceBonusTable.CharacteristicBonus(Characteristic.STRENGTH, 2),
                new RaceBonusTable.CharacteristicBonus(Characteristic.RESISTANCE, 1)));

        assertThat(table.bonusesFor(Race.UNDEAD)).containsExactlyInAnyOrderElementsOf(List.of(
                new RaceBonusTable.CharacteristicBonus(Characteristic.RESISTANCE, 2),
                new RaceBonusTable.CharacteristicBonus(Characteristic.STAMINA, 1)));
    }
}
