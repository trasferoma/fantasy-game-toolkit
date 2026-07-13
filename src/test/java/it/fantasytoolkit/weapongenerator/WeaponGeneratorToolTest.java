package it.fantasytoolkit.weapongenerator;

import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.Weapon;
import it.fantasytoolkit.weapongenerator.result.WeaponResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WeaponGeneratorToolTest {

    @Test
    void generatesWeaponWithRequestedWeaponAndRarity() {
        WeaponResult result = WeaponGeneratorTool.building()
                .weapon(Weapon.SWORD)
                .rarity(Rarity.RARE)
                .generate();

        assertThat(result.weapon()).isEqualTo(Weapon.SWORD);
        assertThat(result.rarity()).isEqualTo(Rarity.RARE);
        assertThat(result.buffs()).isNotEmpty();
        assertThat(result.debuffs()).isNotNull();
    }

    @Test
    void generatesWeaponWithNoStatusEffectHasEmptyBuffsAndDebuffs() {
        WeaponResult result = WeaponGeneratorTool.building()
                .weapon(Weapon.AXE)
                .rarity(Rarity.COMMON)
                .noStatusEffect()
                .generate();

        assertThat(result.weapon()).isEqualTo(Weapon.AXE);
        assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        assertThat(result.buffs()).isNotNull().isEmpty();
        assertThat(result.debuffs()).isNotNull().isEmpty();
    }

    @Test
    void generateWithoutRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> WeaponGeneratorTool.building()
                .weapon(Weapon.SWORD)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of rarity, maxRarity, rarityTable or randomRarity must be set before generating a weapon");
    }

    @Test
    void generateWithoutWeaponThrowsIllegalStateException() {
        assertThatThrownBy(() -> WeaponGeneratorTool.building()
                .rarity(Rarity.RARE)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Weapon must be set before generating a weapon");
    }

    @Test
    void generateWithBothWeaponAndRandomWeaponThrowsIllegalStateException() {
        assertThatThrownBy(() -> WeaponGeneratorTool.building()
                .weapon(Weapon.SWORD)
                .randomWeapon()
                .rarity(Rarity.COMMON)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of weapon or randomWeapon can be used together");
    }

    @Test
    void generateWithBothRarityAndRandomRarityThrowsIllegalStateException() {
        assertThatThrownBy(() -> WeaponGeneratorTool.building()
                .weapon(Weapon.SWORD)
                .rarity(Rarity.COMMON)
                .randomRarity()
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of rarity, maxRarity, rarityTable or randomRarity can be used together");
    }

    @Test
    void generatesWeaponWithMaxRarityCommonAlwaysReturnsCommon() {
        for (int i = 0; i < 50; i++) {
            WeaponResult result = WeaponGeneratorTool.building()
                    .weapon(Weapon.BOW)
                    .maxRarity(Rarity.COMMON)
                    .generate();

            assertThat(result.weapon()).isEqualTo(Weapon.BOW);
            assertThat(result.rarity()).isEqualTo(Rarity.COMMON);
        }
    }

    @Test
    void generatesRandomWeaponAndRandomRarityStayWithinExpectedValues() {
        for (int i = 0; i < 50; i++) {
            WeaponResult result = WeaponGeneratorTool.building()
                    .randomWeapon()
                    .randomRarity()
                    .generate();

            assertThat(result.weapon()).isIn((Object[]) Weapon.values());
            assertThat(result.rarity()).isNotNull().isIn((Object[]) Rarity.values());
            assertThat(result.buffs()).isNotEmpty();
            assertThat(result.debuffs()).isNotNull();
        }
    }
}
