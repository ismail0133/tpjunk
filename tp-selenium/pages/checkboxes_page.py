from selenium.webdriver.common.by import By


class CheckboxesPage:
    """Encapsule les interactions de la page checkboxes."""

    URL = "/checkboxes"
    CHECKBOXES = (By.CSS_SELECTOR, "input[type='checkbox']")

    def __init__(self, driver, base_url):
        self.driver = driver
        self.base_url = base_url

    def open(self):
        self.driver.get(self.base_url + self.URL)
        return self

    def get_checkboxes(self):
        """Retourne la liste des deux éléments checkbox."""
        return self.driver.find_elements(*self.CHECKBOXES)

    def est_cochee(self, index):
        """Retourne True si la checkbox à l'index donné est cochée."""
        return self.get_checkboxes()[index].is_selected()

    def cocher(self, index):
        """Coche la checkbox si elle ne l'est pas déjà."""
        checkbox = self.get_checkboxes()[index]
        if not checkbox.is_selected():
            checkbox.click()
        return self

    def decocher(self, index):
        """Décoche la checkbox si elle est cochée."""
        checkbox = self.get_checkboxes()[index]
        if checkbox.is_selected():
            checkbox.click()
        return self
