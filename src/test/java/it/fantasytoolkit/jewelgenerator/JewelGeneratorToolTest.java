package it.fantasytoolkit.jewelgenerator;

import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.jewelgenerator.result.JewelResult;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JewelGeneratorToolTest {
    @Test
    void simpleCall() {
        for (int i = 0; i < 10; i++) {
            JewelResult result = JewelGeneratorTool
                    .building()
                    .jewel(Jewel.RING)
                    .rarity(Rarity.RARE)
                    .generate();

            System.out.println(result);
        }
    }

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

    @Test
    void generatesRandomJewelTypeWithoutRarity() {
        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a jewel");
    }

    @Test
    void generatesRandomJewelTypeWithoutJewel() {
        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .maxRarity(Rarity.RARE)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Jewel must be set before generating a jewel");
    }

    @Test
    void generatesRandomJewelType() {
        JewelResult result = JewelGeneratorTool.building()
                .randomJewel()
                .rarity(Rarity.RARE)
                .generate();

        Jewel jewel = result.jewel();

        boolean found = Stream.of(Jewel.values()).
                anyMatch(j -> j == jewel);

        assertThat(found).isTrue();
    }

    @Test
    void generatesJewelWithRandomJewelAndRandomRarity() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .randomJewel()
                    .randomRarity()
                    .generate();

            assertThat(result.jewel()).isIn((Object[]) Jewel.values());
            assertThat(result.rarity()).isNotNull().isIn((Object[]) Rarity.values());

            assertThat(result.buffs()).isNotEmpty();
            assertThat(result.debuffs()).isNotNull();
        }
    }

    @Test
    void generateWithBothJewelAndRandomJewelThrowsIllegalStateException() {
        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .randomJewel()
                .rarity(Rarity.COMMON)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of jewel or randomJewel can be used together");
    }

    @Test
    void generateWithBothRarityAndRandomRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarity(Rarity.COMMON)
                .randomRarity()
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of rarity, maxRarity, rarityTable or randomRarity can be used together");
    }

    @Test
    void generatesJewelWithRandomRarityStaysWithinFullRange() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .randomRarity()
                    .generate();

            assertThat(result.jewel()).isEqualTo(Jewel.RING);
            assertThat(result.rarity()).isNotNull();
            assertThat(result.rarity().ordinal()).isBetween(0, Rarity.LEGENDARY.ordinal());
        }
    }

    @Test
    void generatesJewel() {
        JewelResult result = JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarity(Rarity.RARE)
                .generate();

        System.out.println(result);
    }

    @Test
    void generatesJewelWithRarityTable() {
        RarityTable table = RarityTable.builder()
                .entry(Rarity.COMMON, 60)
                .entry(Rarity.UNCOMMON, 20)
                .entry(Rarity.RARE, 19)
                .entry(Rarity.LEGENDARY, 1)
                .build();

        JewelResult result = JewelGeneratorTool.building()
                .jewel(Jewel.RING)
                .rarityTable(table)
                .generate();

        System.out.println(result);
    }

    @Test
    void generatedCommonJewelAlwaysHasExactlyOneBuffInDefaultRange() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .rarity(Rarity.COMMON)
                    .generate();

            assertThat(result.buffs()).hasSize(1);

            BuffElement buff = result.buffs().getFirst();
            assertThat(buff.characteristic()).isNotNull();
            assertThat(buff.value()).isBetween(1, 2);

            assertThat(result.debuffs()).isNotNull().isEmpty();
        }
    }

    @Test
    void generatedJewelBuffCharacteristicsAreDistinct() {
        for (int i = 0; i < 50; i++) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .rarity(Rarity.LEGENDARY)
                    .generate();

            assertDistinctBuffCharacteristics(result.buffs());
        }
    }

    @Test
    void generatedJewelExposesNonEmptyBuffsForEveryRarity() {
        for (Rarity rarity : Rarity.values()) {
            JewelResult result = JewelGeneratorTool.building()
                    .jewel(Jewel.RING)
                    .rarity(rarity)
                    .generate();

            assertThat(result.buffs()).isNotEmpty();
            assertThat(result.debuffs()).isNotNull();
        }
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

        assertThat(result.buffs()).isNotNull().isNotEmpty();
        assertThat(result.debuffs()).isNotNull();

        assertDistinctBuffCharacteristics(result.buffs());
    }

    private void assertDistinctBuffCharacteristics(List<BuffElement> buffs) {
        Set<Characteristic> distinctCharacteristics = new HashSet<>();
        for (BuffElement buff : buffs) {
            assertThat(buff.characteristic()).isNotNull();
            assertThat(buff.value()).isPositive();
            distinctCharacteristics.add(buff.characteristic());
        }
        assertThat(distinctCharacteristics).hasSameSizeAs(buffs);
    }
}
