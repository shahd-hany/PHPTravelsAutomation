package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends PageBase {

    // ============ Locators (CSS only – no XPath) ============

    private final By emailField    = By.cssSelector("input[type='email'], input[name='email']");
    private final By passwordField = By.cssSelector("input[type='password'], input[name='password']");
    private final By loginButton   = By.cssSelector("button[type='submit']");

    // Element that appears only after successful login (nav user menu)
    private final By userMenuIndicator = By.cssSelector(
            ".navbar-nav .dropdown, .user-dropdown, nav .avatar, .nav-user");

    // ============ Constructor ============
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ============ Navigation ============
    public void openLoginPage() {
        navigateTo("https://phptravels.net/login");
    }

    // ============ Actions ============
    public void enterEmail(String email) {
        sendKeys(emailField, email);
    }

    public void enterPassword(String password) {
        sendKeys(passwordField, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    /**
     * Full login flow: open page → enter credentials → submit.
     * Uses Explicit Wait to confirm successful redirect after login.
     */
    public void login(String email, String password) {
        openLoginPage();
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        // Explicit Wait – wait until URL changes away from /login
        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        loginWait.until(d -> !d.getCurrentUrl().contains("/login"));
    }

    // ============ Verification ============
    public boolean isLoggedIn() {
        try {
            WebElement indicator = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(userMenuIndicator));
            return indicator.isDisplayed();
        } catch (Exception e) {
            // Fallback: check URL is not the login page
            return !driver.getCurrentUrl().contains("/login");
        }
    }
}
