package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class PageBase {

    public WebDriver driver;
    public WebDriverWait wait;

    // Constructor
    public PageBase(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ============ Click ============
    public void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // ============ Send Keys ============
    public void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    // ============ Get Text ============
    public String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    // ============ Explicit Wait ============
    public WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ============ Fluent Wait ============
    public WebElement fluentWait(By locator) {
        FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);

        return fluentWait.until(d -> d.findElement(locator));
    }

    // ============ Select Dropdown ============
    public void selectByVisibleText(By locator, String text) {
        Select select = new Select(waitForElement(locator));
        select.selectByVisibleText(text);
    }

    public void selectByValue(By locator, String value) {
        Select select = new Select(waitForElement(locator));
        select.selectByValue(value);
    }

    public void selectByIndex(By locator, int index) {
        Select select = new Select(waitForElement(locator));
        select.selectByIndex(index);
    }

    // ============ List of WebElements ============
    public List<WebElement> findElements(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator);
    }

    // ============ Actions Class ============
    public void hoverOverElement(By locator) {
        WebElement element = waitForElement(locator);
        Actions actions = new Actions(driver);
        actions.moveToElement(element).perform();
    }

    public void doubleClick(By locator) {
        WebElement element = waitForElement(locator);
        Actions actions = new Actions(driver);
        actions.doubleClick(element).perform();
    }

    public void rightClick(By locator) {
        WebElement element = waitForElement(locator);
        Actions actions = new Actions(driver);
        actions.contextClick(element).perform();
    }

    // ============ Scroll ============
    public void scrollDown(int pixels) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("scrollBy(0," + pixels + ")");
    }

    public void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    // ============ Alerts ============
    public void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    public void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.dismiss();
    }

    public String getAlertText() {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        return alert.getText();
    }

    public void sendKeysToAlert(String text) {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.sendKeys(text);
        alert.accept();
    }

    // ============ Checkbox / Radio Button ============
    public void selectCheckbox(By locator) {
        WebElement checkbox = waitForElement(locator);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    public void deselectCheckbox(By locator) {
        WebElement checkbox = waitForElement(locator);
        if (checkbox.isSelected()) {
            checkbox.click();
        }
    }

    public void selectRadioButton(By locator) {
        WebElement radio = waitForClickable(locator);
        radio.click();
    }

    // ============ Element State ============
    public boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        return driver.findElement(locator).isEnabled();
    }

    public boolean isSelected(By locator) {
        return driver.findElement(locator).isSelected();
    }

    // ============ Navigation ============
    public void navigateTo(String url) {
        driver.navigate().to(url);
    }

    public void navigateBack() {
        driver.navigate().back();
    }

    public void navigateForward() {
        driver.navigate().forward();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}

