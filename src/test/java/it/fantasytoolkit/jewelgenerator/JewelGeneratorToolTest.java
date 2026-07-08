package it.fantasytoolkit.jewelgenerator;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.jewelgenerator.result.JewelResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JewelGeneratorToolTest {

    @Test
    void generatesJewelWithRequestedJewelAndRarity() {
        assertGeneratedJewelMatches(Jewel.RING, Rarity.COMMON);
    }

    @Test
    void generatesJewelWithDifferentJewelAndRarityCombination() {
        assertGeneratedJewelMatches(Jewel.NECKLACE, Rarity.LEGENDARY);
    }

    @Test
    void generatesJewelWithAnotherJewelAndRarityCombination() {
        assertGeneratedJewelMatches(Jewel.EARRING, Rarity.EPIC);
    }

    @Test
    void generateWithoutRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> JewelGeneratorTool.building().jewel(Jewel.RING).generate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateWithBothRarityAndMaxRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarity(Rarity.COMMON)
                .maxRarity(Rarity.RARE)
                .generate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generatesJewelWithMaxRarityCommonAlwaysReturnsCommon() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .maxRarity(Rarity.COMMON)
                    .generate();

            assertThat(result.jewel()).isEqualTo(Jewel.RING);
            assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void generatesJewelWithMaxRarityRareStaysWithinExpectedRange() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.NECKLACE)
                    .maxRarity(Rarity.RARE)
                    .generate();

            assertThat(result.jewel()).isEqualTo(Jewel.NECKLACE);
            assertThat(result.rarity()).isNotNull();
            assertThat(result.rarity().ordinal()).isLessThanOrEqualTo(Rarity.RARE.ordinal());
        }
    }

    @Test
    void generatesJewelWithMaxRarityLegendaryStaysWithinFullRange() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.EARRING)
                    .maxRarity(Rarity.LEGENDARY)
                    .generate();

            assertThat(result.jewel()).isEqualTo(Jewel.EARRING);
            assertThat(result.rarity()).isNotNull();
            assertThat(result.rarity().ordinal()).isLessThanOrEqualTo(Rarity.LEGENDARY.ordinal());
        }
    }

    @Test
    void generatesJewelWithRarityTableCommonAlwaysReturnsCommon() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 100)
                .build();

        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .rarityTable(table)
                    .generate();

            assertThat(result.jewel()).isEqualTo(Jewel.RING);
            assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void generateWithBothRarityAndRarityTableThrowsIllegalStateException() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 100)
                .build();

        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarity(Rarity.COMMON)
                .rarityTable(table)
                .generate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateWithBothMaxRarityAndRarityTableThrowsIllegalStateException() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 100)
                .build();

        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .maxRarity(Rarity.RARE)
                .rarityTable(table)
                .generate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateWithAllThreeRaritySourcesThrowsIllegalStateException() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 100)
                .build();

        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarity(Rarity.COMMON)
                .maxRarity(Rarity.RARE)
                .rarityTable(table)
                .generate())
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertGeneratedJewelMatches(Jewel jewel, Rarity rarity) {
        JewelResult result = JewelGeneratorTool
                .building()
                .jewel(jewel)
                .rarity(rarity)
                .generate();

        assertThat(result).isNotNull();
        assertThat(result.jewel()).isEqualTo(jewel);
        assertThat(result.rarity()).isEqualTo(rarity);
    }
}
