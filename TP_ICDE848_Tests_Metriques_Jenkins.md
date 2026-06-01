# TP — Tests & Métriques dans Jenkins
### ICDE848 · Mastère EISI 1ère année · Semestre 8

---

> **Point de départ.** Tu disposes déjà d'un projet Maven `tp-jenkins` avec trois classes métier : `Article`, `Panier`, `CommandeService`. Jenkins est installé et fonctionnel depuis le TP précédent.
>
> **Ce que tu vas construire dans ce TP.** Les tests unitaires, les tests d'intégration, les outils d'analyse qualité, et un Jenkinsfile qui publie tout ça automatiquement dans Jenkins.

---

## Rappel — ce qu'il y a déjà dans ton projet

```
tp-jenkins/
├── pom.xml                               ← À compléter au fil du TP
└── src/
    ├── main/java/fr/epsi/
    │   ├── model/
    │   │   ├── Article.java              ← fourni
    │   │   └── Panier.java               ← fourni
    │   └── service/
    │       └── CommandeService.java      ← fourni
    └── test/java/fr/epsi/service/
        ├── CommandeServiceTest.java      ← à créer (TP1)
        └── CommandeServiceIT.java        ← à créer (TP3)
```

### Les trois méthodes que tu vas tester

```java
// CommandeService.java

double calculerTotal(Panier panier)
// → calcule la somme prix × quantité de chaque article
// → lève IllegalArgumentException si panier null ou vide

double appliquerRemise(double total, int pourcentage)
// → applique une remise entre 0 et 100%
// → lève IllegalArgumentException si pourcentage invalide

String categoriserCommande(double total)
// → retourne "PETITE" si total < 50€
// → retourne "MOYENNE" si 50€ ≤ total < 200€
// → retourne "GRANDE" si total ≥ 200€
```

---

## TP1 — Écrire les tests unitaires

### Ce qu'est un test unitaire

Un test unitaire vérifie **une seule méthode, dans un seul cas précis**. Il est rapide (quelques millisecondes), indépendant des autres tests, et reproductible.

Le plugin Maven qui les exécute s'appelle **Surefire**. Il détecte automatiquement tous les fichiers dont le nom se termine par `Test.java`.

---

### Le pattern AAA

Tous tes tests doivent suivre cette structure en trois blocs commentés :

```java
@Test
void nomDuTest() {
    // GIVEN — préparer le contexte (objets, données)
    
    // WHEN — appeler la méthode testée (une seule ligne)
    
    // THEN — vérifier le résultat avec assertEquals / assertThrows
}
```

> **Pourquoi cette convention ?** Un test qui se lit comme une phrase est un test qu'on maintient. Dans six mois, "GIVEN un panier avec 3 stylos à 2€ / WHEN je calcule le total / THEN j'obtiens 6€" est immédiatement compréhensible.

---

### Convention de nommage

```
methode_Scenario_ResultatAttendu()
```

Exemples :
```java
calculerTotal_TroisStylos_RetourneSix()
calculerTotal_PanierNull_LeveException()
appliquerRemise_DixPourcent_RetourneQuatreVingtDix()
```

Ce nommage n'est pas une contrainte stylistique. C'est ce qui apparaît dans les rapports Jenkins — un test qui s'appelle `test3()` ne dit rien quand il échoue.

---

### Étape 1 — Ajouter JUnit 5 dans le pom.xml

Ouvre ton `pom.xml`. Ajoute la section `<dependencies>` si elle n'existe pas encore, et insère la dépendance JUnit 5 :

```xml
<dependencies>

    <!-- JUnit 5 — framework de tests -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>  <!-- présent uniquement à la compilation des tests -->
    </dependency>

</dependencies>
```

Vérifie que Maven télécharge bien la dépendance :

```bash
mvn dependency:resolve
# → [INFO] BUILD SUCCESS
```

---

### Étape 2 — Créer le fichier de test

Crée le fichier `src/test/java/fr/epsi/service/CommandeServiceTest.java`.

Commence par la structure vide :

```java
package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.model.Panier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandeServiceTest {

    private CommandeService service;
    private Panier panier;

    /**
     * @BeforeEach est exécuté avant CHAQUE test.
     * Chaque test repart d'un service et d'un panier neufs.
     * Sans ça, un test pourrait polluer l'état du suivant.
     */
    @BeforeEach
    void setUp() {
        service = new CommandeService();
        panier  = new Panier();
    }

    // ── Tes tests vont ici ──

}
```

