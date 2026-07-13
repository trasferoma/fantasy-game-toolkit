package it.fantasytoolkit.armourgenerator.result;

import java.util.List;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkitcore.core.model.Armour;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record ArmourResult(Armour armour, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs)
        implements GeneratedElementResult {
    public static ArmourResult.Builder builder() {
        return new ArmourResult.Builder();
    }

    public static final class Builder {
        private Armour armour;
        private Rarity rarity;
        private List<BuffElement> buffs;
        private List<DebuffElement> debuffs;

        private Builder() {
        }

        public ArmourResult.Builder armour(Armour armour) {
            this.armour = armour;
            return this;
        }

        public ArmourResult.Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public ArmourResult.Builder buffs(List<BuffElement> buffs) {
            this.buffs = buffs;
            return this;
        }

        public ArmourResult.Builder debuffs(List<DebuffElement> debuffs) {
            this.debuffs = debuffs;
            return this;
        }

        public ArmourResult build() {
            return new ArmourResult(armour, rarity, buffs, debuffs);
        }
    }
}
