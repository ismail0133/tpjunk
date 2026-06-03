package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.model.Panier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du CommandeService.
 *
 * Convention de nommage :
 *   methode_Scenario_ResultatAttendu()
 *
 * Pattern AAA :
 *   GIVEN  → préparer le contexte
 *   WHEN   → exécuter l'action
 *   THEN   → vérifier le résultat
 *
 * ICDE848 – TP Jenkins
 */
class CommandeServiceTest {

    private CommandeService service;
    private Panier panier;

    /** Exécuté avant chaque test — repart d'un état propre */
    @BeforeEach
    void setUp() {
        service = new CommandeService();
        panier  = new Panier();
    }

    // ─────────────────────────────────────────────────
    // calculerTotal
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("Total correct pour 3 stylos à 2€")
    void calculerTotal_TroisStylos_RetourneSix() {
        // GIVEN
        panier.ajouter(new Article("Stylo", 2.0), 3);

        // WHEN
        double total = service.calculerTotal(panier);

        // THEN
        assertEquals(6.0, total, 0.001);
    }

    @Test
    @DisplayName("Total correct pour plusieurs articles différents")
    void calculerTotal_PlusieursArticles_RetourneSomme() {
        // GIVEN
        panier.ajouter(new Article("Stylo",  2.0), 3);  // 6€
        panier.ajouter(new Article("Cahier", 5.0), 2);  // 10€

        // WHEN
        double total = service.calculerTotal(panier);

        // THEN
        assertEquals(16.0, total, 0.001);
    }

    @Test
    @DisplayName("Panier vide lève une IllegalArgumentException")
    void calculerTotal_PanierVide_LeveException() {
        // GIVEN
        Panier panierVide = new Panier();

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class,
            () -> service.calculerTotal(panierVide));
    }

    @Test
    @DisplayName("Panier null lève une IllegalArgumentException")
    void calculerTotal_PanierNull_LeveException() {
        // GIVEN
        Panier panierNull = null;

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class,
            () -> service.calculerTotal(panierNull));
    }

    // ─────────────────────────────────────────────────
    // appliquerRemise
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("Remise 10% sur 100€ = 90€")
    void appliquerRemise_DixPourcent_RetourneQuatreVingtDix() {
        // GIVEN
        double total = 100.0;
        int pourcentage = 10;

        // WHEN
        double resultat = service.appliquerRemise(total, pourcentage);

        // THEN
        assertEquals(90.0, resultat, 0.001);
    }

    @Test
    @DisplayName("Remise 100% → total à zéro")
    void appliquerRemise_CentPourcent_RetourneZero() {
        // GIVEN
        double total = 200.0;
        int pourcentage = 100;

        // WHEN
        double resultat = service.appliquerRemise(total, pourcentage);

        // THEN
        assertEquals(0.0, resultat, 0.001);
    }

    @Test
    @DisplayName("Remise 0% ne change pas le total")
    void appliquerRemise_ZeroPourcent_RetourneTotalInchange() {
        // GIVEN
        double total = 100.0;
        int pourcentage = 0;

        // WHEN
        double resultat = service.appliquerRemise(total, pourcentage);

        // THEN
        assertEquals(100.0, resultat, 0.001);
    }

    @Test
    @DisplayName("Remise négative lève une IllegalArgumentException")
    void appliquerRemise_RemiseNegative_LeveException() {
        // GIVEN
        double total = 100.0;
        int pourcentage = -5;

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class,
            () -> service.appliquerRemise(total, pourcentage));
    }

    @Test
    @DisplayName("Remise > 100 lève une IllegalArgumentException")
    void appliquerRemise_RemiseSupCent_LeveException() {
        // GIVEN
        double total = 100.0;
        int pourcentage = 150;

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class,
            () -> service.appliquerRemise(total, pourcentage));
    }

    // ─────────────────────────────────────────────────
    // calculerTVA — TDD
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("TVA 20% sur 100€ = 20€")
    void calculerTVA_CentEuros_RetourneVingt() {
        double tva = service.calculerTVA(100.0);
        assertEquals(20.0, tva, 0.01);
    }

    @Test
    @DisplayName("TVA sur 33.33€ arrondie à 2 décimales")
    void calculerTVA_MontantDecimal_RetourneArrondi() {
        double tva = service.calculerTVA(33.33);
        assertEquals(6.67, tva, 0.01);
    }

    @Test
    @DisplayName("TVA sur 0€ = 0€")
    void calculerTVA_Zero_RetourneZero() {
        double tva = service.calculerTVA(0.0);
        assertEquals(0.0, tva, 0.01);
    }

    @Test
    @DisplayName("Montant négatif lève une IllegalArgumentException")
    void calculerTVA_MontantNegatif_LeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculerTVA(-10.0));
    }

    @Test
    @DisplayName("TVA sur montant très petit (0.01€)")
    void calculerTVA_UnCentime_RetourneZero() {
        double tva = service.calculerTVA(0.01);
        assertEquals(0.0, tva, 0.001);
    }

    // ─────────────────────────────────────────────────
    // categoriserCommande
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("30€ → catégorie PETITE")
    void categoriser_TrenteEuros_RetournePetite() {
        // GIVEN
        double total = 30.0;

        // WHEN
        String categorie = service.categoriserCommande(total);

        // THEN
        assertEquals("PETITE", categorie);
    }

    @Test
    @DisplayName("150€ → catégorie MOYENNE")
    void categoriser_CentCinquanteEuros_RetourneMoyenne() {
        // GIVEN
        double total = 150.0;

        // WHEN
        String categorie = service.categoriserCommande(total);

        // THEN
        assertEquals("MOYENNE", categorie);
    }

    @Test
    @DisplayName("500€ → catégorie GRANDE")
    void categoriser_CinqCentsEuros_RetourneGrande() {
        // GIVEN
        double total = 500.0;

        // WHEN
        String categorie = service.categoriserCommande(total);

        // THEN
        assertEquals("GRANDE", categorie);
    }

    @Test
    @DisplayName("Frontière : exactement 50€ → MOYENNE")
    void categoriser_CinquanteEuros_RetourneMoyenne() {
        // GIVEN
        double total = 50.0;

        // WHEN
        String categorie = service.categoriserCommande(total);

        // THEN
        assertEquals("MOYENNE", categorie);
    }

    @Test
    @DisplayName("Frontière : exactement 200€ → GRANDE")
    void categoriser_DeuxCentsEuros_RetourneGrande() {
        // GIVEN
        double total = 200.0;

        // WHEN
        String categorie = service.categoriserCommande(total);

        // THEN
        assertEquals("GRANDE", categorie);
    }
}
