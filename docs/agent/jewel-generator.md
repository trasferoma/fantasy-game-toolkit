# jewel-generator — generazione di gioielli con rarità

Package: `it.fantasytoolkit.jewelgenerator`.

Genera un gioiello con tipo, rarità e status effect. È il capostipite del
pattern condiviso da weapon, armour e potion generator (fonte del tipo + fonte
della rarità esclusive, delega a [buff-debuff-generator](buff-debuff-generator.md)).

## `JewelGeneratorTool`

Entry-point: `JewelGeneratorTool.building()` → `Builder` → `generate()` →
`JewelResult`.

### Builder

**Tipo di gioiello** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `jewel(Jewel jewel)` | tipo fisso |
| `randomJewel()` | tipo casuale tra tutti i `Jewel.values()` |

**Rarità** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `rarity(Rarity rarity)` | rarità fissa |
| `maxRarity(Rarity maxRarity)` | casuale fino a quel livello **incluso** (per `ordinal()`) |
| `rarityTable(RarityTable table)` | estrazione pesata (vedi [core](core.md)) |
| `randomRarity()` | casuale tra tutte le `Rarity.values()` |

**Status effect** (opzionale):

| Metodo | Effetto |
|--------|---------|
| `noStatusEffect()` | gioiello **senza** buff/debuff (`buffs`/`debuffs` vuoti) |

**Chiusura**: `generate()` → `JewelResult`.

### Comportamento

- Di default il gioiello riceve i propri status effect delegando a
  `BuffDebuffGeneratorTool` con la rarità risolta (i debuff restano oggi sempre
  vuoti, vedi [buff-debuff-generator](buff-debuff-generator.md)).
- `noStatusEffect()` salta quella chiamata e produce `buffs`/`debuffs` vuoti;
  è combinabile con qualsiasi fonte di tipo/rarità e non incide su tipo e rarità.

### Risultato — `JewelResult`

`record JewelResult(Jewel jewel, Rarity rarity, List<BuffElement> buffs,
List<DebuffElement> debuffs) implements GeneratedElementResult`.

`buffs`/`debuffs` riusano i record di
[buff-debuff-generator](buff-debuff-generator.md). Builder proprio:
`JewelResult.builder()...build()`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- nessuna fonte di tipo, oppure entrambe (`jewel` + `randomJewel`);
- zero fonti di rarità, oppure più di una tra `rarity`/`maxRarity`/`rarityTable`/`randomRarity`.

### Esempi

```java
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.EPIC).generate();
JewelGeneratorTool.building().randomJewel().maxRarity(Rarity.RARE).generate();
JewelGeneratorTool.building().jewel(Jewel.NECKLACE).rarityTable(table).generate();
JewelGeneratorTool.building().randomJewel().randomRarity().generate();

// gioiello senza status effect
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.COMMON).noStatusEffect().generate();
```
