package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends PageBase {

    // ============ Locators (CSS only) ============
    private final By emailField    = By.cssSelector("input[type='email']");
    private final By passwordField = By.cssSelector("input[type='password']");
    private final By loginButton   = By.cssSelector("button[type='submit']");

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
     * Explicit Wait to confirm successful redirect after login.
     */
    public void login(String email, String password) {
        openLoginPage();

        // Wait for login form to be ready (site can be slow / occasionally hangs)
        WebDriverWait formWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            formWait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        } catch (Exception firstTry) {
            // One retry: refresh and wait again
            driver.navigate().refresh();
            formWait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        }

        enterEmail(email);
        enterPassword(password);
        clickLogin();
        // Explicit Wait – wait until URL changes away from /login
        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        loginWait.until(d -> !d.getCurrentUrl().contains("/login"));
    }

    // ============ Verification ============
    public boolean isLoggedIn() {
        return !driver.getCurrentUrl().contains("/login");
    }
}
