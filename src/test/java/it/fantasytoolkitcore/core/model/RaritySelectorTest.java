package it.fantasytoolkitcore.core.model;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import it.fantasytoolkitcore.core.tool.RaritySelector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RaritySelectorTest {

    @Test
    void selectReturnsNonNullRarity() {
        RaritySelector selector = RaritySelector.withDefaultDistribution();

        Rarity selected = selector.select(new Random(0L));

        assertThat(selected).isNotNull();
    }

    @Test
    void selectIsDeterministicForTheSameSeed() {
        RaritySelector firstSelector = RaritySelector.withDefaultDistribution();
        RaritySelector secondSelector = RaritySelector.withDefaultDistribution();

        Rarity firstSelected = firstSelector.select(new Random(42L));
        Rarity secondSelected = secondSelector.select(new Random(42L));

        assertThat(firstSelected).isEqualTo(secondSelected);
    }

    @Test
    void selectOnlyReturnsKnownRaritiesAndCoversCommon() {
        RaritySelector selector = RaritySelector.withDefaultDistribution();
        Random random = new Random(1L);
        Set<Rarity> selectedRarities = EnumSet.noneOf(Rarity.class);

        for (int i = 0; i < 1000; i++) {
            selectedRarities.add(selector.select(random));
        }

        assertThat(selectedRarities).isSubsetOf(Rarity.values());
        assertThat(selectedRarities).contains(Rarity.COMMON);
    }
}
