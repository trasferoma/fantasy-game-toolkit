package it.fantasytoolkit.charactergenerator.result;

import java.util.List;

import it.fantasytoolkitcore.core.model.CharacterClass;
import it.fantasytoolkitcore.core.model.Race;
import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record CharacterResult(Race race, CharacterClass characterClass, String name,
        List<CharacterCharacteristic> characteristics) implements GeneratedElementResult {
    public static CharacterResult.Builder builder() {
        return new CharacterResult.Builder();
    }

    public static final class Builder {
        private Race race;
        private CharacterClass characterClass;
        private String name;
        private List<CharacterCharacteristic> characteristics;

        private Builder() {
        }

        public CharacterResult.Builder race(Race race) {
            this.race = race;
            return this;
        }

        public CharacterResult.Builder characterClass(CharacterClass characterClass) {
            this.characterClass = characterClass;
            return this;
        }

        public CharacterResult.Builder name(String name) {
            this.name = name;
            return this;
        }

        public CharacterResult.Builder characteristics(List<CharacterCharacteristic> characteristics) {
            this.characteristics = characteristics;
            return this;
        }

        public CharacterResult build() {
            return new CharacterResult(race, characterClass, name, characteristics);
        }
    }
}
