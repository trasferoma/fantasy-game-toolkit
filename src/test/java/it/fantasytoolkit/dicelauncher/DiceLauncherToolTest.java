package it.fantasytoolkit.dicelauncher;

import it.fantasytoolkit.dicelauncher.result.DiceRoll;
import it.fantasytoolkit.dicelauncher.result.DiceRollResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DiceLauncherToolTest {

    @Test
    void twoIdenticalDiceGroupsAreNotMerged() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(2, 6)
                .dice(2, 6)
                .roll();

        System.out.printf("Result: %s", result);

        assertThat(result.rolls()).hasSize(2);
        for (DiceRoll diceRoll : result.rolls()) {
            assertThat(diceRoll.numberOfDice()).isEqualTo(2);
            assertThat(diceRoll.results()).hasSize(2);
        }
    }

    @Test
    void mixedDiceGroupsAreKeptInInsertionOrder() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(2, 6)
                .dice(1, 8)
                .dice(3, 12)
                .roll();

        assertThat(result.rolls()).hasSize(3);

        DiceRoll first = result.rolls().get(0);
        DiceRoll second = result.rolls().get(1);
        DiceRoll third = result.rolls().get(2);

        assertThat(first.numberOfDice()).isEqualTo(2);
        assertThat(first.numberOfFaces()).isEqualTo(6);
        assertThat(second.numberOfDice()).isEqualTo(1);
        assertThat(second.numberOfFaces()).isEqualTo(8);
        assertThat(third.numberOfDice()).isEqualTo(3);
        assertThat(third.numberOfFaces()).isEqualTo(12);
    }

    @Test
    void everyDiceValueIsWithinFacesRange() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(10, 4)
                .dice(10, 20)
                .dice(10, 100)
                .roll();

        for (DiceRoll diceRoll : result.rolls()) {
            for (int value : diceRoll.results()) {
                assertThat(value).isBetween(1, diceRoll.numberOfFaces());
            }
        }
    }

    @Test
    void subtotalAndTotalAreConsistentWithResults() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(3, 6)
                .dice(2, 10)
                .roll();

        int expectedTotal = 0;
        for (DiceRoll diceRoll : result.rolls()) {
            int expectedSubtotal = diceRoll.results().stream().mapToInt(Integer::intValue).sum();
            assertThat(diceRoll.subtotal()).isEqualTo(expectedSubtotal);
            expectedTotal += expectedSubtotal;
        }
        assertThat(result.total()).isEqualTo(expectedTotal);
    }

    @Test
    void diceGroupWithCodeIsAttachedToResultingDiceRoll() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(2, 6, "fire")
                .roll();

        assertThat(result.rolls().getFirst().code()).isEqualTo("fire");

        System.out.println(result);
    }

    @Test
    void diceGroupWithoutCodeHasNullCode() {
        DiceRollResult result = DiceLauncherTool.building()
                .dice(2, 6)
                .roll();

        assertThat(result.rolls().get(0).code()).isNull();
    }

    @Test
    void diceWithZeroNumberOfDiceThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> DiceLauncherTool.building().dice(0, 6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void diceWithSingleFaceThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> DiceLauncherTool.building().dice(1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rollWithoutAnyDiceGroupThrowsIllegalStateException() {
        assertThatThrownBy(() -> DiceLauncherTool.building().roll())
                .isInstanceOf(IllegalStateException.class);
    }
}
