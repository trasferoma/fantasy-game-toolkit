package it.fantasytoolkit.core.model;

import lombok.Getter;

@Getter
public enum Race {
    HUMAN("/namegenerator/humans_names.txt", 'h'),
    ELF("/namegenerator/elves_names.txt", 'e'),
    ORC("/namegenerator/orks_names.txt", 'o'),
    UNDEAD("/namegenerator/undeads_names.txt", 'u');

    private final String namesFile;
    private final char symbol;

    Race(String namesFile, char symbol) {
        this.namesFile = namesFile;
        this.symbol = symbol;
    }
}
