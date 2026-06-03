# Rapport - TP TESE860 Automatisation des tests

## 1. Objectif du TP

L'objectif du TP était d'étendre le projet Maven existant avec une stratégie de tests plus complète :

- ajout d'une suite de tests UI automatisés avec Selenium et Pytest ;
- développement d'une nouvelle fonctionnalité en TDD ;
- ajout de tests unitaires avec Mockito ;
- intégration des tests UI dans le pipeline Jenkins.

Le travail a été réalisé à partir du sujet `TP_TESE860_Automatisation_Tests.md`, sans ajouter de fonctionnalité hors consigne.

## 2. Suite de tests Selenium

Une suite de tests Selenium a été créée dans le dossier `tp-selenium/`.

La structure respecte le pattern Page Object Model :

- `pages/login_page.py` centralise les actions et locators de la page de connexion ;
- `pages/checkboxes_page.py` centralise les actions et locators de la page des cases à cocher ;
- `tests/test_login.py` contient les scénarios de connexion ;
- `tests/test_checkboxes.py` contient les scénarios sur les checkboxes ;
- `conftest.py` fournit le driver Chrome en mode headless.

Les scénarios Selenium couvrent deux pages différentes de `https://the-internet.herokuapp.com` :

- la page `/login` ;
- la page `/checkboxes`.

Les cas de test choisis vérifient des parcours représentatifs :

- connexion valide avec redirection vers `/secure` ;
- affichage du message de succès ;
- mot de passe incorrect ;
- nom d'utilisateur incorrect ;
- formulaire vide ;
- état initial des checkboxes ;
- interaction de coche et décoche.

Un test paramétré Pytest permet aussi de rejouer plusieurs combinaisons username/password avec un seul test.

Résultat obtenu :

```text
14 tests Selenium passés
```

Les rapports générés sont :

- `tp-selenium/rapport-selenium.xml` pour Jenkins ;
- `tp-selenium/rapport-selenium.html` pour consultation humaine.

## 3. Développement en TDD

Une nouvelle méthode `calculerTVA` a été ajoutée dans `CommandeService`.

Spécification métier :

- calculer la TVA à 20% ;
- retourner un montant arrondi à 2 décimales ;
- lever une `IllegalArgumentException` si le montant est négatif.

Les tests ont été ajoutés dans `CommandeServiceTest` :

- TVA sur 100 euros ;
- TVA sur un montant décimal ;
- TVA sur zéro ;
- montant négatif ;
- valeur frontière sur 0.01 euro.

Cycle TDD attendu :

1. RED : les tests sont écrits avant l'existence de `calculerTVA`.
2. GREEN : la méthode est ajoutée pour faire passer les tests.
3. REFACTOR : le code est relu et documenté avec une Javadoc.

Résultat obtenu :

```text
mvn test : BUILD SUCCESS
```

## 4. Tests avec Mockito

Une interface `StockRepository` a été ajoutée pour représenter la dépendance de stock.

La méthode `commandeRealisable` a été ajoutée dans `CommandeService`. Elle vérifie si le stock disponible est suffisant pour une quantité demandée.

Les tests Mockito sont regroupés dans `CommandeServiceMockTest`.

Scénarios testés :

- stock suffisant ;
- stock insuffisant ;
- stock égal à la demande ;
- stock à zéro ;
- service sans `StockRepository`.

Le mock est vérifié avec :

```java
verify(stockRepository, times(1)).getStock(article);
```

Le fichier `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` configure Mockito avec `mock-maker-subclass`. Cette configuration évite un problème d'agent Java avec le JDK local tout en permettant de mocker l'interface demandée par le TP.

## 5. Intégration Jenkins

Le `Jenkinsfile` contient un nouveau stage `Tests UI Selenium`, placé après le stage `Qualité` et avant le stage `Archive`.

Ce stage :

- crée un environnement virtuel Python ;
- installe les dépendances depuis `tp-selenium/requirements.txt` ;
- lance les tests Selenium en headless ;
- génère un rapport JUnit XML ;
- génère un rapport HTML ;
- publie le rapport JUnit dans Jenkins ;
- archive le rapport HTML.

Commande Jenkins utilisée dans le stage :

```bash
pytest tests/ \
    -v \
    --junitxml=rapport-selenium.xml \
    --html=rapport-selenium.html \
    --self-contained-html
```

## 6. Validation

Commandes exécutées avec succès :

```bash
mvn test -B
mvn verify -B
mvn checkstyle:checkstyle -B
```

Résultats :

- tests unitaires Java : succès ;
- tests Mockito : succès ;
- tests d'intégration : succès ;
- JaCoCo : seuil de couverture validé ;
- Checkstyle : rapport généré ;
- tests Selenium : 14 tests passés.

## 7. Points de vigilance

Les dossiers générés ne doivent pas être rendus :

- `target/` ;
- `tp-selenium/.venv/` ;
- `tp-selenium/venv/` ;
- caches Pytest et Python.

Les captures Jenkins doivent être ajoutées après exécution du pipeline sur Jenkins, car elles dépendent de l'environnement Jenkins local ou distant.
