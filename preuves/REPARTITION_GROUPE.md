# Répartition du travail - Groupe de 3

## Membre 1 - Tests Selenium et rapports UI

Responsabilités :

- créer la structure `tp-selenium/` ;
- configurer `conftest.py` avec Chrome headless ;
- créer les Page Objects :
  - `LoginPage` ;
  - `CheckboxesPage` ;
- écrire les tests Selenium :
  - login valide ;
  - login invalide ;
  - champs vides ;
  - tests paramétrés ;
  - checkboxes ;
- générer les rapports :
  - `rapport-selenium.xml` ;
  - `rapport-selenium.html`.

Preuves à fournir :

- capture du terminal avec `14 passed` ;
- capture du rapport HTML Selenium.

## Membre 2 - TDD Java et Mockito

Responsabilités :

- ajouter les tests TDD de `calculerTVA` ;
- faire la phase RED avant d'ajouter la méthode ;
- ajouter la méthode `calculerTVA` dans `CommandeService` ;
- créer l'interface `StockRepository` ;
- ajouter la méthode `commandeRealisable` ;
- écrire les tests Mockito dans `CommandeServiceMockTest` ;
- vérifier le mock avec `verify()`.

Preuves à fournir :

- capture RED de `calculerTVA` ;
- capture GREEN avec `mvn test` ;
- capture montrant les tests Mockito en succès.

## Membre 3 - Jenkins, qualité et rapport final

Responsabilités :

- modifier le `Jenkinsfile` ;
- ajouter le stage `Tests UI Selenium` ;
- vérifier la publication du rapport JUnit Selenium ;
- vérifier l'archivage du rapport HTML Selenium ;
- lancer :
  - `mvn test` ;
  - `mvn verify` ;
  - `mvn checkstyle:checkstyle` ;
- préparer le rapport écrit final ;
- centraliser les captures dans `preuves/screenshots/`.

Preuves à fournir :

- capture Jenkins du pipeline complet ;
- capture Jenkins des résultats de tests ;
- capture JaCoCo ou `mvn verify` ;
- rapport final.

## Synthèse collective

Le rendu final doit contenir :

- le code source complet ;
- le dossier `tp-selenium/` sans environnement virtuel ;
- le `Jenkinsfile` mis à jour ;
- le dossier `preuves/` ;
- les captures dans `preuves/screenshots/` ;
- le rapport écrit ;
- la répartition du groupe.
