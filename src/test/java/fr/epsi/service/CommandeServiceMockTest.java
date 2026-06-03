package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires avec Mockito.
 * Le mock remplace une vraie dépendance de stock.
 */
@ExtendWith(MockitoExtension.class)
class CommandeServiceMockTest {

    @Mock
    private StockRepository stockRepository;

    private CommandeService service;
    private Article article;

    @BeforeEach
    void setUp() {
        service = new CommandeService(stockRepository);
        article = new Article("Stylo", 2.0);
    }

    @Test
    @DisplayName("Stock suffisant → commande réalisable")
    void commandeRealisable_StockSuffisant_RetourneTrue() {
        // GIVEN
        when(stockRepository.getStock(article)).thenReturn(10);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "La commande devrait être réalisable");
        verify(stockRepository, times(1)).getStock(article);
    }

    @Test
    @DisplayName("Stock insuffisant → commande non réalisable")
    void commandeRealisable_StockInsuffisant_RetourneFalse() {
        // GIVEN
        when(stockRepository.getStock(article)).thenReturn(2);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertFalse(resultat, "La commande ne devrait pas être réalisable");
    }

    @Test
    @DisplayName("Stock exactement égal à la demande → réalisable")
    void commandeRealisable_StockEgalDemande_RetourneTrue() {
        // GIVEN
        when(stockRepository.getStock(article)).thenReturn(5);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "Commande égale au stock devrait être réalisable");
    }

    @Test
    @DisplayName("Stock à zéro → commande non réalisable")
    void commandeRealisable_StockZero_RetourneFalse() {
        // GIVEN
        when(stockRepository.getStock(article)).thenReturn(0);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 1);

        // THEN
        assertFalse(resultat, "Stock à zéro : commande non réalisable");
    }

    @Test
    @DisplayName("Sans StockRepository → IllegalStateException")
    void commandeRealisable_SansRepository_LeveException() {
        // GIVEN
        CommandeService serviceNonConfigure = new CommandeService();

        // WHEN + THEN
        assertThrows(IllegalStateException.class,
            () -> serviceNonConfigure.commandeRealisable(article, 1));
    }
}
