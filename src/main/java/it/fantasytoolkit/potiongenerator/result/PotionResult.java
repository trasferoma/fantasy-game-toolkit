package it.fantasytoolkit.potiongenerator.result;

import it.fantasytoolkit.buffdebuffgenerator.result.BuffElement;
import it.fantasytoolkit.buffdebuffgenerator.result.DebuffElement;
import it.fantasytoolkitcore.core.model.PotionType;
import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record PotionResult(PotionType type, Rarity rarity, int value, BuffElement buff,
        DebuffElement debuff) implements GeneratedElementResult {
    public static PotionResult.Builder builder() {
        return new PotionResult.Builder();
    }

    public static final class Builder {
        private PotionType type;
        private Rarity rarity;
        private int value;
        private BuffElement buff;
        private DebuffElement debuff;

        private Builder() {
        }

        public PotionResult.Builder type(PotionType type) {
            this.type = type;
            return this;
        }

        public PotionResult.Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public PotionResult.Builder value(int value) {
            this.value = value;
            return this;
        }

        public PotionResult.Builder buff(BuffElement buff) {
            this.buff = buff;
            return this;
        }

        public PotionResult.Builder debuff(DebuffElement debuff) {
            this.debuff = debuff;
            return this;
        }

        public PotionResult build() {
            return new PotionResult(type, rarity, value, buff, debuff);
        }
    }
}
