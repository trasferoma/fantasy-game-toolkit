# weapon-generator — generazione di armi con rarità, status effect e attacco

Package: `it.fantasytoolkit.weapongenerator`.

Copia 1:1 del pattern di [jewel-generator](jewel-generator.md), con in più il
valore di **attacco** dipendente dalla rarità.

## `WeaponGeneratorTool`

Entry-point: `WeaponGeneratorTool.building()` → `Builder` → `generate()` →
`WeaponResult`.

### Builder

**Tipo di arma** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `weapon(Weapon weapon)` | tipo fisso |
| `randomWeapon()` | tipo casuale tra tutti i `Weapon.values()` |

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
| `noStatusEffect()` | off | arma con `buffs`/`debuffs` vuoti |
| `rules(WeaponRules rules)` | `DefaultWeaponRules` | range di attacco custom |

**Chiusura**: `generate()` → `WeaponResult`.

### Comportamento

- **Status effect**: come jewel, di default delega a `BuffDebuffGeneratorTool`
  con la rarità risolta; `noStatusEffect()` produce status effect vuoti senza
  toccare tipo e rarità.
- **Attacco**: `attack` (int) pescato casualmente in un intervallo dipendente
  dalla rarità, fornito da `WeaponRules`.

### Risultato — `WeaponResult`

`record WeaponResult(Weapon weapon, Rarity rarity, List<BuffElement> buffs,
List<DebuffElement> debuffs, int attack) implements GeneratedElementResult`.

Builder proprio: `WeaponResult.builder()...build()`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- nessuna fonte di tipo, oppure entrambe (`weapon` + `randomWeapon`);
- zero o più di una fonte di rarità.

### Regole di attacco — `weapongenerator.rules`

- `AttackRange(int minValue, int maxValue)` — record intervallo.
- `WeaponRules` — interfaccia: `AttackRange attackFor(Rarity)`.
- `DefaultWeaponRules` — mappa `Rarity` → `AttackRange` (`EnumMap`).

Range di default (crescenti):

| Rarità | Attacco `[min, max]` |
|--------|----------------------|
| COMMON | `[1, 3]` |
| UNCOMMON | `[3, 6]` |
| RARE | `[6, 10]` |
| EPIC | `[10, 15]` |
| LEGENDARY | `[15, 25]` |

### Esempi

```java
WeaponGeneratorTool.building().weapon(Weapon.SWORD).rarity(Rarity.EPIC).generate();
WeaponGeneratorTool.building().randomWeapon().maxRarity(Rarity.RARE).generate();
WeaponGeneratorTool.building().weapon(Weapon.BOW).rarity(Rarity.COMMON).noStatusEffect().generate();

WeaponResult weapon = WeaponGeneratorTool.building()
        .randomWeapon().randomRarity().generate();
int attack = weapon.attack();
```
