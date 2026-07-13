package it.fantasytoolkit.jewelgenerator.result;

import java.util.List;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkitcore.core.model.Jewel;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record JewelResult(Jewel jewel, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs)
        implements GeneratedElementResult {
    public static JewelResult.Builder builder() {
        return new JewelResult.Builder();
    }

    public static final class Builder {
        private Jewel jewel;
        private Rarity rarity;
        private List<BuffElement> buffs;
        private List<DebuffElement> debuffs;

        private Builder() {
        }

        public JewelResult.Builder jewel(Jewel jewel) {
            this.jewel = jewel;
            return this;
        }

        public JewelResult.Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public JewelResult.Builder buffs(List<BuffElement> buffs) {
            this.buffs = buffs;
            return this;
        }

        public JewelResult.Builder debuffs(List<DebuffElement> debuffs) {
            this.debuffs = debuffs;
            return this;
        }

        public JewelResult build() {
            return new JewelResult(jewel, rarity, buffs, debuffs);
        }
    }
}
