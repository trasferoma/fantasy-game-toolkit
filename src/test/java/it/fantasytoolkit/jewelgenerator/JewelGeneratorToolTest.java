package it.fantasytoolkit.jewelgenerator;

import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
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
        assertThatThrownBy(() -> JewelGeneratorTool.jewel(Jewel.RING).generate())
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertGeneratedJewelMatches(Jewel jewel, Rarity rarity) {
        JewelResult result = JewelGeneratorTool
                .rarity(rarity)
                .jewel(jewel)
                .generate();

        assertThat(result).isNotNull();
        assertThat(result.jewel()).isEqualTo(jewel);
        assertThat(result.rarity()).isEqualTo(rarity);
    }
}