---

### Étape 3 — Écrire les tests de `calculerTotal`

#### Cas nominal : panier avec un seul article

```java
@Test
@DisplayName("Total correct pour 3 stylos à 2€")
void calculerTotal_TroisStylos_RetourneSix() {
    // GIVEN
    panier.ajouter(new Article("Stylo", 2.0), 3);

    // WHEN
    double total = service.calculerTotal(panier);

    // THEN
    // assertEquals(valeurAttendue, valeurObtenue, delta)
    // Le delta 0.001 tolère les imprécisions des doubles
    assertEquals(6.0, total, 0.001);
}
```

#### Cas nominal : panier avec plusieurs articles différents

```java
@Test
@DisplayName("Total correct pour plusieurs articles")
void calculerTotal_PlusieursArticles_RetourneSomme() {
    // GIVEN — 3 stylos à 2€ = 6€, 2 cahiers à 5€ = 10€
    panier.ajouter(new Article("Stylo",  2.0), 3);
    panier.ajouter(new Article("Cahier", 5.0), 2);

    // WHEN
    double total = service.calculerTotal(panier);

    // THEN
    assertEquals(16.0, total, 0.001);
}
```

#### Cas d'erreur : panier vide

```java
@Test
@DisplayName("Panier vide lève une IllegalArgumentException")
void calculerTotal_PanierVide_LeveException() {
    // GIVEN — panier sans aucun article (setUp() en crée un vide)

    // WHEN + THEN — assertThrows vérifie que l'exception est bien levée
    assertThrows(
        IllegalArgumentException.class,
        () -> service.calculerTotal(panier)
    );
}
```

#### Cas d'erreur : panier null

```java
@Test
@DisplayName("Panier null lève une IllegalArgumentException")
void calculerTotal_PanierNull_LeveException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.calculerTotal(null)
    );
}
```

---

### Étape 4 — Écrire les tests de `appliquerRemise`

```java
@Test
@DisplayName("Remise 10% sur 100€ = 90€")
void appliquerRemise_DixPourcent_RetourneQuatreVingtDix() {
    // GIVEN — total brut = 100€, remise = 10%
    // WHEN
    double resultat = service.appliquerRemise(100.0, 10);
    // THEN
    assertEquals(90.0, resultat, 0.001);
}

@Test
@DisplayName("Remise 0% ne change pas le total")
void appliquerRemise_ZeroPourcent_RetourneTotalInchange() {
    double resultat = service.appliquerRemise(100.0, 0);
    assertEquals(100.0, resultat, 0.001);
}

@Test
@DisplayName("Remise négative lève une IllegalArgumentException")
void appliquerRemise_RemiseNegative_LeveException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.appliquerRemise(100.0, -5)
    );
}

@Test
@DisplayName("Remise > 100 lève une IllegalArgumentException")
void appliquerRemise_RemiseSupCent_LeveException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.appliquerRemise(100.0, 150)
    );
}
```

---

### Étape 5 — Écrire les tests de `categoriserCommande`

Ici, les cas les plus importants sont les **valeurs frontières** — ce sont les bugs les plus fréquents sur les conditions `< 50` et `< 200`.

```java
@Test
@DisplayName("30€ → catégorie PETITE")
void categoriser_TrenteEuros_RetournePetite() {
    assertEquals("PETITE", service.categoriserCommande(30.0));
}

@Test
@DisplayName("150€ → catégorie MOYENNE")
void categoriser_CentCinquanteEuros_RetourneMoyenne() {
    assertEquals("MOYENNE", service.categoriserCommande(150.0));
}

@Test
@DisplayName("500€ → catégorie GRANDE")
void categoriser_CinqCentsEuros_RetourneGrande() {
    assertEquals("GRANDE", service.categoriserCommande(500.0));
}

@Test
@DisplayName("Frontière : exactement 50€ → MOYENNE (pas PETITE)")
void categoriser_CinquanteEuros_RetourneMoyenne() {
    // Ce test vérifie que la condition est bien < 50 et non <= 50
    assertEquals("MOYENNE", service.categoriserCommande(50.0));
}

@Test
@DisplayName("Frontière : exactement 200€ → GRANDE (pas MOYENNE)")
void categoriser_DeuxCentsEuros_RetourneGrande() {
    assertEquals("GRANDE", service.categoriserCommande(200.0));
}
```

