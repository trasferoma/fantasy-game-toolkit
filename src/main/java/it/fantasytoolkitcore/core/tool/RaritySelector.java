package it.fantasytoolkitcore.core.tool;

import it.fantasytoolkitcore.core.model.Rarity;
import it.fantasytoolkitcore.core.model.RarityTable;

import java.util.Random;

public final class RaritySelector {

    private final RarityTable rarityTable;

    private RaritySelector(RarityTable rarityTable) {
        this.rarityTable = rarityTable;
    }

    public static RaritySelector withDefaultDistribution() {
        RarityTable rarityTable = RarityTable.builder()
                .entry(Rarity.COMMON, 50)
                .entry(Rarity.UNCOMMON, 25)
                .entry(Rarity.RARE, 12)
                .entry(Rarity.EPIC, 8)
                .entry(Rarity.LEGENDARY, 5)
                .build();

        return new RaritySelector(rarityTable);
    }

    public Rarity select(Random random) {
        return rarityTable.draw(random);
    }
}
