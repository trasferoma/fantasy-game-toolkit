package it.fantasytoolkit.armourgenerator;

import it.fantasytoolkitcore.core.model.Armour;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkit.armourgenerator.result.ArmourResult;
import it.fantasytoolkit.armourgenerator.rules.ArmourRules;
import it.fantasytoolkit.armourgenerator.rules.DefaultArmourRules;
import it.fantasytoolkit.armourgenerator.rules.DefenseRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArmourGeneratorToolTest {

    private static final int ITERATIONS = 50;

    @Test
    void simpleGeneration() {
        for (int i = 0; i < ITERATIONS; i++) {
            ArmourResult result = ArmourGeneratorTool.building()
                    .randomArmour()
                    .randomRarity()
                    .noStatusEffect()
                    .generate();

            System.out.println(result);
        }
    }

    @Test
    void generatesArmourWithRequestedArmourAndRarity() {
        ArmourResult result = ArmourGeneratorTool.building()
                .armour(Armour.HELMET)
                .rarity(Rarity.RARE)
                .generate();

        assertThat(result.armour()).isEqualTo(Armour.HELMET);
        assertThat(result.rarity()).isEqualTo(Rarity.RARE);
        assertThat(result.buffs()).isNotEmpty();
        assertThat(result.debuffs()).isNotNull();
    }

    @Test
    void generatesArmourWithNoStatusEffectHasEmptyBuffsAndDebuffs() {
        ArmourResult result = ArmourGeneratorTool.building()
                .armour(Armour.CHESTPLATE)
                .rarity(Rarity.COMMON)
                .noStatusEffect()
                .generate();

        assertThat(result.armour()).isEqualTo(Armour.CHESTPLATE);
        assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        assertThat(result.buffs()).isNotNull().isEmpty();
        assertThat(result.debuffs()).isNotNull().isEmpty();
    }

    @Test
    void generateWithoutRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> ArmourGeneratorTool.building()
                .armour(Armour.HELMET)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of rarity, maxRarity, rarityTable or randomRarity must be set before generating an armour");
    }

    @Test
    void generateWithoutArmourThrowsIllegalStateException() {
        assertThatThrownBy(() -> ArmourGeneratorTool.building()
                .rarity(Rarity.RARE)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Armour must be set before generating an armour");
    }

    @Test
    void generateWithBothArmourAndRandomArmourThrowsIllegalStateException() {
        assertThatThrownBy(() -> ArmourGeneratorTool.building()
                .armour(Armour.HELMET)
                .randomArmour()
                .rarity(Rarity.COMMON)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of armour or randomArmour can be used together");
    }

    @Test
    void generateWithBothRarityAndRandomRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> ArmourGeneratorTool.building()
                .armour(Armour.HELMET)
                .rarity(Rarity.COMMON)
                .randomRarity()
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of rarity, maxRarity, rarityTable or randomRarity can be used together");
    }

    @Test
    void generatesArmourWithMaxRarityCommonAlwaysReturnsCommon() {
        for (int i = 0; i < 50; i++) {
            ArmourResult result = ArmourGeneratorTool.building()
                    .armour(Armour.BOOTS)
                    .maxRarity(Rarity.COMMON)
                    .generate();

            assertThat(result.armour()).isEqualTo(Armour.BOOTS);
            assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void generatesRandomArmourAndRandomRarityStayWithinExpectedValues() {
        for (int i = 0; i < 50; i++) {
            ArmourResult result = ArmourGeneratorTool.building()
                    .randomArmour()
                    .randomRarity()
                    .generate();

            assertThat(result.armour()).isIn((Object[]) Armour.values());
            assertThat(result.rarity()).isNotNull().isIn((Object[]) Rarity.values());
            assertThat(result.buffs()).isNotEmpty();
            assertThat(result.debuffs()).isNotNull();
        }
    }

    @Test
    void generatesCommonDefenseWithinDefaultRange() {
        assertDefenseWithinDefaultRange(Rarity.COMMON);
    }

    @Test
    void generatesUncommonDefenseWithinDefaultRange() {
        assertDefenseWithinDefaultRange(Rarity.UNCOMMON);
    }

    @Test
    void generatesRareDefenseWithinDefaultRange() {
        assertDefenseWithinDefaultRange(Rarity.RARE);
    }

    @Test
    void generatesEpicDefenseWithinDefaultRange() {
        assertDefenseWithinDefaultRange(Rarity.EPIC);
    }

    @Test
    void generatesLegendaryDefenseWithinDefaultRange() {
        assertDefenseWithinDefaultRange(Rarity.LEGENDARY);
    }

    @Test
    void generatesUsingCustomRules() {
        ArmourRules customRules = rarity -> new DefenseRange(100, 100);

        ArmourResult result = ArmourGeneratorTool.building()
                .armour(Armour.HELMET)
                .rarity(Rarity.COMMON)
                .rules(customRules)
                .generate();

        assertThat(result.defense()).isEqualTo(100);
    }

    private void assertDefenseWithinDefaultRange(Rarity rarity) {
        DefenseRange range = new DefaultArmourRules().defenseFor(rarity);

        for (int i = 0; i < ITERATIONS; i++) {
            ArmourResult result = ArmourGeneratorTool.building()
                    .armour(Armour.HELMET)
                    .rarity(rarity)
                    .generate();

            assertThat(result.defense()).isBetween(range.minValue(), range.maxValue());
        }
    }
}
