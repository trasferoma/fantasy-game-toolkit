package it.fantasytoolkit.potiongenerator;

import it.fantasytoolkit.potiongenerator.result.PotionResult;
import it.fantasytoolkit.potiongenerator.rules.DefaultPotionRules;
import it.fantasytoolkit.potiongenerator.rules.PotionRules;
import it.fantasytoolkit.potiongenerator.rules.RegenerationRange;
import it.fantasytoolkitcore.core.model.PotionType;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PotionGeneratorToolTest {

    private static final int ITERATIONS = 50;

    @Test
    void generatesBuffPotionWithBuffAndNoDebuff() {
        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.BUFF)
                .rarity(Rarity.COMMON)
                .generate();

        assertThat(result.type()).isEqualTo(PotionType.BUFF);
        assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        assertThat(result.value()).isZero();
        assertThat(result.debuff()).isNull();
        assertThat(result.buff()).isNotNull();
        assertThat(result.buff().characteristic()).isNotNull();
        assertThat(result.buff().value()).isBetween(1, 2);
    }

    @Test
    void generatesDebuffPotionWithDebuffAndNoBuff() {
        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.DEBUFF)
                .rarity(Rarity.COMMON)
                .generate();

        assertThat(result.type()).isEqualTo(PotionType.DEBUFF);
        assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        assertThat(result.value()).isZero();
        assertThat(result.buff()).isNull();
        assertThat(result.debuff()).isNotNull();
        assertThat(result.debuff().characteristic()).isNotNull();
        assertThat(result.debuff().value()).isBetween(1, 2);
    }

    @Test
    void generatesHealthRegenerationPotionWithinDefaultRange() {
        RegenerationRange range = new DefaultPotionRules().regenerationFor(Rarity.COMMON);

        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.HEALTH_REGENERATION)
                .rarity(Rarity.COMMON)
                .generate();

        assertThat(result.type()).isEqualTo(PotionType.HEALTH_REGENERATION);
        assertThat(result.value()).isBetween(range.minValue(), range.maxValue());
        assertThat(result.buff()).isNull();
        assertThat(result.debuff()).isNull();
    }

    @Test
    void generatesManaRegenerationPotionWithinDefaultRange() {
        RegenerationRange range = new DefaultPotionRules().regenerationFor(Rarity.EPIC);

        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.MANA_REGENERATION)
                .rarity(Rarity.EPIC)
                .generate();

        assertThat(result.type()).isEqualTo(PotionType.MANA_REGENERATION);
        assertThat(result.value()).isBetween(range.minValue(), range.maxValue());
        assertThat(result.buff()).isNull();
        assertThat(result.debuff()).isNull();
    }

    @Test
    void generatesRandomTypeAcrossAllValuesConsistently() {
        for (int i = 0; i < ITERATIONS; i++) {
            PotionResult result = PotionGeneratorTool.building()
                    .randomType()
                    .randomRarity()
                    .generate();

            assertThat(result.type()).isIn((Object[]) PotionType.values());
            assertThat(result.rarity()).isNotNull().isIn((Object[]) Rarity.values());

            switch (result.type()) {
                case BUFF -> {
                    assertThat(result.buff()).isNotNull();
                    assertThat(result.debuff()).isNull();
                }
                case DEBUFF -> {
                    assertThat(result.debuff()).isNotNull();
                    assertThat(result.buff()).isNull();
                }
                case HEALTH_REGENERATION, MANA_REGENERATION -> {
                    assertThat(result.value()).isPositive();
                    assertThat(result.buff()).isNull();
                    assertThat(result.debuff()).isNull();
                }
            }
        }
    }

    @Test
    void generatesPotionWithMaxRarityCommonAlwaysReturnsCommon() {
        for (int i = 0; i < ITERATIONS; i++) {
            PotionResult result = PotionGeneratorTool.building()
                    .type(PotionType.HEALTH_REGENERATION)
                    .maxRarity(Rarity.COMMON)
                    .generate();

            assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void generatesPotionUsingRarityTable() {
        RarityTable rarityTable = RarityTable.builder()
                .entry(Rarity.LEGENDARY, 100)
                .build();

        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.HEALTH_REGENERATION)
                .rarityTable(rarityTable)
                .generate();

        assertThat(result.rarity()).isEqualTo(Rarity.LEGENDARY);
    }

    @Test
    void generatesPotionUsingRandomRarityStaysWithinExpectedValues() {
        for (int i = 0; i < ITERATIONS; i++) {
            PotionResult result = PotionGeneratorTool.building()
                    .type(PotionType.MANA_REGENERATION)
                    .randomRarity()
                    .generate();

            assertThat(result.rarity()).isNotNull().isIn((Object[]) Rarity.values());
        }
    }

    @Test
    void generateWithoutTypeThrowsIllegalStateException() {
        assertThatThrownBy(() -> PotionGeneratorTool.building()
                .rarity(Rarity.RARE)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Type must be set before generating a potion");
    }

    @Test
    void generateWithBothTypeAndRandomTypeThrowsIllegalStateException() {
        assertThatThrownBy(() -> PotionGeneratorTool.building()
                .type(PotionType.BUFF)
                .randomType()
                .rarity(Rarity.COMMON)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of type or randomType can be used together");
    }

    @Test
    void generateWithoutRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> PotionGeneratorTool.building()
                .type(PotionType.BUFF)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a potion");
    }

    @Test
    void generateWithBothRarityAndRandomRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> PotionGeneratorTool.building()
                .type(PotionType.BUFF)
                .rarity(Rarity.COMMON)
                .randomRarity()
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of rarity, maxRarity, rarityTable or randomRarity can be used together");
    }

    @Test
    void generatesUsingCustomRules() {
        PotionRules customRules = rarity -> new RegenerationRange(100, 100);

        PotionResult result = PotionGeneratorTool.building()
                .type(PotionType.HEALTH_REGENERATION)
                .rarity(Rarity.COMMON)
                .rules(customRules)
                .generate();

        assertThat(result.value()).isEqualTo(100);
    }
}
