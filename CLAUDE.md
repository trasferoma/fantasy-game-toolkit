# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandi

Progetto Maven, **Java 21**. La macchina ha come default un JDK 17 (`JAVA_HOME` → `C:\lavoro\jdk\openlogic-openjdk-17...`), che **non compila** (`invalid target release: 21`). Per i comandi Maven punta `JAVA_HOME` al JDK 21:

```bash
export JAVA_HOME="/c/Users/fabio.dearcangelis/.jdks/ms-21.0.9"   # git bash

mvn test                                        # compila ed esegue tutti i test
mvn test -Dtest=CharacterNameGeneratorToolTest  # una singola classe di test
mvn test -Dtest=CharacterNameGeneratorToolTest#generatesElfNameFromDictionaryWithoutNickname  # un singolo metodo
mvn compile                                # sola compilazione
mvn package                                # build completa
```

Test: JUnit Jupiter (JUnit 5) + AssertJ (`assertThat(...)`). Surefire con `useModulePath=false`. Lombok è `provided` (`@Getter` risolto a compile-time).

## Architettura

Toolkit per giochi fantasy, organizzato in **moduli logici (package)** dentro un singolo modulo Maven. Ogni generatore pubblico è un `*Tool` con costruttore privato e un entry-point statico `building()` che stacca un `Builder` interno fluente; la catena si chiude sempre con `generate()`, che restituisce un result immutabile (record). Ogni result implementa `GeneratedElementResult`, oggi **marker interface vuota** (`core.pojo.GeneratedElementResult`): un contratto-tag comune tra i result, senza metodi.

I `*Tool` usano internamente `new Random()`: la generazione è **casuale e non riproducibile** (nessun seme esposto).

### `core` — modello di dominio e tipi condivisi

Package `it.fantasytoolkitcore.core` (nota: distinto dal `groupId` `it.fantasytoolkit` e dal package dei tool).

