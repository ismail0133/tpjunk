import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options


BASE_URL = "https://the-internet.herokuapp.com"


@pytest.fixture
def driver():
    """Crée un driver Chrome avant chaque test et le ferme après."""
    options = Options()
    options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

    driver = webdriver.Chrome(options=options)
    driver.implicitly_wait(5)

    yield driver

    driver.quit()


@pytest.fixture
def base_url():
    return BASE_URL
