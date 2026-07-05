package it.fantasytoolkit.namegenerator.result;

import it.fantasytoolkit.core.pojo.GeneratedElementResult;
import it.fantasytoolkit.core.types.Seed;

public class NameResult extends GeneratedElementResult {
    private String name;

    public NameResult setName(String name, Seed seed) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public NameResult setName(String name) {
        this.name = name;
        return this;
    }

    public String toString() {
        return "NameResult [name=" + name + ", seed=" + getSeed() + "]";
    }
}
