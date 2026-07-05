# IMPLEMENTATION — Risultati di nome strutturati e riproducibili tramite seed

**Specifica di riferimento:** `spec-seeded-name-result.md`  — nel resto del documento: «la SPEC».
**Stato:** `IN_PROGRESS`  <!-- NOT_STARTED | IN_PROGRESS | BLOCKED | COMPLETED -->

Documento di lavoro: la SPEC (il "cosa") resta stabile; qui vivono stato, piano, decisioni e problemi (il "come").

## Regole per l'agente
- Leggere `CLAUDE.md` (se presente) e la SPEC prima di toccare codice.
- Alla ripresa del lavoro, leggere prima questo file e riprendere dallo stato corrente.
- Prima di modificare, elencare i file che verranno toccati. Nessun refactoring fuori scope.
- Non modificare i requisiti della SPEC senza decisione esplicita.
- Dopo ogni fase: eseguire i test pertinenti e aggiornare questo file. Spuntare una voce solo dopo verifica reale, mai a priori.
- Scelta che **non** cambia il comportamento osservabile → procedi e annotala in *Decisioni*.
- Scelta che **cambia** comportamento o criteri di accettazione, o ambiguità non risolvibile dalla SPEC → **fermati**, imposta lo stato a `BLOCKED` e registra in *Problemi aperti* / *Deviazioni*.
- Nota ambiente: la macchina ha JDK 17 come default e non compila Java 21. Prima dei comandi Maven puntare `JAVA_HOME` al JDK 21 (vedi `CLAUDE.md` del progetto).

## Piano operativo

**Fase 1 — Analisi**
- [x] Individuati i punti del codice coinvolti e le convenzioni/pattern già presenti (motore `NameGeneratorTool`, tool statici, enum `Race`, risorse `.txt` dal classpath).
- [x] Confermate le assunzioni della SPEC (motore con `Random` interno non seminabile, tipi staged `Seed`/`GeneratedElementResult`/`NameResult` con i relativi difetti, stile dei test).
- [x] Rilevato lo stile dei test esistenti (JUnit Jupiter + AssertJ; verifica di appartenenza al dizionario via `AbstractNameGeneratorTest.readLines`).
- [x] Compilata la lista "File coinvolti (effettivi)".

**Fase 2A — Implementazione (`Seed` + `SeedBuilder`, nel modulo `core`)** — RIVISTA (design cambiato: `long` ovunque)
- [x] `Seed` (record `Seed(long value)`): **nessuna** stringa, **nessuna** conversione. Accessor `value()` restituisce direttamente il `long`; `toString()` restituisce `Long.toString(value)`. Rimossi `of(String)`, `random()`, `longValue()`, `Long.parseLong` e `ThreadLocalRandom` da `Seed`.
- [x] `SeedBuilder` (`src/main/java/it/fantasytoolkit/core/types/SeedBuilder.java`): builder **solo per la generazione casuale**, nasconde `ThreadLocalRandom` in un metodo privato. Entry-point `SeedBuilder.newSeed()`, `build()` → `Seed` casuale. Il valore noto (riproduzione) si ottiene con `new Seed(long)`, non dal builder.
- [x] Aggiornato `SeedTest` al nuovo design (value su `long`, toString, **determinismo PRNG a parità di seed**) e aggiunto `SeedBuilderTest` (build produce Seed valido; due build non sempre identici). Esito: `Tests run: 5, Failures: 0` — BUILD SUCCESS (JDK 21).
- [x] Revisione `java-functional-evolver` eseguita: unica modifica di valore (`for`+`HashSet` di `SeedBuilderTest` → stream); resto invariato. Test ri-verificati verdi.
- [ ] **STOP: verifica utente in corso prima di procedere alla Fase 2B.**

