# potion-generator — generazione di pozioni per famiglia e rarità

Package: `it.fantasytoolkit.potiongenerator`.

Riusa il pattern builder + risoluzione rarità dei generatori di equipaggiamento,
ma il payload dipende dalla **famiglia** (`PotionType`) e non ci sono status
effect opzionali: il payload è intrinseco alla famiglia.

## `PotionGeneratorTool`

Entry-point: `PotionGeneratorTool.building()` → `Builder` → `generate()` →
`PotionResult`.

### Builder

**Tipo (famiglia)** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `type(PotionType type)` | famiglia fissa |
| `randomType()` | famiglia casuale tra tutti i `PotionType.values()` |

**Rarità** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `rarity(Rarity rarity)` | rarità fissa |
| `maxRarity(Rarity maxRarity)` | casuale fino a quel livello incluso (per `ordinal()`) |
| `rarityTable(RarityTable table)` | estrazione pesata |
| `randomRarity()` | casuale tra tutte le `Rarity.values()` |

**Opzionale**:

| Metodo | Default | Effetto |
|--------|---------|---------|
| `rules(PotionRules rules)` | `DefaultPotionRules` | range di rigenerazione custom |

**Chiusura**: `generate()` → `PotionResult`.

> **Niente `noStatusEffect()`**: per una pozione il payload
> (buff/debuff/rigenerazione) è intrinseco alla famiglia, non un extra opzionale.

### Comportamento — payload per famiglia

`generate()` fa `switch` sul tipo risolto:

| Famiglia | Payload |
|----------|---------|
| `BUFF` | un singolo `BuffElement` (delega a `BuffDebuffGeneratorTool`, si prende `buffs.get(0)`: ogni rarità produce ≥1 buff). `value` = `0`, `debuff` = `null`. |
| `DEBUFF` | genera un `BuffElement` e lo **converte** in `DebuffElement` (stessi `Characteristic`+`value`), perché il buff-debuff generator non produce ancora debuff. `value` = `0`, `buff` = `null`. |
| `HEALTH_REGENERATION` / `MANA_REGENERATION` | `value` (punti vita/mana rigenerati) pescato in un intervallo dipendente dalla rarità (`PotionRules`). `buff`/`debuff` = `null`. |

### Risultato — `PotionResult`

`record PotionResult(PotionType type, Rarity rarity, int value, BuffElement buff,
DebuffElement debuff) implements GeneratedElementResult`.

A differenza degli altri result usa **singoli** `BuffElement`/`DebuffElement`
(non liste). I campi non pertinenti alla famiglia sono `null` (`buff`/`debuff`)
o `0` (`value`). Builder proprio: `PotionResult.builder()...build()`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- nessuna fonte di tipo, oppure entrambe (`type` + `randomType`);
- zero o più di una fonte di rarità.

### Regole di rigenerazione — `potiongenerator.rules`

- `RegenerationRange(int minValue, int maxValue)` — record intervallo.
- `PotionRules` — interfaccia: `RegenerationRange regenerationFor(Rarity)`.
- `DefaultPotionRules` — mappa `Rarity` → `RegenerationRange` (`EnumMap`).

Range di default (identici per vita e mana):

| Rarità | Rigenerazione `[min, max]` |
|--------|----------------------------|
| COMMON | `[5, 10]` |
| UNCOMMON | `[10, 20]` |
| RARE | `[20, 35]` |
| EPIC | `[35, 55]` |
| LEGENDARY | `[55, 90]` |

### Esempi

```java
PotionGeneratorTool.building().type(PotionType.BUFF).rarity(Rarity.EPIC).generate();
PotionGeneratorTool.building().type(PotionType.DEBUFF).maxRarity(Rarity.RARE).generate();
PotionGeneratorTool.building().type(PotionType.HEALTH_REGENERATION).rarity(Rarity.COMMON).generate();  // value in [5,10]
PotionGeneratorTool.building().randomType().randomRarity().generate();
```