- `core.model.Race` — enum (`HUMAN`/`ELF`/`ORC`/`UNDEAD`), `@Getter` Lombok, mappa ogni razza al proprio file di nomi (`namesFile`) e a un `symbol` char. Punto di estensione per nuove razze: nuova costante + relativo file di risorse.
- `core.model.Rarity` — enum ordinato dal meno al più raro: `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`. L'ordine (`ordinal()`) è semanticamente rilevante (vedi `maxRarity`).
- `core.model.Jewel` — enum dei tipi di gioiello: `RING`, `NECKLACE`, `BRACELET`, `EARRING`.
- `core.model.Weapon` — enum dei tipi di arma: `SWORD`, `BOW`, `AXE`, `HAMMER`, `SHIELD`, `DAGGER`, `CROSSBOW`, `BATTLEAXE`, `STAFF`, `SCEPTER`.
- `core.model.Armour` — enum dei tipi di armatura: `CHESTPLATE`, `PANTS`, `BOOTS`, `GAUNTLETS`, `HELMET`, `BELT`.
- `core.model.PotionType` — enum delle famiglie di pozione: `BUFF`, `DEBUFF`, `HEALTH_REGENERATION`, `MANA_REGENERATION`. La distinzione vita/mana della rigenerazione vive qui (il result ha un solo `int value`).
- `core.model.Characteristic` — enum delle caratteristiche di gioco: `STRENGTH`, `INTELLIGENCE`, `AGILITY`, `CHARISMA`, `RESISTANCE`, `STAMINA`, `LUCK`.
- `core.model.CharacterClass` — enum delle classi del personaggio: `WARRIOR`, `MAGE`, `THIEF`, `RANGER`. Come `Race`, ogni classe ha bonus tematici sulle caratteristiche (vedi `ClassBonusTable`).
- `core.model.ChamberType` — enum del ruolo di una stanza nel dungeon: `ENTRY`, `STANDARD`, `FINAL`. Usato da `dungeongenerator` per marcare stanza d'ingresso e stanza finale.
- `core.model.RarityTable` — tabella di estrazione pesata delle rarità. Costruita con `RarityTable.builder().entry(rarity, weight)...build()`; `draw(Random)` restituisce una `Rarity` con probabilità proporzionale al peso. `build()` valida che i pesi siano positivi, le rarità uniche e la **somma dei pesi pari a 100**; altrimenti `IllegalStateException`. Immutabile (`List.copyOf`).
- `core.tool.RaritySelector` — selettore di rarità con **distribuzione di default predefinita**. `RaritySelector.withDefaultDistribution()` costruisce internamente una `RarityTable` con i pesi standard (COMMON `50`, UNCOMMON `25`, RARE `12`, EPIC `8`, LEGENDARY `5`; somma `100`); `select(Random)` delega a `RarityTable.draw(Random)` e restituisce la `Rarity` estratta. `final`, costruttore privato, `Random` fornito dall'esterno. Riusa `RarityTable`, non ne duplica la logica. La `RarityTable` interna non è esposta: per alimentare i generatori si usa `select(random)` → `rarity(Rarity)`. Uso: `RaritySelector.withDefaultDistribution().select(random)`.
- `core.model.RaceBonusTable` — tabella dei bonus alle caratteristiche per razza (gemella di `ClassBonusTable`). Costruita con `RaceBonusTable.builder().bonus(race, characteristic, value)...build()`; `bonusesFor(Race)` restituisce la lista di `CharacteristicBonus(Characteristic, int value)` (record pubblico annidato), o `List.of()` se la razza non ha entry. `build()` valida che i valori siano positivi e le coppie (razza, caratteristica) uniche; **tabella vuota ammessa** (opt-out dai bonus). Immutabile (`EnumMap` + liste unmodifiable). `RaceBonusTable.withDefaultBonuses()` fornisce i default: HUMAN `STRENGTH+1, AGILITY+1, INTELLIGENCE+1`; ELF `AGILITY+2, INTELLIGENCE+1`; ORC `STRENGTH+2, RESISTANCE+1`; UNDEAD `RESISTANCE+2, STAMINA+1`.
- `core.model.ClassBonusTable` — gemella per copia di `RaceBonusTable`, con chiave `CharacterClass` al posto di `Race` (stesso builder, stesse validazioni, stesso `CharacteristicBonus`, tabella vuota ammessa, `EnumMap`). `ClassBonusTable.withDefaultBonuses()` fornisce i default: WARRIOR `STRENGTH+2, STAMINA+1`; MAGE `INTELLIGENCE+2, CHARISMA+1`; THIEF `AGILITY+2, LUCK+1`; RANGER `AGILITY+2, RESISTANCE+1`.
- `core.pojo.GeneratedElementResult` — marker interface comune a tutti i result.

### `namegenerator` — generazione di nomi da liste di parole del classpath

- `namegenerator.tool.NameGeneratorTool` — motore base (costruttore pubblico che riceve il path del file). Carica un file dal classpath (una parola per riga, UTF-8, righe vuote scartate). L'estrazione avviene con `pick(Random)`, **unico punto di selezione**, con un `Random` fornito dall'esterno. Risorsa mancante o lista vuota → `IllegalStateException` con messaggio diagnostico. È il mattone riusato dal generatore pubblico.
- `namegenerator.result.NameResult` — `record NameResult(String name) implements GeneratedElementResult`, con `Builder` interno fluente (`NameResult.builder().name(...).build()`).
- `namegenerator.CharacterNameGeneratorTool` — generatore pubblico dei nomi. `building()` → `Builder`; `race(Race)` e `addNickname()` sono metodi fluenti invocabili in qualsiasi ordine; `generate()` produce il `NameResult`. Il nome è quello di razza (lista scelta dalla `Race`); con `addNickname()` diventa `name + " " + nickname`, con nickname da `nicknames.txt`. `generate()` senza `race()` → `IllegalStateException`.

