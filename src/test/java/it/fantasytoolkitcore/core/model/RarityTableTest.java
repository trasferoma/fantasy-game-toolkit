package it.fantasytoolkitcore.core.model;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RarityTableTest {

    @Test
    void buildsTableWhenWeightsSumToOneHundred() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 70)
                .entry(Rarity.UNCOMMON, 25)
                .entry(Rarity.RARE, 5)
                .build();

        assertThat(table).isNotNull();
    }

    @Test
    void buildThrowsWhenWeightsSumBelowOneHundred() {
        assertThatThrownBy(() -> RarityTable.builder()
                .entry(Rarity.COMMON, 60)
                .entry(Rarity.UNCOMMON, 30)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenWeightsSumAboveOneHundred() {
        assertThatThrownBy(() -> RarityTable.builder()
                .entry(Rarity.COMMON, 60)
                .entry(Rarity.UNCOMMON, 50)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenWeightIsNegative() {
        assertThatThrownBy(() -> RarityTable.builder()
                .entry(Rarity.COMMON, -10)
                .entry(Rarity.UNCOMMON, 110)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenWeightIsZero() {
        assertThatThrownBy(() -> RarityTable.builder()
                .entry(Rarity.COMMON, 0)
                .entry(Rarity.UNCOMMON, 100)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildThrowsWhenRarityIsDuplicated() {
        assertThatThrownBy(() -> RarityTable.builder()
                .entry(Rarity.COMMON, 50)
                .entry(Rarity.COMMON, 50)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawAlwaysReturnsTheOnlyEntry() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 100)
                .build();
        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            assertThat(table.draw(random)).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void drawOnlyReturnsConfiguredEntriesAndCoversBoth() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 50)
                .entry(Rarity.LEGENDARY, 50)
                .build();
        Random random = new Random();
        Set<Rarity> drawnRarities = EnumSet.noneOf(Rarity.class);

        for (int i = 0; i < 200; i++) {
            Rarity drawn = table.draw(random);
            assertThat(drawn).isIn(Rarity.COMMON, Rarity.LEGENDARY);
            drawnRarities.add(drawn);
        }

        assertThat(drawnRarities).containsExactlyInAnyOrder(Rarity.COMMON, Rarity.LEGENDARY);
    }
}
