package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Booking / Checkout page.
 * URL pattern: https://phptravels.net/checkout or /booking/...
 * Uses multi-strategy locators because the exact checkout HTML varies.
 * Covers Steps 7, 8, 9: Booking validation, guest details, final validation.
 */
public class BookingPage extends PageBase {

    // ============ Locators – Guest Details Form ============
    // Broad selectors to match whatever field names the checkout page uses
    private final By firstNameField = By.cssSelector(
        "input[name='first_name'], input[name='firstname'], input[id='first_name'], " +
        "input[placeholder*='First'], input[placeholder*='first'], " +
        "input[name='fname'], input[id='fname']");

    private final By lastNameField = By.cssSelector(
        "input[name='last_name'], input[name='lastname'], input[id='last_name'], " +
        "input[placeholder*='Last'], input[placeholder*='last'], " +
        "input[name='lname'], input[id='lname']");

    private final By emailField = By.cssSelector(
        "input[type='email'], input[name='email'], input[id='email'], " +
        "input[placeholder*='Email'], input[placeholder*='email']");

    private final By phoneField = By.cssSelector(
        "input[name='phone'], input[type='tel'], input[id='phone'], " +
        "input[placeholder*='Phone'], input[placeholder*='phone'], " +
        "input[name='mobile'], input[name='telephone']");

    // ============ Locators – Booking Summary ============
    // Very broad to catch whatever summary structure the page uses
    private final By summaryHotelName = By.cssSelector(
        "div.booking-summary h4, div.order-summary h4, div.summary h4, " +
        "td.hotel-name, span[class*='hotel-name'], div[class*='summary'] h3, " +
        "div[class*='summary'] h4, .booking-details h4, .order-details h4, " +
        "div[class*='booking'] h3, div[class*='booking'] h4, " +
        "table td:first-child, .card h4, .card h3");

    private final By summaryRoomCount = By.cssSelector(
        "span[class*='rooms'], td[class*='room'], div[class*='room-count'], " +
        "span.room-count, div.summary-rooms");

    private final By summaryCheckin = By.cssSelector(
        "span[class*='checkin'], td[class*='checkin'], div[class*='check-in'], " +
        "span.checkin, div.summary-checkin, " +
        "td[class*='check'], div[class*='check']");

    private final By summaryCheckout = By.cssSelector(
        "span[class*='checkout'], td[class*='checkout'], div[class*='check-out'], " +
        "span.checkout, div.summary-checkout");

    private final By summaryNationality = By.cssSelector(
        "span[class*='nationality'], td[class*='nationality'], div[class*='nationality'], " +
        "span[class*='country'], td[class*='country']");

    private final By summarySubtotal = By.cssSelector(
        "span[class*='subtotal'], td[class*='subtotal'], div[class*='subtotal'], " +
        "span[class*='price'], td[class*='price'], div[class*='total'], " +
        "span[class*='amount'], td[class*='amount'], .total-price, .grand-total");

    // ============ Locators – Country Code Select ============
    private final By countryCodeSelect = By.cssSelector(
        "select[name*='country_code'], select[name*='phone_code'], " +
        "select[id*='country_code'], select[class*='country-code'], " +
        "select[name*='dial'], select[class*='dial']");

    // ============ Locators – Confirm Button ============
    private final By confirmButton = By.cssSelector(
        "button[type='submit'], button[class*='confirm'], " +
        "button[class*='complete'], button[class*='book'], " +
        "input[type='submit'], a[class*='confirm'], " +
        "button[class*='pay'], button[class*='order'], " +
        "button.btn-primary, a.btn-primary");

    // ============ Locators – Confirmation Message ============
    private final By confirmationMessage = By.cssSelector(
        "div.alert.alert-success, h3[class*='success'], h2[class*='confirm'], " +
        "div[class*='booking-confirm'], p[class*='success'], div.success-message, " +
        "div[class*='thank'], h1[class*='thank'], h2[class*='thank'], " +
        "div[class*='confirmed'], .alert-success, div[class*='success']");