```java
CharacterNameGeneratorTool.building().addNickname().race(Race.HUMAN).generate();   // nome + nickname
CharacterNameGeneratorTool.building().race(Race.ORC).generate();                   // solo nome di razza
```

### `jewelgenerator` — generazione di gioielli con rarità

- `jewelgenerator.JewelGeneratorTool` — `building()` → `Builder` → `generate()` → `JewelResult`.
  - **Tipo di gioiello**: `jewel(Jewel)` per uno fisso, oppure `randomJewel()` per uno casuale tra tutti i `Jewel.values()`. Nessuno dei due → `IllegalStateException`.
  - **Rarità**: esattamente **una** tra `rarity(Rarity)` (rarità fissa), `maxRarity(Rarity)` (casuale fino a quel livello incluso, per `ordinal()`), `rarityTable(RarityTable)` (estrazione pesata) e `randomRarity()` (casuale tra tutte le `Rarity.values()`). Zero o più di una fonte → `IllegalStateException`.
  - **Buff/debuff**: di default il gioiello riceve i propri status effect delegando internamente a `BuffDebuffGeneratorTool` con la rarità risolta (i debuff restano oggi sempre vuoti, vedi `buffdebuffgenerator`). `noStatusEffect()` produce invece un gioiello **senza** status effect (`buffs` e `debuffs` vuoti) saltando quella chiamata; è combinabile con qualsiasi sorgente di gioiello/rarità e non incide su tipo e rarità.
- `jewelgenerator.result.JewelResult` — `record JewelResult(Jewel jewel, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs) implements GeneratedElementResult`, con `Builder` interno. `buffs`/`debuffs` riusano i record di `buffdebuffgenerator.result`.

```java
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.EPIC).generate();
JewelGeneratorTool.building().randomJewel().maxRarity(Rarity.RARE).generate();
JewelGeneratorTool.building().jewel(Jewel.NECKLACE).rarityTable(table).generate();
JewelGeneratorTool.building().randomJewel().randomRarity().generate();
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.COMMON).noStatusEffect().generate();  // buffs/debuffs vuoti
```

### `buffdebuffgenerator` — generazione di buff/debuff per rarità

- `buffdebuffgenerator.BuffDebuffGeneratorTool` — `building().rarity(Rarity)...generate()` → `BuffDebuffResult`. `rarity(...)` è obbligatoria (altrimenti `IllegalStateException`); `rules(BuffDebuffRules)` è opzionale e di default usa `DefaultBuffDebuffRules`.
  - La rarità seleziona la lista di `BuffCombination` ammesse; se ne pesca una a caso. Ogni combinazione definisce `count` (quanti buff) e l'intervallo `[minValue, maxValue]` dei valori. Le caratteristiche vengono mescolate e se ne prendono le prime `count`, ciascuna con un valore casuale nell'intervallo.
  - **Stato attuale**: i **debuff non sono ancora generati** — `debuffs` è sempre `List.of()`. La struttura per estenderli esiste (`DebuffElement`, `StatusEffect`), ma la logica di produzione va ancora scritta.
- `buffdebuffgenerator.result` — `BuffDebuffResult(List<BuffElement> buffs, List<DebuffElement> debuffs)`; `BuffElement`/`DebuffElement` sono record `(Characteristic, int value)` che implementano l'interfaccia comune `StatusEffect`.
- `buffdebuffgenerator.rules` — `BuffDebuffRules` (interfaccia: `combinationsFor(Rarity)`), `DefaultBuffDebuffRules` (mappa `Rarity` → combinazioni, `EnumMap`), `BuffCombination(int count, int minValue, int maxValue)`. Le regole sono un **punto di estensione**: passando una `BuffDebuffRules` custom si cambiano combinazioni e range senza toccare il tool.

### `weapongenerator` — generazione di armi con rarità e status effect