**Fase 2B — Implementazione (result, motore, tool)**
- [ ] `GeneratedElementResult`: trasformarlo da classe base a **interfaccia** con il solo contratto `Seed seed()` (contratto comune dei result, condiviso dai tool futuri). Eliminare il POJO mutabile e `setSeed`.
- [ ] `NameResult`: diventa `record NameResult(String name, Seed seed) implements GeneratedElementResult`, con **`Builder` interno** fluente ed elegante (`name(...)`, `seed(...)`, `build()`). Eliminare la vecchia classe con `setName(String, Seed)` (bug: ignora il seed) e gli overload/setter mutabili.
- [ ] `NameGeneratorTool`: esporre la selezione con un `Random` fornito dall'esterno (es. `pick(Random)`), **unico punto di estrazione**; rimuovere il `Random` interno non seminabile.
- [ ] `HeroNameGeneratorTool`: `generateName()` (nuovo seed, delega a `generateName(Seed)`) e `generateName(Seed)`; ordine di pesca fisso (nome poi epiteto) dal medesimo `Random` seminato; `name` = `"nome epiteto"`.
- [ ] `EnemyNameGeneratorTool`: `generateName(Race)` (nuovo seed, delega a `generateName(Race, Seed)`) e `generateName(Race, Seed)`.
- [ ] Mantenere invariati caricamento liste, messaggi d'errore e nomi dei file di risorse.
- [ ] Nessun refactoring fuori scope.

**Fase 3 — Test**
- [ ] Un test per ciascun criterio della *Definition of done* della SPEC.
- [ ] Test dedicato del `Builder` di `NameResult` (valorizzazione via `name(...)`/`seed(...)`/`build()`).
- [ ] Adeguare i test esistenti al nuovo contratto `NameResult` (niente più `split` su String grezza in `HeroNameGeneratorToolTest`; accessor `name()`/`seed()`).
- [ ] Eseguire la suite del modulo (`mvn test` con `JAVA_HOME` sul JDK 21).

**Fase 4 — Revisione**
- [ ] Coerenza con la SPEC; nessuna modifica non richiesta a `Race` o ai dizionari.
- [ ] Quality gate del progetto superate (se configurate).
- [ ] Aggiornare *Decisioni/Deviazioni* e portare lo stato a `COMPLETED`.

## File coinvolti (effettivi)
Provvisori dall'analisi — da confermare in Fase 1 (formato: `` `path/File.java` — motivo``).
- `src/main/java/it/fantasytoolkit/core/types/Seed.java` — record `Seed(long value)`, dato puro, nessuna conversione.
- `src/main/java/it/fantasytoolkit/core/types/SeedBuilder.java` — builder di generazione casuale (`newSeed().build()`), nasconde `ThreadLocalRandom`.
- `src/main/java/it/fantasytoolkit/core/pojo/GeneratedElementResult.java` — da classe base a **interfaccia** `Seed seed()`; rimozione del POJO mutabile e `setSeed`.
- `src/main/java/it/fantasytoolkit/namegenerator/result/NameResult.java` — `record` che implementa `GeneratedElementResult` + `Builder` interno; rimozione bug/overload.
- `src/main/java/it/fantasytoolkit/namegenerator/tool/NameGeneratorTool.java` — selezione con `Random` esterno seminato, senza duplicazione del percorso casuale.
- `src/main/java/it/fantasytoolkit/namegenerator/HeroNameGeneratorTool.java` — coppia `generateName()` / `generateName(Seed)`, ordine di pesca fisso, `NameResult`.
- `src/main/java/it/fantasytoolkit/namegenerator/EnemyNameGeneratorTool.java` — coppia `generateName(Race)` / `generateName(Race, Seed)`, `NameResult`.
- `src/test/java/it/fantasytoolkit/namegenerator/HeroNameGeneratorToolTest.java` — adeguamento al contratto `NameResult` + test di riproducibilità.
- `src/test/java/it/fantasytoolkit/namegenerator/EnemyNameGeneratorToolTest.java` — adeguamento al contratto `NameResult` + test di riproducibilità.
- `src/test/java/it/fantasytoolkit/namegenerator/AbstractNameGeneratorTest.java` — riuso di `readLines` per la verifica di appartenenza (adeguamento solo se necessario).

## Registro
Voci datate (`YYYY-MM-DD`), append-only.