---

### Étape 6 — Lancer les tests localement

```bash
mvn clean test
```

Résultat attendu dans le terminal :

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Les rapports XML sont générés dans `target/surefire-reports/`. C'est ce dossier que Jenkins lira pour afficher les graphiques.

**Exercice de compréhension.** Dans `CommandeService.java`, modifie temporairement la condition `if (total < 50)` en `if (total <= 50)`. Relance `mvn clean test`. Quel test échoue ? Pourquoi ? Remets la condition d'origine.

---

## TP2 — Couverture de code avec JaCoCo

### Ce qu'est la couverture

JaCoCo instrumente le bytecode et mesure, pendant l'exécution des tests, quelles lignes et quelles branches ont été traversées. Le résultat est un pourcentage : **70% de couverture lignes** signifie que 30% du code n'est jamais exécuté par les tests.

> Une couverture élevée ne garantit pas l'absence de bugs — mais une couverture faible garantit des angles morts.

---

### Étape 1 — Ajouter JaCoCo dans le pom.xml

Dans la section `<build><plugins>`, ajoute ce plugin après Surefire :

```xml
<!-- ── JaCoCo : couverture de code ────────────────────────── -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>

        <!-- 1. Démarre l'agent AVANT les tests — OBLIGATOIRE -->
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>

        <!-- 2. Génère le rapport HTML après les tests -->
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>

        <!-- 3. Fait échouer le build si couverture < 70% -->
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>

    </executions>
</plugin>
```

---

### Étape 2 — Générer et lire le rapport

```bash
mvn clean verify
```

Ouvre le rapport HTML :

```
target/site/jacoco/index.html
```

Tu dois voir un tableau avec les colonnes suivantes :

| Colonne | Signification |
|---------|--------------|
| Instructions | % des instructions bytecode couvertes |
| Branches | % des branches `if/else` couvertes |
| Lines | % des lignes source couvertes |
| Methods | % des méthodes appelées au moins une fois |

Clique sur `CommandeService` pour voir ligne par ligne : **vert** = couvert, **rouge** = jamais exécuté, **jaune** = branche partiellement couverte.

**Question.** Quelle méthode a la couverture la plus faible ? Est-ce normal compte tenu des tests écrits ?

---

### Étape 3 — Tester le seuil

Supprime temporairement deux ou trois tests dans `CommandeServiceTest.java` et relance `mvn clean verify`. Le build doit échouer avec :

```
[ERROR] Rule violated: lines covered ratio is 0.xx, but expected minimum is 0.70
```

Remets les tests. C'est exactement ce que Jenkins fera à chaque commit.

---

## TP3 — Tests d'intégration avec Failsafe

### Différence entre test unitaire et test d'intégration

| Critère | Test unitaire | Test d'intégration |
|---------|--------------|-------------------|
| Fichier | `*Test.java` | `*IT.java` |
| Plugin Maven | Surefire | Failsafe |
| Phase déclenchée | `mvn test` | `mvn verify` |
| Ce qu'on teste | Une méthode isolée | Une chaîne de méthodes |
| Vitesse | Millisecondes | Peut être lent |
| En cas d'échec | Le build s'arrête immédiatement | Le build continue jusqu'à la fin de `verify` |

Failsafe est conçu pour ne pas interrompre le build en cas d'échec — il accumule les résultats et les publie tous à la fin. C'est utile quand plusieurs tests d'intégration sont indépendants.

---

### Étape 1 — Ajouter Failsafe dans le pom.xml

```xml
<!-- ── Failsafe : tests d'intégration (*IT.java) ─────────── -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.2.5</version>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

### Étape 2 — Créer CommandeServiceIT.java

Les tests d'intégration ne testent pas une méthode en isolation — ils simulent un **scénario métier complet** : construire un panier, calculer le total, appliquer une remise, catégoriser.

Crée `src/test/java/fr/epsi/service/CommandeServiceIT.java` :

```java
package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.model.Panier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration : chaînes de méthodes bout en bout.
 * Convention *IT.java → lancé par Failsafe (mvn verify).
 */
