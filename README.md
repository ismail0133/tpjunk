# TP Jenkins – Boutique en ligne
## ICDE848 – Intégration Continue : Serveur, Tests & Métriques

---

## 📁 Structure du projet

```
tp-jenkins/
├── pom.xml                          ← Configuration Maven complète
├── checkstyle.xml                   ← Règles de style de code
├── Jenkinsfile                      ← Pipeline CI complète
├── README.md                        ← Ce fichier
└── src/
    ├── main/java/fr/epsi/
    │   ├── model/
    │   │   ├── Article.java         ← Modèle : un produit
    │   │   └── Panier.java          ← Modèle : panier d'achats
    │   └── service/
    │       └── CommandeService.java ← Logique métier
    └── test/java/fr/epsi/service/
        ├── CommandeServiceTest.java  ← Tests unitaires (14 tests)
        └── CommandeServiceIT.java   ← Tests d'intégration (3 tests)
```

J'ai réalisé la configuration complète du TP : tests automatisés, couverture JaCoCo, analyse qualité et pipeline Jenkins.

---

## 🚀 Démarrage rapide

### Prérequis
- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Git (`git --version`)
- Jenkins local sur le port `8080`
- ngrok (`./scripts/install-ngrok.sh`)

### Cloner et tester en local

```bash
# Cloner le projet
git clone https://github.com/VOTRE_USERNAME/tp-jenkins.git
cd tp-jenkins

# Tests unitaires uniquement
mvn clean test

# Tout : tests + intégration + couverture
mvn clean verify

# Tout + analyse qualité complète
mvn clean verify checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs
```

### Résultats attendus

```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
Tests run: 3,  Failures: 0, Errors: 0, Skipped: 0  (intégration)
[INFO] BUILD SUCCESS
```

---

## 📊 Rapports générés

Après `mvn clean verify checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs` :

| Rapport | Chemin | Utilisé par Jenkins |
|---|---|---|
| Tests unitaires | `target/surefire-reports/*.xml` | Plugin JUnit |
| Tests intégration | `target/failsafe-reports/*.xml` | Plugin JUnit |
| Couverture | `target/site/jacoco/index.html` | Plugin JaCoCo |
| Checkstyle | `target/checkstyle-result.xml` | Warnings NG |
| PMD | `target/pmd.xml` | Warnings NG |
| CPD | `target/cpd.xml` | Warnings NG |
| SpotBugs | `target/spotbugsXml.xml` | Warnings NG |

---

## 🔧 Configuration Jenkins requise

### Global Tool Configuration
- JDK : nom `JDK17`, Java 17
- Maven : nom `Maven3`, version 3.9.x

### Plugins à installer
- Git plugin
- Maven Integration
- JaCoCo Plugin
- Warnings Next Generation
- Role-based Authorization Strategy
- ThinBackup
- Email Extension Plugin
- GitHub plugin (si webhook GitHub)

### Liaison GitHub → Jenkins avec ngrok

Le `Jenkinsfile` contient le déclencheur `githubPush()`. Pour que GitHub puisse appeler Jenkins quand Jenkins tourne en local, lance un tunnel ngrok vers le port `8080`.

```bash
# 1. Vérifier ou installer ngrok
./scripts/install-ngrok.sh

# Si ngrok demande un token personnel
NGROK_AUTHTOKEN=ton_token ./scripts/install-ngrok.sh

# 2. Démarrer le tunnel vers Jenkins local
./scripts/start-ngrok-jenkins.sh
```

Le script affiche une URL de webhook sous cette forme :

```text
https://xxxx.ngrok-free.app/github-webhook/
```

Ajoute cette URL dans GitHub :

```text
Repository GitHub > Settings > Webhooks > Add webhook
Payload URL  : https://xxxx.ngrok-free.app/github-webhook/
Content type : application/json
Events       : Just the push event
Active       : coché
```

Si GitHub CLI est installé et connecté, le webhook peut être créé automatiquement :

```bash
./scripts/configure-github-webhook.sh
```

À chaque nouveau tunnel ngrok gratuit, l'URL peut changer. Il faut alors relancer `./scripts/start-ngrok-jenkins.sh` et mettre à jour le webhook GitHub.

### Post-build Actions du job
```
Publier les résultats JUnit  : **/surefire-reports/*.xml
                               **/failsafe-reports/*.xml
JaCoCo coverage report       : **/target/jacoco.exec
Warnings NG – Checkstyle     : **/checkstyle-result.xml
Warnings NG – PMD            : **/pmd.xml
Warnings NG – CPD            : **/cpd.xml
Warnings NG – SpotBugs       : **/spotbugsXml.xml
```

---

## 📚 Ressources

- [Jenkins.io](https://jenkins.io) – Documentation officielle
- [JUnit 5](https://junit.org/junit5/) – Documentation JUnit
- [JaCoCo](https://www.jacoco.org) – Documentation couverture
- [Checkstyle](https://checkstyle.org) – Règles disponibles
- [SpotBugs](https://spotbugs.github.io) – Documentation
