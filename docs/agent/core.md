# core — modello di dominio e tipi condivisi

Package: `it.fantasytoolkitcore.core` (distinto dal package dei tool
`it.fantasytoolkit.*` e dal `groupId` `it.fantasytoolkit`).

Contiene gli enum di dominio, le tabelle di estrazione rarità e di bonus, e la
marker interface comune ai result. È il vocabolario condiviso da tutti i tool.

## Marker result

### `core.pojo.GeneratedElementResult`

Interfaccia **vuota** (marker/tag) implementata da tutti i result dei tool.
Nessun metodo: serve solo a dare un contratto-tipo comune ai risultati generati.

## Enum di dominio

### `core.model.Race`

Costanti: `HUMAN`, `ELF`, `ORC`, `UNDEAD`.

`@Getter` Lombok. Ogni razza espone:

- `getNamesFile()` → path classpath del file di nomi (es. `/namegenerator/humans_names.txt`);
- `getSymbol()` → `char` simbolo della razza (`h`, `e`, `o`, `u`).

Punto di estensione per una nuova razza: nuova costante + relativo file di nomi.

### `core.model.Rarity`

Costanti **ordinate dal meno al più raro**: `COMMON`, `UNCOMMON`, `RARE`,
`EPIC`, `LEGENDARY`. L'ordine (`ordinal()`) è semanticamente rilevante: è usato
da `maxRarity(...)` nei generatori per pescare fino a un livello incluso.

### `core.model.Jewel`

Costanti: `RING`, `NECKLACE`, `BRACELET`, `EARRING`.

### `core.model.Weapon`

Costanti: `SWORD`, `BOW`, `AXE`, `HAMMER`, `SHIELD`, `DAGGER`, `CROSSBOW`,
`BATTLEAXE`, `STAFF`, `SCEPTER`.

### `core.model.Armour`

Costanti: `CHESTPLATE`, `PANTS`, `BOOTS`, `GAUNTLETS`, `HELMET`, `BELT`, `SHIELD`.

### `core.model.PotionType`

Costanti: `BUFF`, `DEBUFF`, `HEALTH_REGENERATION`, `MANA_REGENERATION`. La
distinzione vita/mana della rigenerazione vive qui (il result ha un solo
`int value`).

### `core.model.Characteristic`

Costanti: `STRENGTH`, `INTELLIGENCE`, `AGILITY`, `CHARISMA`, `RESISTANCE`,
`STAMINA`, `LUCK`.

### `core.model.CharacterClass`

Costanti: `WARRIOR`, `MAGE`, `THIEF`, `RANGER`. Come `Race`, ogni classe ha
bonus tematici sulle caratteristiche (vedi `ClassBonusTable`).

### `core.model.ChamberType`

Costanti: `ENTRY`, `STANDARD`, `FINAL`. Marca il ruolo di una stanza nel
dungeon (vedi [dungeon-generator](dungeon-generator.md)).

## Tabella rarità e selettore

### `core.model.RarityTable`

Tabella di estrazione **pesata** delle rarità.

**Builder**: `RarityTable.builder().entry(Rarity, int weight)...build()`.

**API**: `draw(Random)` → `Rarity` con probabilità proporzionale al peso.

**Vincoli (validati in `build()`, altrimenti `IllegalStateException`)**:

- ogni peso deve essere **positivo**;
- le rarità devono essere **uniche**;
- la **somma dei pesi deve essere esattamente 100**.

Immutabile (`List.copyOf`).

```java
RarityTable table = RarityTable.builder()
        .entry(Rarity.COMMON, 60)
        .entry(Rarity.RARE, 40)
        .build();
Rarity drawn = table.draw(new Random());
```

### `core.tool.RaritySelector`

Selettore di rarità con **distribuzione di default predefinita**. `final`,
costruttore privato, `Random` fornito dall'esterno.

**API**:

- `RaritySelector.withDefaultDistribution()` → costruisce internamente una
  `RarityTable` coi pesi standard: COMMON `50`, UNCOMMON `25`, RARE `12`,
  EPIC `8`, LEGENDARY `5` (somma `100`);
- `select(Random)` → delega a `RarityTable.draw(Random)` e restituisce la
  `Rarity` estratta.

La `RarityTable` interna non è esposta. Per alimentare un generatore con una
rarità estratta secondo la distribuzione di default:

```java
Rarity rarity = RaritySelector.withDefaultDistribution().select(new Random());
JewelGeneratorTool.building().randomJewel().rarity(rarity).generate();
```

## Tabelle di bonus

Due tabelle gemelle (stesso builder, stesse validazioni, stesso record
`CharacteristicBonus`), una con chiave `Race` e una con chiave `CharacterClass`.
Usate da [character-generator](character-generator.md) per applicare bonus
additivi alle caratteristiche.

### `core.model.RaceBonusTable`

**Builder**: `RaceBonusTable.builder().bonus(Race, Characteristic, int value)...build()`.

**API**:

- `withDefaultBonuses()` → tabella coi default:
  - HUMAN: `STRENGTH +1`, `AGILITY +1`, `INTELLIGENCE +1`
  - ELF: `AGILITY +2`, `INTELLIGENCE +1`
  - ORC: `STRENGTH +2`, `RESISTANCE +1`
  - UNDEAD: `RESISTANCE +2`, `STAMINA +1`
- `bonusesFor(Race)` → `List<CharacteristicBonus>` per la razza, oppure
  `List.of()` se la razza non ha entry.

**Record annidato pubblico**: `RaceBonusTable.CharacteristicBonus(Characteristic characteristic, int value)`.

**Vincoli (validati in `build()`, altrimenti `IllegalStateException`)**:

- i valori devono essere **positivi**;
- le coppie `(razza, caratteristica)` devono essere **uniche**;
- **tabella vuota ammessa** (`builder().build()`): è l'opt-out dai bonus.

Immutabile (`EnumMap` + liste unmodifiable).

### `core.model.ClassBonusTable`

Gemella di `RaceBonusTable` con chiave `CharacterClass`. Stesso builder,
validazioni, record `CharacteristicBonus` e "tabella vuota ammessa".

**API**:

- `withDefaultBonuses()` → default:
  - WARRIOR: `STRENGTH +2`, `STAMINA +1`
  - MAGE: `INTELLIGENCE +2`, `CHARISMA +1`
  - THIEF: `AGILITY +2`, `LUCK +1`
  - RANGER: `AGILITY +2`, `RESISTANCE +1`
- `bonusesFor(CharacterClass)` → `List<CharacteristicBonus>`.