- **Decisioni tecniche** (non cambiano il comportamento)
  - 2026-07-05 — Seed in forma numerica (`value` = testo di un `long`, `Long.parseLong`); seed casuale via `ThreadLocalRandom`. Seed-stringa alfanumerici arbitrari rimandati.
  - 2026-07-06 — SUPERA la precedente: `Seed` diventa `record Seed(long value)`, `long` ovunque, zero conversioni (niente `of(String)`/`longValue()`/`Long.parseLong`). La generazione casuale è estratta in un `SeedBuilder` (`newSeed().build()`) che nasconde `ThreadLocalRandom`; il builder copre **solo** il random, il valore noto usa `new Seed(long)`.
  - 2026-07-06 — Regola aggiornata (CLAUDE.md): `java-functional-evolver` va invocato quasi sempre dopo aver scritto Java; è l'agente a decidere se intervenire.
  - 2026-07-05 — `GeneratedElementResult` diventa interfaccia (`Seed seed()`); `NameResult` è un `record` con `Builder` interno testato. Accessor in stile record (`name()`/`seed()`).
  - 2026-07-05 — Percorso casuale genera un seed e delega al percorso seminato: unico punto di estrazione.
- **Deviazioni dalla SPEC** (da motivare) — nessuna.
- **Problemi aperti** (bloccano l'avanzamento) — nessuno.
- **Test eseguiti**
  - 2026-07-05 — `mvn test -Dtest=SeedTest` (JDK 21): `Tests run: 5, Failures: 0, Errors: 0` — BUILD SUCCESS. [design superato: vedi 2026-07-06]
  - 2026-07-06 — `mvn test -Dtest=SeedTest,SeedBuilderTest` (JDK 21): `Tests run: 5, Failures: 0, Errors: 0` — BUILD SUCCESS. Nuovo design `Seed(long)` + `SeedBuilder`.
- **Revisione funzionale (`java-functional-evolver`)**
  - 2026-07-06 — Invocato sui 4 file. Unica modifica: `SeedBuilderTest.repeatedBuildsAreNotAlwaysIdentical` da `for`+`HashSet` mutato a `IntStream.range(...).mapToObj(...).collect(toSet())`. `Seed`, `SeedBuilder`, `SeedTest` lasciati invariati (già idiomatici). Test ri-verificati verdi.

## Punti da decidere
- **Formato del seed.** ✅ DECISO: forma numerica — `Seed.value` è la rappresentazione testuale di un `long`, riprodotta con `Long.parseLong`; il `long` casuale è generato nel percorso "casuale". Il supporto a seed-stringa *arbitrari* alfanumerici (via hash stabile, evitando `String.hashCode` non contrattuale cross-JVM) è rimandato: possibile estensione futura, fuori scope.
- **Relazione percorso casuale / percorso seminato.** ✅ DECISO: il percorso "casuale" genera un seed e delega al percorso seminato → unico cammino di estrazione, zero duplicazione.
- **Modellazione dei result.** ✅ DECISO: `GeneratedElementResult` diventa **interfaccia** (`Seed seed()`); `NameResult` è un `record` che la implementa, con `Builder` interno testato. Scartata sia la classe base mutabile sia il record flat senza contratto condiviso.

## Esito finale
Da compilare a fine lavoro: stato finale, modifiche effettuate, test eseguiti, note residue.

## Esempio (concreto: contratto `NameResult` + seed riproducibile)
```java
// File coinvolti (effettivi) — previsti:
//   Seed                     — value (testo di un long) -> long per Random; Seed.random()
//   GeneratedElementResult   — interfaccia: Seed seed()
//   NameResult               — record (String name, Seed seed) implements GeneratedElementResult + Builder
//   NameGeneratorTool        — selezione con Random esterno seminato
//   HeroNameGeneratorTool    — generateName() / generateName(Seed), name = "nome epiteto"
//   EnemyNameGeneratorTool   — generateName(Race) / generateName(Race, Seed)
//   HeroNameGeneratorToolTest / EnemyNameGeneratorToolTest — casi della DoD

// Test: uno per criterio della DoD
// (il criterio 7 "nessuna modifica non richiesta" è verificato in revisione, non con un test)
@Test void hero_nameComposto_daDizionari_conSeedNonNullo()  { /* DoD 1 */ }
@Test void enemy_name_daDizionarioRazza_conSeedNonNullo()   { /* DoD 2 */ }
@Test void hero_stessoSeed_stessoName()                     { /* DoD 3: genera, cattura seed, rigenera */ }
@Test void enemy_stessoSeed_stessoName()                    { /* DoD 4: genera, cattura seed, rigenera */ }
@Test void risorsaMancante_lanciaIllegalStateException()    { /* DoD 5 */ }
@Test void risultatiImmutabili_nessunSetter()               { /* DoD 6: verifica contratto/compilazione */ }
```
