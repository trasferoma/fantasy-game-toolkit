package it.fantasytoolkit.buffdebuffgenerator;

import java.util.List;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffDebuffResult;
import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.rules.BuffCombination;
import it.fantasytoolkit.buffdebuffgenerator.rules.BuffDebuffRules;
import it.fantasytoolkit.buffdebuffgenerator.rules.DefaultBuffDebuffRules;
import it.fantasytoolkitcore.core.model.Rarity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BuffDebuffGeneratorToolTest {

    private static final int ITERATIONS = 200;

    @Test
    void simpleCall() {
        System.out.println("==== Common");
        for (int i = 0; i < 5; i++) {
            BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                    .rarity(Rarity.COMMON)
                    .generate();
            System.out.println(result);
        }

        System.out.println("==== Rare");
        for (int i = 0; i < 5; i++) {
            BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                    .rarity(Rarity.RARE)
                    .generate();
            System.out.println(result);
        }

        System.out.println("==== LEGENDARY");
        for (int i = 0; i < 5; i++) {
            BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                    .rarity(Rarity.LEGENDARY)
                    .generate();
            System.out.println(result);
        }
    }

    @Test
    void generateWithoutRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> BuffDebuffGeneratorTool.building().generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Rarity must be set before generating buffs and debuffs");
    }

    @Test
    void generatesCommonAlwaysWithExactlyOneBuffInExpectedRange() {
        for (int i = 0; i < ITERATIONS; i++) {
            BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                    .rarity(Rarity.COMMON)
                    .generate();

            assertThat(result.buffs()).hasSize(1);
            assertThat(result.buffs().getFirst().value()).isBetween(1, 2);
            assertThat(result.debuffs()).isEmpty();
        }
    }

    @Test
    void generatesUncommonBuffsMatchingOneOfTheAllowedCombinations() {
        assertGeneratedResultMatchesRules(Rarity.UNCOMMON);
    }

    @Test
    void generatesRareBuffsMatchingOneOfTheAllowedCombinations() {
        assertGeneratedResultMatchesRules(Rarity.RARE);
    }

    @Test
    void generatesEpicBuffsMatchingOneOfTheAllowedCombinations() {
        assertGeneratedResultMatchesRules(Rarity.EPIC);
    }

    @Test
    void generatesLegendaryBuffsMatchingOneOfTheAllowedCombinations() {
        assertGeneratedResultMatchesRules(Rarity.LEGENDARY);
    }

    @Test
    void generatesUsingCustomRules() {
        BuffDebuffRules customRules = rarity -> List.of(new BuffCombination(2, 10, 10));

        BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                .rarity(Rarity.COMMON)
                .rules(customRules)
                .generate();

        assertThat(result.buffs()).hasSize(2);
        assertThat(result.buffs()).allSatisfy(buff -> assertThat(buff.value()).isEqualTo(10));
        assertThat(result.debuffs()).isEmpty();
    }

    private void assertGeneratedResultMatchesRules(Rarity rarity) {
        List<BuffCombination> allowedCombinations = new DefaultBuffDebuffRules().combinationsFor(rarity);

        for (int i = 0; i < ITERATIONS; i++) {
            BuffDebuffResult result = BuffDebuffGeneratorTool.building()
                    .rarity(rarity)
                    .generate();

            assertThat(result.debuffs()).isEmpty();
            assertMatchesSomeCombination(result.buffs(), allowedCombinations);
            assertHasDistinctCharacteristics(result.buffs());
        }
    }

    private void assertMatchesSomeCombination(List<BuffElement> buffs, List<BuffCombination> allowedCombinations) {
        BuffCombination matchingCombination = allowedCombinations.stream()
                .filter(combination -> combination.count() == buffs.size())
                .findFirst()
                .orElse(null);

        assertThat(matchingCombination)
                .as("a combination with count %d among %s", buffs.size(), allowedCombinations)
                .isNotNull();

        for (BuffElement buff : buffs) {
            assertThat(buff.value()).isBetween(matchingCombination.minValue(), matchingCombination.maxValue());
        }
    }

    private void assertHasDistinctCharacteristics(List<BuffElement> buffs) {
        long distinctCount = buffs.stream()
                .map(BuffElement::characteristic)
                .distinct()
                .count();

        assertThat(distinctCount).isEqualTo(buffs.size());
    }
}