- `weapongenerator.WeaponGeneratorTool` — `building()` → `Builder` → `generate()` → `WeaponResult`. **Copia 1:1 del pattern di `JewelGeneratorTool`**, cambia solo il tipo generato.
  - **Tipo di arma**: `weapon(Weapon)` per una fissa, oppure `randomWeapon()` per una casuale tra tutti i `Weapon.values()`. Nessuno dei due o entrambi → `IllegalStateException`.
  - **Rarità**: esattamente **una** tra `rarity(Rarity)`, `maxRarity(Rarity)`, `rarityTable(RarityTable)` e `randomRarity()`. Zero o più di una fonte → `IllegalStateException`.
  - **Buff/debuff**: come Jewel, di default delega a `BuffDebuffGeneratorTool` con la rarità risolta; `noStatusEffect()` produce un'arma con `buffs`/`debuffs` vuoti, senza toccare tipo e rarità.
  - **Attacco**: ogni arma ha un `attack` (int) pescato casualmente in un intervallo che dipende dalla rarità. L'intervallo è fornito da `WeaponRules` (default `DefaultWeaponRules`); `rules(WeaponRules)` è opzionale e permette range custom.
- `weapongenerator.rules` — `AttackRange(int minValue, int maxValue)` (gemello di `BuffCombination`), interfaccia `WeaponRules` (`attackFor(Rarity)`), `DefaultWeaponRules` (mappa `Rarity` → `AttackRange`, `EnumMap`). Range di default crescenti: COMMON `[1,3]`, UNCOMMON `[3,6]`, RARE `[6,10]`, EPIC `[10,15]`, LEGENDARY `[15,25]`.
- `weapongenerator.result.WeaponResult` — `record WeaponResult(Weapon weapon, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs, int attack) implements GeneratedElementResult`, con `Builder` interno.

```java
WeaponGeneratorTool.building().weapon(Weapon.SWORD).rarity(Rarity.EPIC).generate();
WeaponGeneratorTool.building().randomWeapon().maxRarity(Rarity.RARE).generate();
WeaponGeneratorTool.building().weapon(Weapon.BOW).rarity(Rarity.COMMON).noStatusEffect().generate();  // buffs/debuffs vuoti
```

### `armourgenerator` — generazione di armature con rarità e status effect

- `armourgenerator.ArmourGeneratorTool` — `building()` → `Builder` → `generate()` → `ArmourResult`. Identico a `WeaponGeneratorTool`, con `armour(Armour)` / `randomArmour()` al posto dell'arma. Stesse quattro fonti di rarità e stesso `noStatusEffect()`. Ogni armatura ha un `defense` (int) pescato in un intervallo dipendente dalla rarità, fornito da `ArmourRules` (default `DefaultArmourRules`); `rules(ArmourRules)` opzionale.
- `armourgenerator.rules` — `DefenseRange(int minValue, int maxValue)`, interfaccia `ArmourRules` (`defenseFor(Rarity)`), `DefaultArmourRules` (`EnumMap`). Range di default: COMMON `[1,2]`, UNCOMMON `[2,4]`, RARE `[4,7]`, EPIC `[7,11]`, LEGENDARY `[11,18]`.
- `armourgenerator.result.ArmourResult` — `record ArmourResult(Armour armour, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs, int defense) implements GeneratedElementResult`, con `Builder` interno.

```java
ArmourGeneratorTool.building().armour(Armour.HELMET).rarity(Rarity.LEGENDARY).generate();
ArmourGeneratorTool.building().randomArmour().rarityTable(table).generate();
ArmourGeneratorTool.building().armour(Armour.BOOTS).randomRarity().noStatusEffect().generate();  // buffs/debuffs vuoti
```

### `potiongenerator` — generazione di pozioni per famiglia e rarità

