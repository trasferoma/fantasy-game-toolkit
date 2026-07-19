# armour-generator — generazione di armature con rarità, status effect e difesa

Package: `it.fantasytoolkit.armourgenerator`.

Identico a [weapon-generator](weapon-generator.md), con `armour(...)` /
`randomArmour()` al posto dell'arma e un valore di **difesa** al posto
dell'attacco.

## `ArmourGeneratorTool`

Entry-point: `ArmourGeneratorTool.building()` → `Builder` → `generate()` →
`ArmourResult`.

### Builder

**Tipo di armatura** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `armour(Armour armour)` | tipo fisso |
| `randomArmour()` | tipo casuale tra tutti gli `Armour.values()` |

**Rarità** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `rarity(Rarity rarity)` | rarità fissa |
| `maxRarity(Rarity maxRarity)` | casuale fino a quel livello incluso (per `ordinal()`) |
| `rarityTable(RarityTable table)` | estrazione pesata |
| `randomRarity()` | casuale tra tutte le `Rarity.values()` |

**Opzionali**:

| Metodo | Default | Effetto |
|--------|---------|---------|
| `noStatusEffect()` | off | armatura con `buffs`/`debuffs` vuoti |
| `rules(ArmourRules rules)` | `DefaultArmourRules` | range di difesa custom |

**Chiusura**: `generate()` → `ArmourResult`.

### Comportamento

- **Status effect**: di default delega a `BuffDebuffGeneratorTool` con la rarità
  risolta; `noStatusEffect()` produce status effect vuoti.
- **Difesa**: `defense` (int) pescato casualmente in un intervallo dipendente
  dalla rarità, fornito da `ArmourRules`.

### Risultato — `ArmourResult`

`record ArmourResult(Armour armour, Rarity rarity, List<BuffElement> buffs,
List<DebuffElement> debuffs, int defense) implements GeneratedElementResult`.

Builder proprio: `ArmourResult.builder()...build()`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- nessuna fonte di tipo, oppure entrambe (`armour` + `randomArmour`);
- zero o più di una fonte di rarità.

### Regole di difesa — `armourgenerator.rules`

- `DefenseRange(int minValue, int maxValue)` — record intervallo.
- `ArmourRules` — interfaccia: `DefenseRange defenseFor(Rarity)`.
- `DefaultArmourRules` — mappa `Rarity` → `DefenseRange` (`EnumMap`).

Range di default:

| Rarità | Difesa `[min, max]` |
|--------|---------------------|
| COMMON | `[1, 2]` |
| UNCOMMON | `[2, 4]` |
| RARE | `[4, 7]` |
| EPIC | `[7, 11]` |
| LEGENDARY | `[11, 18]` |

### Esempi

```java
ArmourGeneratorTool.building().armour(Armour.HELMET).rarity(Rarity.LEGENDARY).generate();
ArmourGeneratorTool.building().randomArmour().rarityTable(table).generate();
ArmourGeneratorTool.building().armour(Armour.BOOTS).randomRarity().noStatusEffect().generate();

ArmourResult armour = ArmourGeneratorTool.building()
        .armour(Armour.CHESTPLATE).rarity(Rarity.RARE).generate();
int defense = armour.defense();
```