class CommandeServiceIT {

    private final CommandeService service = new CommandeService();

    @Test
    @DisplayName("Scénario complet : panier mixte → total → remise → catégorie PETITE")
    void pipeline_PanierMixte_CategorisationCorrecte() {
        // GIVEN — un client achète des fournitures de bureau
        Panier panier = new Panier();
        panier.ajouter(new Article("Stylo",  2.0), 10); // 20€
        panier.ajouter(new Article("Cahier", 5.0),  4); // 20€
        // total brut attendu = 40€

        // WHEN — enchaînement des trois opérations métier
        double total       = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 10); // -10% → 36€
        String categorie   = service.categoriserCommande(apresRemise);

        // THEN — cohérence de toute la chaîne
        assertEquals(40.0,    total,       0.001, "Total brut incorrect");
        assertEquals(36.0,    apresRemise, 0.001, "Remise incorrecte");
        assertEquals("PETITE", categorie,          "Catégorie incorrecte");
    }

    @Test
    @DisplayName("Commande premium : ordinateur + accessoires → GRANDE")
    void pipeline_PanierPremium_CategorieGrande() {
        // GIVEN
        Panier panier = new Panier();
        panier.ajouter(new Article("Ordinateur", 800.0), 1);
        panier.ajouter(new Article("Souris",      30.0), 2);

        // WHEN
        double total       = service.calculerTotal(panier);   // 860€
        double apresRemise = service.appliquerRemise(total, 5); // 817€
        String categorie   = service.categoriserCommande(apresRemise);

        // THEN
        assertEquals(860.0,   total,       0.001);
        assertEquals(817.0,   apresRemise, 0.001);
        assertEquals("GRANDE", categorie);
    }

    @Test
    @DisplayName("Remise 100% → total zéro → PETITE")
    void pipeline_RemiseTotale_TotalZeroCategorisePetite() {
        // GIVEN
        Panier panier = new Panier();
        panier.ajouter(new Article("Cadeau", 150.0), 1);

        // WHEN
        double total       = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 100);
        String categorie   = service.categoriserCommande(apresRemise);

        // THEN
        assertEquals(0.0,    apresRemise, 0.001);
        assertEquals("PETITE", categorie);
    }
}
```

---

### Étape 3 — Vérifier la séparation Surefire / Failsafe

```bash
# Lance UNIQUEMENT les tests unitaires (Surefire)
mvn clean test
# → CommandeServiceTest exécuté, CommandeServiceIT ignoré

# Lance TOUT (unitaires + intégration + couverture)
mvn clean verify
# → CommandeServiceTest puis CommandeServiceIT
```

Vérifie que les deux dossiers de rapports sont créés :

```
target/surefire-reports/   ← tests unitaires
target/failsafe-reports/   ← tests d'intégration
```

---

## TP4 — Outils d'analyse qualité

Jusqu'ici, les tests vérifient que le code **fait ce qu'il doit faire**. L'analyse statique vérifie que le code **est bien écrit** : conventions respectées, code mort absent, bugs potentiels détectés.

### Les quatre outils

| Outil | Ce qu'il analyse | Commande | Rapport |
|-------|-----------------|----------|---------|
| **Checkstyle** | Conventions de code (nommage, longueur, style) | `mvn checkstyle:checkstyle` | `target/checkstyle-result.xml` |
| **PMD** | Bugs potentiels, variables inutilisées, catch vides | `mvn pmd:pmd` | `target/pmd.xml` |
| **CPD** | Blocs de code dupliqués (Copy-Paste Detector) | `mvn pmd:cpd` | `target/cpd.xml` |
| **SpotBugs** | Bugs dans le bytecode compilé (NullPointer, concurrence) | `mvn spotbugs:spotbugs` | `target/spotbugsXml.xml` |

> SpotBugs est le successeur officiel de FindBugs, mentionné dans le syllabus. C'est le même outil, renommé et maintenu activement.

---

### Étape 1 — Ajouter les trois plugins dans le pom.xml

```xml
<!-- ── Checkstyle ────────────────────────────────────────── -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <!-- Le fichier de règles est à la racine du projet -->
        <configLocation>checkstyle.xml</configLocation>
        <!-- false = signaler sans bloquer le build -->
        <failOnViolation>false</failOnViolation>
        <outputFile>${project.build.directory}/checkstyle-result.xml</outputFile>
    </configuration>
