package it.fantasytoolkit.weapongenerator.result;

import java.util.List;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.Weapon;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record WeaponResult(Weapon weapon, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs,
        int attack) implements GeneratedElementResult {
    public static WeaponResult.Builder builder() {
        return new WeaponResult.Builder();
    }

    public static final class Builder {
        private Weapon weapon;
        private Rarity rarity;
        private List<BuffElement> buffs;
        private List<DebuffElement> debuffs;
        private int attack;

        private Builder() {
        }

        public WeaponResult.Builder weapon(Weapon weapon) {
            this.weapon = weapon;
            return this;
        }

        public WeaponResult.Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public WeaponResult.Builder buffs(List<BuffElement> buffs) {
            this.buffs = buffs;
            return this;
        }

        public WeaponResult.Builder debuffs(List<DebuffElement> debuffs) {
            this.debuffs = debuffs;
            return this;
        }

        public WeaponResult.Builder attack(int attack) {
            this.attack = attack;
            return this;
        }

        public WeaponResult build() {
            return new WeaponResult(weapon, rarity, buffs, debuffs, attack);
        }
    }
}
