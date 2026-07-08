package it.fantasytoolkit.namegenerator;

import java.util.Random;

import it.fantasytoolkitcore.core.model.Race;
import it.fantasytoolkit.namegenerator.result.NameResult;
import it.fantasytoolkit.namegenerator.tool.NameGeneratorTool;

public final class CharacterNameGeneratorTool {

    private static final String NICKNAMES_FILE = "/namegenerator/nicknames.txt";

    private CharacterNameGeneratorTool() {
    }

    public static Builder race(Race race) {
        return new Builder().race(race);
    }

    public static Builder addNickname() {
        return new Builder().addNickname();
    }

    public static final class Builder {

        private Race race;
        private boolean withNickname;

        private Builder() {
        }

        public Builder race(Race race) {
            this.race = race;
            return this;
        }

        public Builder addNickname() {
            this.withNickname = true;
            return this;
        }

        public NameResult generate() {
            if (race == null) {
                throw new IllegalStateException("Race must be set before generating a name");
            }

            Random random = new Random();
            String name = buildName(random);

            return NameResult.builder()
                    .name(name)
                    .build();
        }

        private String buildName(Random random) {
            String raceName = new NameGeneratorTool(race.getNamesFile()).pick(random);
            if (!withNickname) {
                return raceName;
            }

            String nickname = new NameGeneratorTool(NICKNAMES_FILE).pick(random);
            return raceName + " " + nickname;
        }
    }
}