</plugin>

<!-- ── PMD + CPD ─────────────────────────────────────────── -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.0</version>
    <configuration>
        <failOnViolation>false</failOnViolation>
        <printFailingErrors>true</printFailingErrors>
        <!-- CPD : seuil de tokens dupliqués pour déclencher une alerte -->
        <minimumTokens>50</minimumTokens>
        <outputDirectory>${project.build.directory}</outputDirectory>
    </configuration>
</plugin>

<!-- ── SpotBugs ──────────────────────────────────────────── -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.2.0</version>
    <configuration>
        <failOnError>false</failOnError>
        <xmlOutput>true</xmlOutput>
        <outputDirectory>${project.build.directory}</outputDirectory>
    </configuration>
</plugin>
```

---

### Étape 2 — Créer le fichier checkstyle.xml

Crée le fichier `checkstyle.xml` **à la racine du projet** (même niveau que `pom.xml`) :

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>

    <module name="TreeWalker">

        <!-- Nommage -->
        <module name="TypeName"/>           <!-- Classes en PascalCase -->
        <module name="MethodName"/>         <!-- Méthodes en camelCase -->
        <module name="LocalVariableName"/>  <!-- Variables locales en camelCase -->
        <module name="ConstantName"/>       <!-- Constantes en UPPER_SNAKE_CASE -->

        <!-- Longueur -->
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="50"/>
        </module>

        <!-- Imports -->
        <module name="UnusedImports"/>

        <!-- Bonnes pratiques -->
        <module name="EmptyBlock"/>         <!-- catch vides -->
        <module name="NeedBraces"/>         <!-- {} obligatoires sur if/for -->
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>

    </module>
</module>
```

---

### Étape 3 — Lancer et lire les rapports

```bash
# Lancer tous les outils d'un coup
mvn checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs
```

**Lire le rapport Checkstyle.**
Ouvre `target/checkstyle-result.xml` et repère les balises `<error>` :

```xml
<error line="12" severity="warning"
       message="Missing a Javadoc comment."
       source="...MissingJavadocMethodCheck"/>
```

Chaque `<error>` indique : le fichier, la ligne, la sévérité (`warning` ou `error`), et la règle violée.

**Lire le rapport PMD.**
Ouvre `target/pmd.xml` et repère les balises `<violation>` :

```xml
<violation beginline="5" rule="UnusedLocalVariable"
           ruleset="Best Practices" priority="3">
    Avoid unused local variables such as 'varInutilisee'.
</violation>
```

---

### Étape 4 — Provoquer et identifier des violations

L'objectif est de voir concrètement ce que chaque outil détecte. Ouvre `CommandeService.java` et ajoute **temporairement** cette méthode à la fin de la classe, avant le `}` fermant :

```java
// Méthode volontairement mal écrite — à supprimer après le TP
public void methodeTest() {
    String variableInutile = "je ne sers à rien";
    try {
        int x = 1 / 0;
    } catch (Exception e) {
        // catch vide intentionnel
    }
}
```

```bash
mvn checkstyle:checkstyle pmd:pmd
```

**Ce que tu dois observer :**

- Checkstyle signale : `EmptyBlock` (le catch vide) et possiblement `MissingJavadocMethod`
- PMD signale : `UnusedLocalVariable` (variableInutile) et `EmptyCatchBlock`

Note le numéro de ligne de chaque violation dans ton rapport.

**Supprime la méthode** et relance. Les violations doivent disparaître.

---

### Étape 5 — Lancer la commande complète

C'est la commande exacte que le Jenkinsfile utilisera :

```bash
mvn clean verify checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs
```

À la fin, tu dois avoir ces sept rapports :

```
target/surefire-reports/     → tests unitaires
target/failsafe-reports/     → tests d'intégration
target/site/jacoco/          → couverture JaCoCo
target/checkstyle-result.xml → violations Checkstyle
target/pmd.xml               → violations PMD
target/cpd.xml               → blocs dupliqués
target/spotbugsXml.xml       → bugs bytecode
```

---

## TP5 — Publier les rapports dans Jenkins