- `potiongenerator.PotionGeneratorTool` — `building()` → `Builder` → `generate()` → `PotionResult`. Riusa il pattern builder + risoluzione rarità di `WeaponGeneratorTool`.
  - **Tipo (famiglia)**: `type(PotionType)` per una fissa, oppure `randomType()` per una casuale tra tutti i `PotionType.values()`. Nessuno dei due o entrambi → `IllegalStateException`.
  - **Rarità**: esattamente **una** tra `rarity(Rarity)`, `maxRarity(Rarity)`, `rarityTable(RarityTable)` e `randomRarity()`. Zero o più di una fonte → `IllegalStateException`.
  - **Payload per famiglia** (`generate()` fa `switch` sul tipo risolto):
    - `BUFF` → un singolo `BuffElement` ottenuto delegando a `BuffDebuffGeneratorTool` con la rarità risolta (si prende `buffs.get(0)`: ogni rarità produce sempre ≥1 buff). `value` resta `0`, `debuff` `null`.
    - `DEBUFF` → `BuffDebuffGeneratorTool` non genera debuff (lacuna nota, vedi `buffdebuffgenerator`), quindi si genera un `BuffElement` e lo si **converte** in `DebuffElement` (stessi `Characteristic`+`value`). `value` `0`, `buff` `null`.
    - `HEALTH_REGENERATION` / `MANA_REGENERATION` → `value` (punti vita/mana rigenerati) pescato in un intervallo dipendente dalla rarità, fornito da `PotionRules`. `buff`/`debuff` `null`.
  - **Niente `noStatusEffect()`**: per una pozione il payload (buff/debuff/rigenerazione) è intrinseco alla famiglia, non un extra opzionale.
- `potiongenerator.rules` — `RegenerationRange(int minValue, int maxValue)` (gemello di `AttackRange`), interfaccia `PotionRules` (`regenerationFor(Rarity)`), `DefaultPotionRules` (mappa `Rarity` → `RegenerationRange`, `EnumMap`). Range di default (stessi per vita e mana): COMMON `[5,10]`, UNCOMMON `[10,20]`, RARE `[20,35]`, EPIC `[35,55]`, LEGENDARY `[55,90]`. `rules(PotionRules)` opzionale per range custom.
- `potiongenerator.result.PotionResult` — `record PotionResult(PotionType type, Rarity rarity, int value, BuffElement buff, DebuffElement debuff) implements GeneratedElementResult`, con `Builder` interno. A differenza degli altri result usa **singoli** `BuffElement`/`DebuffElement` (non liste): i campi non pertinenti alla famiglia sono `null` (`buff`/`debuff`) o `0` (`value`).

```java
PotionGeneratorTool.building().type(PotionType.BUFF).rarity(Rarity.EPIC).generate();
PotionGeneratorTool.building().type(PotionType.DEBUFF).maxRarity(Rarity.RARE).generate();
PotionGeneratorTool.building().type(PotionType.HEALTH_REGENERATION).rarity(Rarity.COMMON).generate();  // value in [5,10]
PotionGeneratorTool.building().randomType().randomRarity().generate();
```

### `charactergenerator` — generazione di personaggi con razza, classe, nome e caratteristiche

