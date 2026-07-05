# SPEC — Risultati di nome strutturati e riproducibili tramite seed

**Obiettivo:** far ritornare ai generatori di nomi un risultato strutturato `NameResult` invece di una `String` e introdurre un meccanismo di seed riproducibile, riusabile anche dai tool futuri.

**Contesto**
- Punti del codice interessati: motore `namegenerator.tool.NameGeneratorTool` e i tool pubblici `namegenerator.HeroNameGeneratorTool` (compone `name + " " + epithet` da `heros_names.txt` e `hero_suffix.txt`) e `namegenerator.EnemyNameGeneratorTool` (nome singolo, lista scelta da `Race.getNamesFile()`).
- Pattern o meccanismi esistenti da riusare: `NameGeneratorTool` come mattone base (carica una lista dal classpath, una parola per riga, UTF-8, righe vuote scartate; `IllegalStateException` se risorsa mancante o lista vuota); `java.util.Random` già usato internamente per l'estrazione; enum `core.model.Race` come mappa razza→file; helper di test `AbstractNameGeneratorTest.readLines` che rilegge il dizionario dal classpath.
- File / moduli coinvolti: `namegenerator/tool/NameGeneratorTool.java`, `namegenerator/HeroNameGeneratorTool.java`, `namegenerator/EnemyNameGeneratorTool.java`, `core/types/Seed.java` (record già abbozzato: `record Seed(String value)` con factory `of` e `toString`=value), `core/pojo/GeneratedElementResult.java` (oggi POJO mutabile con `setSeed` fluente), `namegenerator/result/NameResult.java` (oggi mutabile, con `setName(String, Seed)` che ignora il seed e doppio overload di `setName`), più i test `HeroNameGeneratorToolTest`, `EnemyNameGeneratorToolTest`, `AbstractNameGeneratorTest`.

**Comportamento atteso**
- I tool pubblici ritornano un `NameResult` che espone il nome generato e il `Seed` con cui è stato prodotto.
- Ogni tool offre due percorsi: uno "casuale" (crea un nuovo seed e genera) e uno "da seed" (riproduce a partire da un seed dato).
  - Hero: `NameResult generateName()` e `NameResult generateName(Seed seed)`. Il campo `name` del risultato è la stringa composta `"name epithet"`.
  - Enemy: `NameResult generateName(Race race)` e `NameResult generateName(Race race, Seed seed)`.
- Riproducibilità: passando a un tool lo stesso `Seed` restituito da una generazione precedente si riottiene lo stesso `name`, **a parità di dizionari** (contenuto e ordine delle liste `.txt` invariati).
- Il `Seed` è il seme del PRNG: dal suo valore testuale si deriva in modo deterministico il `long` che inizializza `java.util.Random`. Non incorpora il valore selezionato.
- Ordine di pesca fisso: quando da un solo seed si eseguono più estrazioni (Hero: prima il nome, poi l'epiteto) l'ordine è fissato e documentato; con lo stesso `Random` seminato le estrazioni avvengono sempre nella stessa sequenza.
- Casi limite: risorsa mancante o lista vuota → `IllegalStateException` con messaggio diagnostico (comportamento invariato); il `Seed` di un `NameResult` non è mai `null`; nome generato mai vuoto/blank.
- Invariante: la logica di caricamento delle liste (formato, UTF-8, trim, scarto righe vuote, path assoluto dal classpath) resta identica; i nomi dei file di risorse restano invariati.

**Vincoli**
- Cambio di contratto pubblico previsto e voluto (`String` → `NameResult`): i chiamanti e i test vanno adeguati; nessun altro comportamento va alterato.
- Riusare il motore `NameGeneratorTool` come base; nessuna logica duplicata tra percorso casuale e percorso seminato.
- Tipi risultato immutabili come `record`, con un `Builder` interno fluente ed elegante per la valorizzazione (evita costruttori lunghi al crescere delle proprietà); il builder va testato.
- `GeneratedElementResult` diventa un'**interfaccia** (contratto comune `Seed seed()`), non una classe base: i result sono `record` che la implementano, così il seed resta condiviso da tutti i tool futuri senza vincolare l'ereditarietà. `NameResult` è un `record ... implements GeneratedElementResult`.
- Nessuna nuova dipendenza esterna; usare solo JDK 21 e Lombok già presente.
- Convenzioni di progetto: codice in inglese; nomi dei file di risorse invariati; risorse in `src/main/resources/namegenerator/` con path assoluto `/namegenerator/<file>.txt`; `*Tool` con costruttore privato e metodi statici; copia unica delle risorse in `main`.

**Fuori scope**
- Nuove razze, nuovi tool o nuovi tipi di risultato oltre a `NameResult`.
- Persistenza dei seed, serializzazione su file/DB, CLI o API esterne.
- Supporto a seed-stringa arbitrari via hash (vedi «da decidere»): la prima implementazione usa la forma numerica.
- Modifiche al contenuto dei dizionari `.txt`.

**Definition of done** — ogni criterio coperto da almeno un test
1. Hero: `generateName()` ritorna un `NameResult` con `name` = `"nome epiteto"`, dove `nome` appartiene a `heros_names.txt` ed `epiteto` a `hero_suffix.txt`, e con `seed` non nullo;
2. Enemy: `generateName(Race)` ritorna un `NameResult` il cui `name` appartiene al dizionario della razza, con `seed` non nullo;
3. Riproducibilità Hero: `generateName(seed)` con il seed restituito da una `generateName()` precedente produce lo stesso `name`;
4. Riproducibilità Enemy: `generateName(Race, seed)` con il seed restituito da una `generateName(Race)` precedente produce lo stesso `name`;
5. Casi limite invariati: risorsa mancante/lista vuota → `IllegalStateException`; nome mai blank;
6. `NameResult` è un `record` immutabile con `Builder` interno testato; `GeneratedElementResult` è un'interfaccia implementata da `NameResult`; eliminati i vecchi setter e il `setName(String, Seed)` che ignorava il seed;
7. nessuna modifica non richiesta a modello dati, `Race` o contenuto dei dizionari.

**Esempio** (istanza concreta — solo illustrativo)
```java
// Contratto: i tool ritornano NameResult (name + seed), non più String.
// Il Seed è il seme del PRNG: stesso seed + stessi dizionari => stesso name.

// Percorso casuale: crea un nuovo seed e genera.
NameResult first = HeroNameGeneratorTool.generateName();
Seed seed = first.seed();                  // es. Seed("6543210987654321")

// Percorso riproducibile: stesso seed => stesso name (a dizionari invariati).
NameResult again = HeroNameGeneratorTool.generateName(seed);
assert again.name().equals(first.name());

// Hero: ordine di pesca fisso e documentato dal medesimo Random seminato:
//   1) estrazione del nome da heros_names.txt
//   2) estrazione dell'epiteto da hero_suffix.txt
// => name = "<nome> <epiteto>"
```