### Objectif

Tu as déjà un job Jenkins fonctionnel depuis le TP précédent. Tu vas maintenant le reconfigurer pour qu'il publie les sept rapports à chaque build.

---

### Étape 1 — Installer les plugins manquants

```
Gérer Jenkins → Plugins → Disponibles

Installer si absent :
□ JUnit Plugin              → publication des résultats Surefire/Failsafe
□ JaCoCo Plugin             → graphique de couverture
□ Warnings Next Generation  → Checkstyle, PMD, CPD, SpotBugs
```

Redémarre Jenkins si demandé.

---

### Étape 2 — Reconfigurer la commande de build

Dans ton job existant :

```
Configuration → Build → "Appeler les cibles Maven de haut niveau"

Remplace la commande existante par :
clean verify checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs
```

---

### Étape 3 — Ajouter les actions post-build

Toujours dans la configuration du job, section **"Actions à la suite du build"** :

**Publication des tests JUnit (Surefire + Failsafe)**
```
→ Ajouter → "Publier le rapport des résultats de tests JUnit"
→ Fichiers XML : **/surefire-reports/*.xml, **/failsafe-reports/*.xml
```

**Couverture JaCoCo**
```
→ Ajouter → "Record JaCoCo Coverage Report"
→ Path to exec files    : **/target/jacoco.exec
→ Path to class dirs    : **/target/classes
→ Path to source dirs   : **/src/main/java
→ Minimum line coverage : 70
```

**Checkstyle, PMD, CPD, SpotBugs (Warnings Next Gen)**
```
→ Ajouter → "Record compiler warnings and static analysis results"

Outil 1 : Checkstyle
  → Report file pattern : **/checkstyle-result.xml

Outil 2 : PMD
  → Report file pattern : **/pmd.xml

Outil 3 : CPD
  → Report file pattern : **/cpd.xml

Outil 4 : SpotBugs
  → Report file pattern : **/spotbugsXml.xml
```

Sauvegarder, puis **"Lancer un build maintenant"**.

---

### Étape 4 — Lire le tableau de bord Jenkins

Après le build, le tableau de bord du job doit afficher :

```
Test Result      → 14 tests (11 unitaires + 3 intégration), 0 échecs
Coverage Report  → xx% lines
Checkstyle       → N warnings
PMD              → N warnings
SpotBugs         → N warnings
```

**Créer une tendance.** Lance 2 ou 3 builds supplémentaires (bouton "Lancer un build maintenant"). Les graphiques de tendance apparaissent après au moins deux builds — ils montrent l'évolution du nombre de violations et de la couverture dans le temps.

---

### Étape 5 — Vérifier la détection d'échec

Ouvre `CommandeServiceTest.java` et casse intentionnellement un test :

```java
// Modifie temporairement la valeur attendue
assertEquals(99.0, total, 0.001);  // était 6.0
```

Commit et push, ou relance le build manuellement. Le build doit passer au **rouge**. Corrige et relance — il repasse au **vert**. C'est la boucle de feedback centrale de l'intégration continue.

---

### Tableau de bord des erreurs fréquentes

| Symptôme | Cause probable | Solution |
|----------|---------------|---------|
| Rapport JaCoCo vide | `prepare-agent` absent du pom.xml | Vérifier les `<execution>` JaCoCo |
| 0 violations Checkstyle | `configLocation` incorrect | `checkstyle.xml` doit être à la racine |
| Graphique Checkstyle absent dans Jenkins | Pattern de fichier incorrect | Vérifier `**/checkstyle-result.xml` |
| Tests d'intégration non détectés | Fichier ne finit pas par `IT.java` | Renommer `CommandeServiceIT.java` |
| SpotBugs : `NullPointerException` au lancement | Compilation absente | Lancer `mvn compile` avant `spotbugs:spotbugs` |

---

## TP6 — Pipeline Jenkinsfile

### Pourquoi un Jenkinsfile

Jusqu'ici, la configuration du build est stockée dans Jenkins (dans l'interface). Si Jenkins est réinstallé, la config est perdue. Avec un **Jenkinsfile versionné dans Git**, la CI est traitée comme du code : elle est historisée, revue, et recréée automatiquement.

---

### Étape 1 — Créer le Jenkinsfile

