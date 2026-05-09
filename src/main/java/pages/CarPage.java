package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CarPage extends PageBase {

    private static final String HOME_URL = "https://phptravels.net/";
    private static final String CARS_URL = "https://phptravels.net/cars";

    private final By carsTab = By.xpath("//button[@role='tab'][.//span[normalize-space()='Cars']] | //button[.//span[normalize-space()='Cars']]");
    private final By pickupInput = By.xpath("//label[.//span[contains(normalize-space(),'Pick-up Location')]]/following-sibling::div//input[@type='text'][1]");
    private final By returnInput = By.xpath("//label[.//span[contains(normalize-space(),'Return Location')]]/following-sibling::div//input[@type='text'][1]");
    private final By searchCarsButton = By.xpath("//button[@type='submit'][.//span[contains(normalize-space(),'Search Cars')]]");
    private final By firstBookNowButton = By.xpath("(//button[starts-with(@id,'book-btn-') or .//span[normalize-space()='Book Now'] or normalize-space()='Book Now'])[1]");
    private final By resultsContainer = By.xpath("//button[starts-with(@id,'book-btn-')] | //div[contains(@class,'card')]");

    public CarPage(WebDriver driver) {
        super(driver);
    }

    public void openHomePage() {
        navigateTo(HOME_URL);
        waitForPageReady();
    }

    public void openCarsTab() {
        waitClickable(carsTab);
        safeClick(carsTab);
        if (!waitUntilVisible(pickupInput, 8)) {
            navigateTo(CARS_URL);
            waitForPageReady();
            waitVisible(pickupInput);
        }
    }

    public void choosePickupCity(String query, String exactOptionText) {
        selectLocationFromDropdown(pickupInput, query, exactOptionText);
    }

    public void chooseReturnCity(String query, String exactOptionText) {
        selectLocationFromDropdown(returnInput, query, exactOptionText);
    }

    private void selectLocationFromDropdown(By inputLocator, String query, String exactOptionText) {
        WebElement input = waitVisible(inputLocator);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys(query);

        By exactOption = By.xpath("//div[contains(@class,'cursor-pointer')]//div[contains(normalize-space(),\"" + exactOptionText + "\")]");
        By fallbackOption = By.xpath("(//div[contains(@class,'cursor-pointer')]//div[contains(normalize-space(),\"" + query + "\")])[1]");

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(exactOption));
            safeClick(exactOption);
        } catch (TimeoutException e) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(fallbackOption));
            safeClick(fallbackOption);
        }

        wait.until(driver -> !input.getAttribute("value").isBlank());
        shortPause(600);
    }

    public void clickSearchCars() {
        scrollIntoView(searchCarsButton);
        safeClick(searchCarsButton);
    }

    public void waitForResultsPage() {
        wait.until(driver -> driver.getCurrentUrl().contains("/cars/rental/") || isDisplayed(firstBookNowButton) || isDisplayed(resultsContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstBookNowButton));
    }

    public void chooseFirstCar() {
        scrollIntoView(firstBookNowButton);
        safeClick(firstBookNowButton);
    }

    public void waitForBookingRedirect() {
        String oldUrl = currentUrl();
        wait.until(driver -> !driver.getCurrentUrl().equals(oldUrl) || !driver.findElements(firstBookNowButton).isEmpty());
        shortPause(1500);
    }

    public String getCurrentPageUrl() {
        return currentUrl();
    }

    public int getBookButtonsCount() {
        List<WebElement> buttons = driver.findElements(firstBookNowButton);
        return buttons.size();
    }
}
//tests2