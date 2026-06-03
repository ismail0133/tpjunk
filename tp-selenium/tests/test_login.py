import pytest
from pages.login_page import LoginPage


class TestLogin:
    """Tests de la page de login."""

    def test_connexion_valide_redirige_vers_securise(self, driver, base_url):
        page = LoginPage(driver, base_url)
        page.open()

        page.se_connecter("tomsmith", "SuperSecretPassword!")
        page.attendre_redirection_secure()

        assert "/secure" in page.get_url_courante(), (
            "Redirection attendue vers /secure, "
            f"URL obtenue : {page.get_url_courante()}"
        )

    def test_connexion_valide_affiche_message_succes(self, driver, base_url):
        page = LoginPage(driver, base_url)
        page.open()

        page.se_connecter("tomsmith", "SuperSecretPassword!")

        message = page.get_message_flash()
        assert "You logged into a secure area" in message, (
            f"Message attendu non trouvé. Message reçu : {message}"
        )

    def test_mot_de_passe_incorrect_affiche_erreur(self, driver, base_url):
        page = LoginPage(driver, base_url)
        page.open()

        page.se_connecter("tomsmith", "mauvaismdp")

        message = page.get_message_flash()
        assert "Your password is invalid" in message, (
            f"Message d'erreur attendu non trouvé. Message reçu : {message}"
        )

    def test_username_incorrect_affiche_erreur(self, driver, base_url):
        page = LoginPage(driver, base_url)
        page.open()

        page.se_connecter("utilisateur_inconnu", "SuperSecretPassword!")

        message = page.get_message_flash()
        assert "Your username is invalid" in message, (
            f"Message d'erreur attendu non trouvé. Message reçu : {message}"
        )

    def test_champs_vides_restent_sur_page_login(self, driver, base_url):
        page = LoginPage(driver, base_url)
        page.open()

        page.cliquer_connexion()

        assert "/login" in page.get_url_courante()


class TestLoginParametrize:
    """Même test lancé avec plusieurs combinaisons identifiants/attendu."""

    @pytest.mark.parametrize(
        "username, password, message_attendu",
        [
            ("tomsmith", "SuperSecretPassword!", "You logged into a secure area"),
            ("tomsmith", "mauvaismdp", "Your password is invalid"),
            ("inconnu", "SuperSecretPassword!", "Your username is invalid"),
            ("", "", "Your username is invalid"),
        ],
    )
    def test_login_scenarios(
        self, driver, base_url, username, password, message_attendu
    ):
        page = LoginPage(driver, base_url)
        page.open()
        page.se_connecter(username, password)

        message = page.get_message_flash()
        assert message_attendu in message, (
            f"[{username}/{password}] Attendu : '{message_attendu}', "
            f"Obtenu : '{message}'"
        )