Crée le fichier `Jenkinsfile` **à la racine du projet**, au même niveau que `pom.xml`.

```groovy
pipeline {

    agent any

    tools {
        maven 'Maven3'   // nom exact dans Global Tool Configuration
        jdk   'JDK17'
    }

    // ── Paramètres optionnels ─────────────────────────────────
    parameters {
        choice(
            name:        'ENVIRONMENT',
            choices:     ['dev', 'staging', 'prod'],
            description: 'Environnement cible'
        )
        booleanParam(
            name:         'SKIP_TESTS',
            defaultValue: false,
            description:  'Passer les tests (urgence uniquement)'
        )
    }

    stages {

        // ── Stage 1 ──────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Commit : ${env.GIT_COMMIT}"
                echo "Environnement : ${params.ENVIRONMENT}"
            }
        }

        // ── Stage 2 ──────────────────────────────────────────
        stage('Build') {
            steps {
                // -B = batch mode, logs propres pour Jenkins
                sh 'mvn clean compile -B'
            }
        }

        // ── Stage 3 ──────────────────────────────────────────
        stage('Tests unitaires') {
            when {
                not { expression { return params.SKIP_TESTS } }
            }
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo 'Tests unitaires en échec — consulter les logs'
                }
            }
        }

        // ── Stage 4 ──────────────────────────────────────────
        stage('Tests intégration') {
            when {
                not { expression { return params.SKIP_TESTS } }
            }
            steps {
                sh 'mvn verify -Dsurefire.skip=true -B'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        // ── Stage 5 ──────────────────────────────────────────
        stage('Couverture JaCoCo') {
            steps {
                sh 'mvn jacoco:report -B'
            }
            post {
                always {
                    jacoco(
                        execPattern:        '**/target/jacoco.exec',
                        classPattern:       '**/target/classes',
                        sourcePattern:      '**/src/main/java',
                        minimumLineCoverage: '70'
                    )
                }
            }
        }

        // ── Stage 6 ──────────────────────────────────────────
        stage('Qualité') {
            steps {
                sh '''
                    mvn checkstyle:checkstyle \
                        pmd:pmd \
                        pmd:cpd \
                        spotbugs:spotbugs \
                        -B
                '''
            }
            post {
                always {
                    recordIssues(
                        enabledForFailure: true,
                        tools: [
                            checkStyle(pattern: '**/checkstyle-result.xml'),
                            pmdParser(pattern:  '**/pmd.xml'),
                            cpd(pattern:        '**/cpd.xml'),
                            spotBugs(pattern:   '**/spotbugsXml.xml')
                        ],
                        // Build UNSTABLE si plus de 10 avertissements au total
                        qualityGates: [[
                            threshold: 10,
                            type: 'TOTAL',
                            unstable: true
                        ]]
                    )
                }
            }
        }

        // ── Stage 7 ──────────────────────────────────────────
        stage('Archive') {
            steps {
                archiveArtifacts(
                    artifacts:        '**/target/*.jar',
                    fingerprint:      true,
                    allowEmptyArchive: false
                )
            }
        }

        // ── Stage 8 (conditionnel) ────────────────────────────
        // Apparaît seulement quand ENVIRONMENT = prod
        stage('Validation PROD') {
            when {
                expression { return params.ENVIRONMENT == 'prod' }
            }
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input(
                        message:   'Déployer en PRODUCTION ?',
                        ok:        'Oui, déployer',
                        submitter: 'admin'
                    )
                }
            }
        }

    } // fin stages

    post {
        success {
            echo "Build réussi — ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            emailext(
                subject: "ECHEC : ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body:    "Build en échec. Voir : ${env.BUILD_URL}",
                to:      'equipe-dev@monentreprise.fr'
            )
        }
        fixed {
            emailext(
                subject: "CORRIGE : ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body:    "Build de nouveau stable : ${env.BUILD_URL}",
                to:      'equipe-dev@monentreprise.fr'
            )
        }
    }

} // fin pipeline
```

---

### Étape 2 — Créer le job Pipeline dans Jenkins

```
Tableau de bord → Nouveau item
→ Nom    : tp-boutique-pipeline
→ Type   : Pipeline
→ OK

Section "Pipeline" :
  → Définition : Pipeline script from SCM
  → SCM        : Git
  → URL        : URL de ton dépôt GitHub
  → Branche    : */main
  → Script Path: Jenkinsfile   ← nom exact du fichier

→ Sauvegarder → Lancer un build maintenant
```

