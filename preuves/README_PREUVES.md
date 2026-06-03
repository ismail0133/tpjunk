# Preuves de rendu - TP TESE860 Automatisation des tests

Ce dossier regroupe les éléments à fournir pour prouver le travail réalisé.

## Fichiers à rendre

- `RAPPORT_TESE860_AUTOMATISATION_TESTS.md` : rapport écrit du TP.
- `REPARTITION_GROUPE.md` : répartition du travail pour un groupe de 3 personnes.
- `screenshots/` : captures d'écran à ajouter avant le rendu final.

## Captures à ajouter dans `screenshots/`

1. `01-red-tdd-calculer-tva.png`
   - Test `calculerTVA` écrit avant la méthode.
   - Résultat attendu : erreur de compilation ou test échoué.

2. `02-green-mvn-test.png`
   - Commande :
     ```bash
     mvn test
     ```
   - Résultat attendu : `BUILD SUCCESS`.

3. `03-mvn-verify-jacoco.png`
   - Commande :
     ```bash
     mvn verify
     ```
   - Résultat attendu : tests unitaires, tests d'intégration et JaCoCo en succès.

4. `04-selenium-pytest.png`
   - Commande depuis `tp-selenium/` :
     ```bash
     pytest tests/ -v --junitxml=rapport-selenium.xml --html=rapport-selenium.html --self-contained-html
     ```
   - Résultat attendu : `14 passed`.

5. `05-rapport-selenium-html.png`
   - Capture du rapport HTML `tp-selenium/rapport-selenium.html`.

6. `06-jenkins-pipeline-success.png`
   - Capture Jenkins montrant le pipeline complet avec le stage `Tests UI Selenium`.

7. `07-jenkins-resultats-tests.png`
   - Capture Jenkins montrant les résultats JUnit Java et Selenium.

## Fichiers de code importants

- `src/main/java/fr/epsi/service/CommandeService.java`
- `src/main/java/fr/epsi/repository/StockRepository.java`
- `src/test/java/fr/epsi/service/CommandeServiceTest.java`
- `src/test/java/fr/epsi/service/CommandeServiceMockTest.java`
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- `tp-selenium/`
- `Jenkinsfile`

## Fichiers à ne pas rendre

- `target/`
- `tp-selenium/.venv/`
- `tp-selenium/venv/`
- `tp-selenium/.pytest_cache/`
- `__pycache__/`