- `charactergenerator.CharacterGeneratorTool` — `building()` → `Builder` → `generate()` → `CharacterResult`. Modellato su `ArmourGeneratorTool` (stesso pattern builder e helper `countTrue(boolean...)`); non usa rarità né buff/debuff, ma applica i bonus di razza e classe alle caratteristiche.
  - **Razza**: `race(Race)` per una fissa, oppure `randomRace()` per una casuale tra tutti i `Race.values()`. Nessuno dei due o entrambi → `IllegalStateException`.
  - **Classe**: `characterClass(CharacterClass)` per una fissa, oppure `randomClass()` per una casuale tra tutti i `CharacterClass.values()`. Nessuno dei due o entrambi → `IllegalStateException`. Ogni personaggio **ha sempre** una classe (stesso contratto della razza).
  - **Nome**: sempre generato internamente riusando `CharacterNameGeneratorTool` con la razza risolta; `addNickname()` (opzionale) aggiunge il soprannome.
  - **Caratteristiche**: esattamente **una** tra `characteristics(List<Characteristic>)` (lista esplicita) e `allCharacteristics()` (tutte le `Characteristic.values()`). Zero o entrambe → `IllegalStateException`. La lista passata è **de-duplicata preservando l'ordine di inserimento** (`LinkedHashSet`); se dopo il dedup è vuota → `IllegalStateException`.
  - **Punti**: `totalPoints(int)` è **obbligatorio** (altrimenti `IllegalStateException`): è il monte punti totale distribuito tra le caratteristiche. `minCharacteristicValue(int)` è il valore minimo garantito a ogni caratteristica (**default `1`**, non negativo). Se `totalPoints < minCharacteristicValue * count` → `IllegalStateException` diagnostica.
  - **Distribuzione**: ogni caratteristica parte dal minimo, poi i punti residui (`totalPoints - min * count`) vengono assegnati **uno alla volta a caratteristiche casuali**. Es: 3 caratteristiche, min `1`, totale `4` → tutte a `1` e l'unico punto restante va a caso a una delle tre. La somma dopo la sola distribuzione è esattamente `totalPoints`.
  - **Bonus di razza e classe**: dopo la distribuzione vengono applicati, come **ultimi step** (prima la razza, poi la classe), i bonus additivi definiti da `RaceBonusTable` e `ClassBonusTable`. Entrambe le tabelle usano di default `withDefaultBonuses()`; `raceBonusTable(RaceBonusTable)` e `classBonusTable(ClassBonusTable)` permettono di sovrascriverle, e una tabella **vuota** (`builder().build()`) è l'opt-out dai relativi bonus. Con i default attivi la somma finale è `totalPoints + bonusRazza + bonusClasse`. Se un bonus (di razza o di classe) punta a una caratteristica **non presente** nel personaggio generato → `IllegalStateException` diagnostica (il bonus non viene ignorato né aggiunto). Conseguenza: con le tabelle di default attive non si può generare un personaggio con un sottoinsieme di caratteristiche che escluda i target dei bonus della razza/classe risolte.
  - **Verbose**: `verbose()` (opzionale, default off) fa loggare a `generate()` le fasi della generazione su `System.out` con prefisso `[CharacterGenerator]`: razza e classe risolte (con `(fixed)`/`(random)`), nome, caratteristiche selezionate, punti+minimo, stato dopo la distribuzione e **quali/quanto** bonus vengono applicati per razza e per classe (es. `Applying bonus from race ORC (total +3): STRENGTH +2, RESISTANCE +1`), infine lo stato del personaggio con la somma. È puramente osservazionale: con `verbose` off non stampa nulla e il comportamento (valori, validazioni, eccezioni) è identico. Scelta di `System.out` coerente con lo stile self-contained del toolkit (nessuna dipendenza di logging).
- `charactergenerator.result.CharacterCharacteristic` — `record CharacterCharacteristic(Characteristic characteristic, int value)`, nello stile di `BuffElement`. Contenitore dedicato (non un `Map`) pensato per crescere con campi futuri senza toccare gli altri result.
- `charactergenerator.result.CharacterResult` — `record CharacterResult(Race race, CharacterClass characterClass, String name, List<CharacterCharacteristic> characteristics) implements GeneratedElementResult`, con `Builder` interno.

