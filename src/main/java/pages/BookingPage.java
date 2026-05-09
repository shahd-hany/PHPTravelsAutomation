package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BookingPage extends PageBase {

    private final By confirmButton = By.cssSelector(
        "button[type='submit'], button[class*='confirm'], button[class*='book']");

    private final By confirmationMessage = By.cssSelector(
        "div.alert-success, div[class*='success'], div[class*='thank'], div[class*='confirmed']");

    public BookingPage(WebDriver driver) {
        super(driver);
    }

    public void waitForBookingPageToLoad() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        ew.until(d -> {
            String url = d.getCurrentUrl();
            boolean isBookingUrl = url.contains("/checkout") || url.contains("/booking") ||
                                   url.contains("/order") || url.contains("/payment");
            if (!isBookingUrl) return false;
            try {
                List<WebElement> inputs = d.findElements(By.cssSelector("input, form, .card"));
                return !inputs.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
        
        try {
            ew.until(ExpectedConditions.presenceOfElementLocated(confirmButton));
        } catch (Exception ignored) {}
        
    }

    public void clickConfirmBooking() {
        try {
            WebElement termsCheckbox = driver.findElement(By.cssSelector("#terms_accepted"));
            if (!termsCheckbox.isSelected()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", termsCheckbox);
                
                new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementSelectionStateToBe(termsCheckbox, true));
            }
        } catch (Exception e) {
        }

        try {
            WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement btn = ew.until(ExpectedConditions.elementToBeClickable(confirmButton));
            
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
                
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {
            List<WebElement> btns = driver.findElements(
                By.cssSelector("button[type='submit'], button.btn"));
            for (WebElement btn : btns) {
                try {
                    String txt = btn.getText().toLowerCase();
                    if (txt.contains("confirm") || txt.contains("book") ||
                        txt.contains("pay") || txt.contains("complete")) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                        return;
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    public boolean isConfirmationDisplayed() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement msg = ew.until(
                ExpectedConditions.visibilityOfElementLocated(confirmationMessage));
            return msg.isDisplayed();
        } catch (Exception e) {
            String url = driver.getCurrentUrl();
            if (url.contains("/confirmation") || url.contains("/success") ||
                url.contains("/thank")) {
                return true;
            }
            String title = driver.getTitle().toLowerCase();
            return title.contains("confirm") || title.contains("success") ||
                   title.contains("booking");
        }
    }
}