---

### Étape 3 — Observer la Stage View

Après le build, la vue "Stage View" affiche chaque stage avec sa durée et son statut. C'est là que tu identifies immédiatement quel stage a échoué.

---

### Étape 4 — Tester le stage conditionnel PROD

```
tp-boutique-pipeline → "Lancer avec paramètres"
→ ENVIRONMENT : prod
→ SKIP_TESTS  : false
```

Le stage **"Validation PROD"** doit apparaître et mettre le pipeline en attente (bandeau orange). Clique sur "Oui, déployer" pour le valider, ou attends 1 heure pour que le timeout l'annule.

---

### Étape 5 — Ajouter un test manquant et observer l'impact sur la couverture

Toujours sans toucher au code métier, ajoute ce test dans `CommandeServiceTest.java` :

```java
@Test
@DisplayName("Remise 100% → total à zéro")
void appliquerRemise_CentPourcent_RetourneZero() {
    double resultat = service.appliquerRemise(200.0, 100);
    assertEquals(0.0, resultat, 0.001);
}
```

Commit, push, ou relance le build. Observe l'évolution du graphique JaCoCo dans Jenkins. Le pourcentage de couverture doit augmenter légèrement.

---

## Étude de cas — Mise en situation professionnelle (MSPR)

### Contexte

Tu intègres l'équipe DevOps de **TechRetail SA**. Le projet `tp-jenkins` existe mais n'a aucune CI. La direction technique exige une chaîne complète avant la prochaine mise en production.

### Ce qui est fourni

Le code métier uniquement : `Article.java`, `Panier.java`, `CommandeService.java`.

### Livrables attendus

**1 — Tests automatisés**
Minimum 7 tests unitaires JUnit 5 dans `CommandeServiceTest.java`, respectant le pattern AAA et la convention de nommage. Tests d'intégration dans `CommandeServiceIT.java` (minimum 2 scénarios bout en bout). Couverture JaCoCo ≥ 70% visible dans Jenkins.

**2 — Analyse qualité**
Les quatre outils (Checkstyle, PMD, CPD, SpotBugs) configurés dans le pom.xml et publiés dans Jenkins. Au moins une violation identifiée, documentée et corrigée dans le rapport.

**3 — Pipeline CI**
Un `Jenkinsfile` versionné à la racine du dépôt avec au minimum 6 stages (Checkout, Build, Tests unitaires, Tests intégration, Couverture, Qualité). Un paramètre fonctionnel. Les notifications email post-build configurées.

**4 — Rapport écrit (1 à 2 pages)**
Capture du tableau de bord Jenkins montrant les graphiques de tendance sur au moins 3 builds. Analyse d'au moins 2 violations qualité détectées (outil, règle, ligne, correction apportée). Extrait de log commenté d'un build en échec puis en succès.

### Grille d'évaluation

| Critère | Points |
|---------|--------|
| Tests unitaires : nombre, nommage, pattern AAA | 5 |
| Tests d'intégration + couverture ≥ 70% | 4 |
| 4 outils qualité configurés et publiés dans Jenkins | 4 |
| Jenkinsfile : ≥ 6 stages + paramètre + notifications | 4 |
| Rapport : captures + analyse violations + logs | 3 |
| **Total** | **20** |

---

## Ressources

- Documentation Jenkins : https://www.jenkins.io/doc/
- JUnit 5 : https://junit.org/junit5/docs/current/user-guide/
- JaCoCo : https://www.jacoco.org/jacoco/trunk/doc/
- Checkstyle (liste complète des règles) : https://checkstyle.sourceforge.io/checks.html
- PMD (liste des règles) : https://pmd.github.io/latest/pmd_rules_java.html
- SpotBugs : https://spotbugs.readthedocs.io/
- Jenkins - Gérez vos projets en intégration continue — Vidéo Bibliothèque ENI
- Kubernetes, chapitre "Usine logicielle" — Bibliothèque ENI

---

*ICDE848 · Intégration continue : serveur, tests et métriques · EPSI · Mastère EISI 1ère année · 2025-2026*