```java
CharacterGeneratorTool.building().race(Race.ELF).characterClass(CharacterClass.MAGE).allCharacteristics().totalPoints(30).generate();
CharacterGeneratorTool.building().randomRace().randomClass().addNickname().allCharacteristics().totalPoints(50).generate();
CharacterGeneratorTool.building().race(Race.ORC).characterClass(CharacterClass.WARRIOR).allCharacteristics().minCharacteristicValue(0).totalPoints(10).generate();
CharacterGeneratorTool.building().race(Race.HUMAN).characterClass(CharacterClass.THIEF).characteristics(List.of(Characteristic.STRENGTH)).totalPoints(1).raceBonusTable(RaceBonusTable.builder().build()).classBonusTable(ClassBonusTable.builder().build()).generate();  // opt-out da entrambi i bonus
CharacterGeneratorTool.building().race(Race.ORC).characterClass(CharacterClass.WARRIOR).allCharacteristics().totalPoints(20).verbose().generate();  // logga fasi e bonus su System.out
```

> **Nota sulla duplicazione.** `JewelGeneratorTool`, `WeaponGeneratorTool`, `ArmourGeneratorTool` e `PotionGeneratorTool` condividono per copia la logica di risoluzione rarità (e i primi tre anche quella buff/debuff). `CharacterGeneratorTool` riusa lo stesso pattern builder e l'helper `countTrue(boolean...)` per la validazione delle sorgenti (razza, classe e caratteristiche), ma non ha rarità né buff/debuff. `RaceBonusTable` e `ClassBonusTable` sono a loro volta gemelle per copia (chiave `Race` vs `CharacterClass`); l'applicazione dei bonus è invece unificata **dentro** `CharacterGeneratorTool` in un helper condiviso (`applyBonuses`). Scelta coerente con lo stile self-contained del progetto; un'eventuale centralizzazione delle tabelle in `core` sarebbe un refactor trasversale.

### `dungeongenerator` — generazione di dungeon a stanze collegate

