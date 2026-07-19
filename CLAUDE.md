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

## Documentazione delle API pubbliche

La documentazione destinata ai consumatori della libreria si trova in
`docs/agent/INDEX.md`.

Quando devi **utilizzare** un'API pubblica:

1. consulta prima `docs/agent/INDEX.md`;
2. leggi soltanto il documento del modulo interessato;
3. considera la documentazione come **contratto pubblico**;
4. ispeziona l'implementazione soltanto quando devi **modificare** la libreria
   oppure quando la documentazione risulta incompleta o incoerente col codice.

Quando **modifichi** un tool, aggiorna anche il relativo documento in
`docs/agent/`: la documentazione e il codice devono restare coerenti.

Mappa modulo → documento:

| Modulo (package) | Documento |
|------------------|-----------|
| `core` | `docs/agent/core.md` |
| `namegenerator` | `docs/agent/name-generator.md` |
| `jewelgenerator` | `docs/agent/jewel-generator.md` |
| `buffdebuffgenerator` | `docs/agent/buff-debuff-generator.md` |
| `weapongenerator` | `docs/agent/weapon-generator.md` |
| `armourgenerator` | `docs/agent/armour-generator.md` |
| `potiongenerator` | `docs/agent/potion-generator.md` |
| `charactergenerator` | `docs/agent/character-generator.md` |
| `dungeongenerator` | `docs/agent/dungeon-generator.md` |
| `dicelauncher` | `docs/agent/dice-launcher.md` |

## Architettura

Toolkit per giochi fantasy, organizzato in **moduli logici (package)** dentro un singolo modulo Maven. Non ci sono database né configurazione esterna: le liste `.txt` del classpath sono l'unica origine dati (per i nomi).

### Pattern comune dei tool

- Ogni generatore pubblico è un `*Tool` con **costruttore privato** ed entry-point statico `building()`, che stacca un `Builder` interno fluente.
- La catena del builder si chiude con `generate()` (o `roll()` per `DiceLauncherTool`) e restituisce un **result immutabile** (`record`).
- Ogni result implementa `GeneratedElementResult` (`core.pojo.GeneratedElementResult`), oggi **marker interface vuota**: un contratto-tag comune tra i result, senza metodi.
- I metodi del builder sono invocabili in **qualsiasi ordine**; la validazione avviene in `generate()`/`roll()` (salvo `DiceLauncherTool.dice(...)`, che valida subito). Gli errori di configurazione sono `IllegalStateException` (`IllegalArgumentException` per la validazione immediata dei dadi).
- I `*Tool` usano internamente `new Random()`: la generazione è **casuale e non riproducibile** (nessun seme esposto).
- Molti generatori condividono per **copia** la logica di risoluzione rarità (jewel, weapon, armour, potion) e buff/debuff (jewel, weapon, armour). È una scelta coerente con lo stile self-contained del progetto: un'eventuale centralizzazione in `core` sarebbe un refactor trasversale, non un intervento locale.

### Moduli

- `core` (`it.fantasytoolkitcore.core`) — modello di dominio (enum), tabelle rarità/bonus, marker result. Vocabolario condiviso.
- `namegenerator` — nomi di personaggio da liste del classpath (motore base `NameGeneratorTool` + `CharacterNameGeneratorTool`).
- `buffdebuffgenerator` — buff/debuff per rarità; riusato da jewel/weapon/armour/potion. **Nota**: i debuff non sono ancora generati (`debuffs` sempre vuota).
- `jewelgenerator`, `weapongenerator`, `armourgenerator` — equipaggiamento con rarità e status effect (weapon aggiunge `attack`, armour `defense`).
- `potiongenerator` — pozioni con payload per famiglia (`PotionType`).
- `charactergenerator` — personaggi con razza, classe, nome e caratteristiche (bonus di razza/classe).
- `dungeongenerator` — dungeon a stanze collegate + renderer ASCII (`DungeonAsciiRenderer`, non un `*Tool`).
- `dicelauncher` — lancio di gruppi di dadi (chiude con `roll()`).

Per API, builder, parametri, result, errori ed esempi di ogni modulo, vedi il documento corrispondente in `docs/agent/`.

## Convenzioni del progetto

### Codice e package

- **Codice in inglese** (classi, metodi, variabili, messaggi). Anche i **nomi dei file di risorse sono in inglese** e vanno mantenuti stabili: `humans_names.txt`, `elves_names.txt`, `orks_names.txt`, `undeads_names.txt`, `nicknames.txt`.
- **Package**: i tool stanno sotto `it.fantasytoolkit.*` (coerente con `groupId`), mentre il modello di dominio sta sotto `it.fantasytoolkitcore.core.*`. I `*Tool` hanno costruttore privato ed entry-point statico `building()`.
- I result sono `record` immutabili con `Builder` interno fluente.

### Risorse

- Tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`.
- Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- Aggiungere un nome = aggiungere una riga al file corrispondente.

### Test

- JUnit Jupiter + AssertJ (`assertThat(...)`).
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente con l'helper di test `tools.FileReader.readLines(path)` (restituisce un `Set<String>`).

## Vincoli quando modifichi la libreria

- **Non rompere il contratto pubblico** documentato in `docs/agent/` senza aggiornare il documento corrispondente. Codice e documentazione devono restare coerenti.
- **Rispetta il pattern comune**: nuovo generatore = `*Tool` con costruttore privato, `building()` → `Builder` fluente → `generate()` (o `roll()`) → result `record` che implementa `GeneratedElementResult`.
- **Java 21**: usa pure le feature stabili (record, switch expression, pattern matching, sealed) ma niente preview feature non richieste.
- **Nessuna dipendenza esterna nuova** se non strettamente necessaria: il progetto è self-contained (niente logging framework — `verbose` usa `System.out`; nessun DB; nessuna config esterna).
- **Validazione ai boundary**: valida gli input nei metodi pubblici del builder; solleva eccezioni diagnostiche (`IllegalStateException`/`IllegalArgumentException`) con messaggi chiari, non `null` opachi.
- **Estensioni di dominio in `core`**: nuove razze, rarità, tipi, caratteristiche o classi vanno aggiunte agli enum di `core.model` (per una nuova razza serve anche il file di nomi).
- **Punti di estensione via regole**: per cambiare combinazioni/range senza toccare i tool, usa le interfacce `*Rules` (`BuffDebuffRules`, `WeaponRules`, `ArmourRules`, `PotionRules`) e le tabelle di bonus (`RaceBonusTable`, `ClassBonusTable`), passandone implementazioni/istanze custom al builder.
- **Coerenza di stile**: mantieni i pattern già adottati anche quando esistono per copia; non introdurre astrazioni premature né refactor trasversali non concordati.
