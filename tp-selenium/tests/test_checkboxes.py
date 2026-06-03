from pages.checkboxes_page import CheckboxesPage


class TestCheckboxes:

    def test_etat_initial_premiere_checkbox_non_cochee(self, driver, base_url):
        page = CheckboxesPage(driver, base_url).open()
        assert not page.est_cochee(0), "Checkbox 1 devrait être décochée par défaut"

    def test_etat_initial_deuxieme_checkbox_cochee(self, driver, base_url):
        page = CheckboxesPage(driver, base_url).open()
        assert page.est_cochee(1), "Checkbox 2 devrait être cochée par défaut"

    def test_cocher_premiere_checkbox(self, driver, base_url):
        page = CheckboxesPage(driver, base_url).open()
        assert not page.est_cochee(0)

        page.cocher(0)

        assert page.est_cochee(0), "Checkbox 1 devrait être cochée après le clic"

    def test_decocher_deuxieme_checkbox(self, driver, base_url):
        page = CheckboxesPage(driver, base_url).open()
        assert page.est_cochee(1)

        page.decocher(1)

        assert not page.est_cochee(1), "Checkbox 2 devrait être décochée après le clic"

    def test_cocher_les_deux_checkboxes(self, driver, base_url):
        page = CheckboxesPage(driver, base_url).open()
        page.cocher(0).cocher(1)

        assert page.est_cochee(0), "Checkbox 1 devrait être cochée"
        assert page.est_cochee(1), "Checkbox 2 devrait être cochée"