- `dungeongenerator.DungeonGenerationTool` — `building()` → `Builder` → `generate()` → `DungeonResult`. Non usa rarità né buff/debuff: costruisce una mappa di stanze connesse e vi distribuisce eventi, nemici, trappole e scrigni.
  - **Numero di stanze**: `numberOfChambers(int)` è **obbligatorio** (altrimenti `IllegalStateException`) e deve essere almeno `2` (stanza d'ingresso + stanza finale); sotto `2` → `IllegalStateException`. La stanza `0` è sempre `ENTRY`, l'ultima `FINAL`, le intermedie `STANDARD`.
  - **Eventi principali** (`MainEvent`, identificati da un `code`): `mainEvent()` / `randomPositionMainEvent()` generano un codice automatico (`MainEvent_<n>`, contatore progressivo), le overload `mainEvent(String)` / `randomPositionMainEvent(String)` accettano un codice esplicito. I `mainEvent*` finiscono **sempre nella stanza finale**; i `randomPositionMainEvent*` in una stanza casuale **diversa dall'ingresso** (indice `1..numberOfChambers-1`). Invocabili più volte: ogni chiamata aggiunge un evento.
  - **Nemici**: `numberOfEnemy(int)` (default `0`, negativo → `IllegalStateException`) è il totale distribuito **uniformemente** tra tutte le stanze (base uguale per tutte, il resto va a stanze casuali).
  - **Scrigni**: `numberOfChests(int)` (default `0`, negativo → `IllegalStateException`), distribuiti uniformemente come i nemici.
  - **Trappole**: `haveTraps()` (opt-in) fa sì che `generate()` scelga un numero casuale di trappole in `[0, numberOfChambers]`, poi distribuito uniformemente; senza `haveTraps()` le trappole sono `0`.
  - **Connessioni**: si costruisce prima uno **spanning tree** casuale che garantisce la raggiungibilità di ogni stanza, poi si aggiungono fino a `numberOfChambers/2` connessioni extra casuali. Ogni `ChamberConnection` è normalizzata (`from = min`, `to = max`) e deduplicata (`LinkedHashSet`).
- `dungeongenerator.result.DungeonResult` — `record DungeonResult(int numberOfChambers, List<Chamber> chambers, List<ChamberConnection> connections, int numberOfEnemies, int numberOfTraps, int numberOfChests) implements GeneratedElementResult`, con `Builder` interno.
- `dungeongenerator.result.Chamber` — `record Chamber(int id, ChamberType type, List<MainEvent> mainEvents, int enemyCount, int trapCount, int chestCount)` (difensivo: `List.copyOf` sui `mainEvents`).
- `dungeongenerator.result.ChamberConnection` — `record ChamberConnection(int fromChamberId, int toChamberId)`; il compact constructor ordina gli id (`min`/`max`) così che A→B e B→A siano la stessa connessione.
- `dungeongenerator.result.MainEvent` — `record MainEvent(String code)`.
- `dungeongenerator.render.DungeonAsciiRenderer` — renderer statico (`render(DungeonResult)` → `String`), **non** un `*Tool`. Disegna le stanze come box ASCII con corridoi, glifi `<`/`>` per ingresso/finale e un'etichetta di stats per stanza (`!N` main event, `eN` nemici, `^N` trappole, `$N` scrigni, `#<id>` id stanza); affianca alla mappa una legenda in colonna. `render(null)` → `IllegalArgumentException`.

```java
DungeonResult dungeon = DungeonGenerationTool.building()
        .numberOfChambers(6).mainEvent().randomPositionMainEvent()
        .numberOfEnemy(10).numberOfChests(3).haveTraps().generate();
String map = DungeonAsciiRenderer.render(dungeon);
```

### `dicelauncher` — lancio di gruppi di dadi

- `dicelauncher.DiceLauncherTool` — `building()` → `Builder` → `roll()` → `DiceRollResult`. **Unico tool che chiude con `roll()` invece di `generate()`** (non genera un elemento di dominio ma un tiro di dadi).
  - **Gruppi di dadi**: `dice(int numberOfDice, int numberOfFaces)` oppure l'overload `dice(int, int, String code)` con un codice/etichetta opzionale. Invocabile più volte per accumulare gruppi eterogenei (es. `2d6` + `1d20`). Validazione **immediata** al momento dell'aggiunta: `numberOfDice < 1` o `numberOfFaces < 2` → `IllegalArgumentException`. `roll()` senza alcun gruppo → `IllegalStateException`.
  - Ogni faccia è pescata in `[1, numberOfFaces]`; il `subtotal` di un gruppo è la somma dei suoi dadi, il `total` del result la somma di tutti i subtotali.
- `dicelauncher.result.DiceRoll` — `record DiceRoll(int numberOfDice, int numberOfFaces, String code, List<Integer> results, int subtotal)`. **Non** implementa `GeneratedElementResult`: è un elemento interno al result, non un result a sé.
- `dicelauncher.result.DiceRollResult` — `record DiceRollResult(List<DiceRoll> rolls, int total) implements GeneratedElementResult`, con `Builder` interno.

```java
DiceRollResult result = DiceLauncherTool.building()
        .dice(2, 6).dice(1, 20, "attacco").roll();
result.total();   // somma di tutti i dadi
```

Non ci sono database né configurazione esterna: le liste `.txt` sono l'unica origine dati per i nomi. Aggiungere un nome = aggiungere una riga al file corrispondente.

## Convenzioni del progetto

- **Codice in inglese** (classi, metodi, variabili, messaggi). Anche i **nomi dei file di risorse sono in inglese** e vanno mantenuti stabili così come sono: `humans_names.txt`, `elves_names.txt`, `orks_names.txt`, `undeads_names.txt`, `nicknames.txt`.
- **Risorse**: tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`. Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- **Package**: i tool stanno sotto `it.fantasytoolkit.*` (coerente con `groupId`), mentre il modello di dominio sta sotto `it.fantasytoolkitcore.core.*`. I `*Tool` hanno costruttore privato ed entry-point statico `building()`.
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente con l'helper di test `tools.FileReader.readLines(path)` (restituisce un `Set<String>`).