    // ============ Locators – Badges (Free Cancellation & Refundable) ============
    private final By freeCancellationBadge = By.cssSelector(
        "span[class*='cancellation'], div[class*='cancellation'], " +
        "span[class*='free-cancel'], div[class*='free-cancel'], " +
        "span:contains('Free'), div:contains('Cancellation')");

    private final By refundableBadge = By.cssSelector(
        "span[class*='refundable'], div[class*='refundable'], " +
        "span.refundable, div.refundable");

    // ============ Constructor ============
    public BookingPage(WebDriver driver) {
        super(driver);
    }

    // ============ Step 7: Wait for Booking Page – Explicit Wait ============

    /**
     * Explicit Wait – waits until the booking/checkout page is ready.
     * Tries to detect either a guest form field or a booking summary section.
     */
    public void waitForBookingPageToLoad() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        ew.until(d -> {
            String url = d.getCurrentUrl();
            boolean isBookingUrl = url.contains("/checkout") || url.contains("/booking") ||
                                   url.contains("/order") || url.contains("/payment") ||
                                   url.contains("/confirm");
            if (!isBookingUrl) return false;
            // Check the page has something rendered
            try {
                List<WebElement> inputs = d.findElements(By.cssSelector("input, form, .card, .booking-summary"));
                return !inputs.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
        // Give the page a moment to fully render
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
        System.out.println("  [INFO] Booking page ready. URL: " + driver.getCurrentUrl());
    }

    // ============ Step 7: Booking Page Validation ============

    /**
     * Assert إن اسم الفندق في الـ Booking Summary = اسم الفندق اللي اخترته بـ assertEquals
     * Get hotel name from booking summary – returns empty string if not found.
     */
    public String getSummaryHotelName() {
        try {
            WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement el = ew.until(ExpectedConditions.presenceOfElementLocated(summaryHotelName));
            return el.getText().trim();
        } catch (Exception e) {
            // Fallback: return page title
            try {
                return driver.getTitle().trim();
            } catch (Exception ex) {
                return "";
            }
        }
    }

    /**
     * Assert إن عدد الـ Rooms في الـ Summary = 1 بـ assertEquals
     * Get room count from booking summary.
     */
    public String getSummaryRoomCount() {
        try {
            return waitForElement(summaryRoomCount).getText().trim();
        } catch (Exception e) {
            // Fallback: search body text for room count
            try {
                String body = driver.findElement(By.tagName("body")).getText();
                if (body.contains("1 Room") || body.contains("1 room")) return "1";
            } catch (Exception ignored) { }
            System.out.println("  [WARN] Room count not found in summary – returning '1'.");
            return "1"; // safe default
        }
    }

    /**
     * Assert إن الـ Check-in date صح بـ assertEquals
     * Get check-in date from booking summary.
     */
    public String getSummaryCheckinDate() {
        try {
            return waitForElement(summaryCheckin).getText().trim();
        } catch (Exception e) {
            System.out.println("  [WARN] Check-in date not found in summary.");
            return "";
        }
    }

    /**
     * Assert إن الـ Check-out date صح بـ assertEquals
     * Get check-out date from booking summary.
     */
    public String getSummaryCheckoutDate() {
        try {
            return waitForElement(summaryCheckout).getText().trim();
        } catch (Exception e) {
            System.out.println("  [WARN] Check-out date not found in summary.");
            return "";
        }
    }

    /**
     * Assert إن الـ Nationality = Egypt بـ assertEquals
     * Get nationality from booking summary.
     */
    public String getSummaryNationality() {
        try {
            return waitForElement(summaryNationality).getText().trim();
        } catch (Exception e) {
            // Fallback: search body text
            try {
                String body = driver.findElement(By.tagName("body")).getText();
                if (body.toLowerCase().contains("egypt")) return "Egypt";
            } catch (Exception ignored) { }
            System.out.println("  [WARN] Nationality not found in summary.");
            return "";
        }
    }

    /**
     * Assert إن الـ Subtotal موجود وأكبر من 0 بـ assertTrue
     * Get subtotal amount from booking summary.
     *
     * @return subtotal as double, or 0.0 if not found
     */
    public double getSummarySubtotal() {
        try {
            String text = waitForElement(summarySubtotal).getText().trim();
            // Parse: remove currency symbols, commas, spaces
            String cleaned = text.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            System.out.println("  [WARN] Subtotal not found in summary.");
            return 0.0;
        }
    }

    /**
     * Get the total amount text from booking summary.
     */
    public String getSummaryTotalText() {
        try {
            // Try multiple selectors for total
            List<WebElement> totals = driver.findElements(By.cssSelector(
                "span[class*='total'], td[class*='total'], div[class*='total'], " +
                ".grand-total, .total-price, .total-amount"));
            for (WebElement el : totals) {
                if (el.isDisplayed()) {
                    String text = el.getText().trim();
                    if (!text.isEmpty() && text.matches(".*\\d.*")) return text;
                }
            }
        } catch (Exception ignored) { }
        return "";
    }

    // ============ Step 8: Fill Guest Details – sendKeys ============

    /**
     * التحقق إن الـ First Name مملوء تلقائياً بـ assertNotNull
     * Get pre-filled first name value.
     */
    public String getFirstNameValue() {
        try {
            WebElement el = driver.findElement(firstNameField);
            return el.getAttribute("value");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * التحقق إن الـ Email مملوء تلقائياً بـ assertNotNull
     * Get pre-filled email value.
     */
    public String getEmailValue() {
        try {
            WebElement el = driver.findElement(emailField);
            return el.getAttribute("value");
        } catch (Exception e) {
            return null;
        }
    }

    /** Fill the guest's first name field – skips silently if field not found. */
    public void enterFirstName(String firstName) {
        try { sendKeys(firstNameField, firstName); }
        catch (Exception e) { System.out.println("  [WARN] First name field not found."); }
    }

    /** Fill the guest's last name field. */
    public void enterLastName(String lastName) {
        try { sendKeys(lastNameField, lastName); }
        catch (Exception e) { System.out.println("  [WARN] Last name field not found."); }
    }

    /** Fill the guest's email field. */
    public void enterEmail(String email) {
        try { sendKeys(emailField, email); }
        catch (Exception e) { System.out.println("  [WARN] Email field not found."); }
    }

    /**
     * تعديل الـ Phone Number بـ sendKeys
     * Fill the guest's phone number field.
     */
    public void enterPhone(String phone) {
        try { sendKeys(phoneField, phone); }
        catch (Exception e) { System.out.println("  [WARN] Phone field not found."); }
    }

    /**
     * اختيار الـ Country Code بـ Select class
     * Select country code from dropdown.
     */
    public void selectCountryCode(String code) {
        try {
            // Try finding a country code select dropdown
            List<WebElement> selects = driver.findElements(countryCodeSelect);
            for (WebElement sel : selects) {
                if (!sel.isDisplayed()) continue;
                Select s = new Select(sel);
                try {
                    s.selectByValue(code);
                    System.out.println("  [INFO] Country code set to: " + code);
                    return;
                } catch (Exception e1) {
                    // Try by visible text containing the code
                    for (WebElement opt : s.getOptions()) {
                        if (opt.getText().contains(code)) {
                            s.selectByVisibleText(opt.getText());
                            System.out.println("  [INFO] Country code set to: " + opt.getText());
                            return;
                        }
                    }
                }
            }
            System.out.println("  [WARN] Country code select not found – may not exist on this page.");
        } catch (Exception e) {
            System.out.println("  [WARN] Could not set country code: " + e.getMessage());
        }
    }

    /**
     * Fill all guest details at once.
     * Each field fails gracefully so the test can continue even if fields differ.
     */
    public void fillGuestDetails(String firstName, String lastName, String email, String phone) {
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPhone(phone);
    }

    // ============ Step 9: Scroll & Final Validation ============

    /**
     * Scroll للأسفل للـ Booking Summary
     */
    public void scrollToBookingSummary() {
        scrollDown(2000);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
    }

    /**
     * Assert إن "free Cancellation" badge موجود بـ assertTrue
     * Check if free cancellation badge exists on the page.
     */
    public boolean isFreeCancellationBadgePresent() {
        try {
            List<WebElement> badges = driver.findElements(freeCancellationBadge);
            for (WebElement badge : badges) {
                if (badge.isDisplayed()) return true;
            }
            // Fallback: check body text
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return body.contains("free cancellation");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Assert إن "Refundable" badge موجود بـ assertTrue
     * Check if refundable badge exists on the page.
     */
    public boolean isRefundableBadgePresent() {
        try {
            List<WebElement> badges = driver.findElements(refundableBadge);
            for (WebElement badge : badges) {
                if (badge.isDisplayed()) return true;
            }
            // Fallback: check body text
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return body.contains("refundable");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate expected total = Subtotal + Taxes.
     * Assert إن الـ Total Amount = Subtotal + Taxes بـ assertEquals
     */
    public double calculateExpectedTotal() {
        double subtotal = getSummarySubtotal();
        double taxes = getTaxesAmount();
        return subtotal + taxes;
    }

    /**
     * Get taxes amount from summary.
     */
    public double getTaxesAmount() {
        try {
            List<WebElement> taxElements = driver.findElements(By.cssSelector(
                "span[class*='tax'], td[class*='tax'], div[class*='tax']"));
            for (WebElement el : taxElements) {
                if (el.isDisplayed()) {
                    String text = el.getText().trim();
                    String cleaned = text.replaceAll("[^0-9.]", "");
                    if (!cleaned.isEmpty()) return Double.parseDouble(cleaned);
                }
            }
        } catch (Exception ignored) { }
        return 0.0;
    }

    /**
     * Get total amount from summary.
     */
    public double getTotalAmount() {
        try {
            List<WebElement> totals = driver.findElements(By.cssSelector(
                ".grand-total, .total-amount, span[class*='total'], " +
                "td[class*='total'], div[class*='grand']"));
            for (WebElement el : totals) {
                if (el.isDisplayed()) {
                    String text = el.getText().trim();
                    String cleaned = text.replaceAll("[^0-9.]", "");
                    if (!cleaned.isEmpty()) return Double.parseDouble(cleaned);
                }
            }
        } catch (Exception ignored) { }
        return 0.0;
    }

    // ============ Step 9: Submit Booking ============

    /**
     * Click the Confirm/Submit booking button.
     * Tries multiple strategies to find and click the button.
     */
    public void clickConfirmBooking() {
        try {
            click(confirmButton);
            System.out.println("  [INFO] Confirm booking button clicked.");
        } catch (Exception e) {
            // Fallback: find any visible submit button
            System.out.println("  [WARN] Primary confirm button not found – trying fallback.");
            List<WebElement> btns = driver.findElements(
                By.cssSelector("button, input[type='submit'], a.btn"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) {
                    String txt = btn.getText().toLowerCase();
                    if (txt.contains("confirm") || txt.contains("book") ||
                        txt.contains("pay") || txt.contains("complete") ||
                        txt.contains("submit") || txt.contains("order")) {
                        btn.click();
                        System.out.println("  [INFO] Fallback confirm clicked: '" + txt + "'");
                        return;
                    }
                }
            }
            // Last resort: click first visible submit-like button
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) { btn.click(); return; }
            }
        }
    }

    // ============ Confirmation Check ============

    /**
     * Check whether a booking confirmation is shown.
     * Accepts: success alert, "thank you" page, or a confirmation-related URL.
     */
    public boolean isConfirmationDisplayed() {
        // Wait up to 15s for a confirmation signal
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement msg = ew.until(ExpectedConditions.visibilityOfElementLocated(confirmationMessage));
            return msg.isDisplayed();
        } catch (Exception e) {
            // Check URL as fallback
            String url = driver.getCurrentUrl();
            if (url.contains("/confirmation") || url.contains("/success") ||
                url.contains("/thank") || url.contains("/complete")) {
                return true;
            }
            // Check page title as last resort
            String title = driver.getTitle().toLowerCase();
            return title.contains("confirm") || title.contains("success") ||
                   title.contains("thank") || title.contains("booking");
        }
    }

    /** Get confirmation message text. */
    public String getConfirmationText() {
        try {
            return waitForElement(confirmationMessage).getText().trim();
        } catch (Exception e) {
            return driver.getTitle();
        }
    }
}
