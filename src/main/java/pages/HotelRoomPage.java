package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Page Object for the Hotel Detail / Room Selection page.
 * URL pattern: https://phptravels.net/stay/{hotel-name}/{id}/...
 * Uses multi-strategy locators to handle the Alpine.js dynamic page.
 * Covers Steps 4, 5, 6 (room listing, validation, negative test, room selection, booking).
 */
public class HotelRoomPage extends PageBase {

    // ============ Constructor ============
    public HotelRoomPage(WebDriver driver) {
        super(driver);
    }

    // ============ Step 4: Wait for Hotel Detail Page to Load ============

    /**
     * Waits for the hotel detail page content to be ready.
     * Tries multiple selectors that may appear on the /stay/ page.
     */
    public void waitForRoomListingToLoad() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        // Wait for either: a table with rooms, or any div that looks like room content,
        // or the page title, or the main card section to be present
        ew.until(d -> {
            String url = d.getCurrentUrl();
            if (!url.contains("/stay/") && !url.contains("/hotel/")) return false;
            // Check if the page has rendered (not just redirected)
            try {
                // The /stay/ page typically loads rooms in a table or card list
                List<WebElement> candidates = d.findElements(
                    By.cssSelector("table.table, div.room-box, div[class*='room'], " +
                                   "section[class*='room'], div.card, .rooms-section"));
                return !candidates.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
        // Give Alpine.js a moment to render dynamic content
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

    // ============ Step 4: Get All Room Options – List of WebElements ============

    /**
     * جمع كل الـ Available Rooms بـ List of WebElements.
     * Returns a List of WebElements representing room options.
     * Uses a cascade of CSS strategies to find the rooms container.
     */
    public List<WebElement> getRoomOptions() {
        waitForRoomListingToLoad();

        // Strategy 1: table rows (common PHPTravels layout)
        List<WebElement> rows = driver.findElements(By.cssSelector("table.table tbody tr"));
        if (!rows.isEmpty()) {
            System.out.println("  [INFO] Found " + rows.size() + " room rows in table.");
            return rows;
        }

        // Strategy 2: div cards with room-related classes
        List<WebElement> cards = driver.findElements(
            By.cssSelector("div.room-box, div[class*='room-item'], div[class*='room-block'], " +
                           "div[class*='room-card'], div[class*='room-wrapper']"));
        if (!cards.isEmpty()) {
            System.out.println("  [INFO] Found " + cards.size() + " room cards (div).");
            return cards;
        }

        // Strategy 3: any visible card on the page (fallback)
        List<WebElement> allCards = driver.findElements(By.cssSelector("div.card"));
        List<WebElement> visible = new ArrayList<>();
        for (WebElement c : allCards) {
            if (c.isDisplayed()) visible.add(c);
        }
        if (!visible.isEmpty()) {
            System.out.println("  [INFO] Fallback: found " + visible.size() + " generic cards.");
            return visible;
        }

        System.out.println("  [WARN] No room option containers found on page: " + driver.getCurrentUrl());
        return new ArrayList<>();
    }

    /** Returns the total count of available room options. */
    public int getRoomCount() {
        return getRoomOptions().size();
    }

    // ============ Step 5: Room Validation ============

    /**
     * Read the room type name from a specific room card by index.
     * Tries multiple sub-element selectors inside each room container.
     */
    public String getRoomTypeName(int index) {
        List<WebElement> rooms = getRoomOptions();
        if (rooms.isEmpty()) return "Unknown Room";
        if (index >= rooms.size()) index = 0;
        WebElement room = rooms.get(index);

        // Try heading tags inside the room card
        String[] nameSelectors = {
            "h4", "h3", "h5", "h2",
            "div.room-name", "span.room-name", "td.room-name",
            "div[class*='room-title']", "span[class*='room-title']",
            "div[class*='name']", "td:first-child", "td"
        };
        for (String sel : nameSelectors) {
            try {
                WebElement el = room.findElement(By.cssSelector(sel));
                String text = el.getText().trim();
                if (!text.isEmpty()) return text;
            } catch (NoSuchElementException ignored) { }
        }

        // Fallback: return the full text of the row/card
        String fullText = room.getText().trim();
        if (!fullText.isEmpty()) {
            // Return first non-empty line
            for (String line : fullText.split("\\n")) {
                if (!line.trim().isEmpty()) return line.trim();
            }
        }
        return "Room " + (index + 1);
    }

    /**
     * Read the price text of a room card by index.
     */
    public String getRoomPrice(int index) {
        List<WebElement> rooms = getRoomOptions();
        if (rooms.isEmpty()) return "N/A";
        if (index >= rooms.size()) index = 0;
        WebElement room = rooms.get(index);

        String[] priceSelectors = {
            "span.price", "div.price", "h4.price", "td.price",
            "span[class*='price']", "div[class*='price']",
            "h3[class*='price']", "p[class*='price']",
            "span[class*='amount']", "div[class*='amount']",
            "strong", "b"
        };
        for (String sel : priceSelectors) {
            try {
                WebElement el = room.findElement(By.cssSelector(sel));
                String text = el.getText().trim();
                // Make sure it looks like a price (has digits)
                if (!text.isEmpty() && text.matches(".*\\d.*")) return text;
            } catch (NoSuchElementException ignored) { }
        }

        // Fallback: look for EGP / USD / price-like text anywhere in the room card
        String fullText = room.getText();
        for (String line : fullText.split("\\n")) {
            if (line.matches(".*[A-Z]{3}\\s*[\\d,.]+.*") || line.matches(".*\\$[\\d,.]+.*")) {
                return line.trim();
            }
        }
        return "Price not found";
    }

    // ============ Step 5 (Negative Test): Select 2 rooms – expect error ============

    /**
     * محاولة تغيير الـ Quantity لأوضة لـ 2 بدل 1
     * Negative Test - اختيار أوضتين عشان نشوف الـ Error Message
     *
     * @param roomIndex the room index to change quantity for
     * @param quantity the quantity to set (e.g. 2)
     */
    public void selectRoomQuantity(int roomIndex, int quantity) {
        // Find all select elements on the page
        List<WebElement> allSelects = driver.findElements(By.cssSelector("select"));
        int qtySelectCount = 0;

        for (WebElement sel : allSelects) {
            if (!sel.isDisplayed()) continue;
            try {
                Select s = new Select(sel);
                List<WebElement> opts = s.getOptions();
                if (opts.isEmpty()) continue;

                // Qty selects have purely numeric options like 0, 1, 2...
                boolean isQtySelect = opts.stream()
                        .allMatch(o -> o.getText().trim().matches("\\d+"));
                if (!isQtySelect) continue;

                if (qtySelectCount == roomIndex) {
                    s.selectByVisibleText(String.valueOf(quantity));
                    System.out.println("  [INFO] Room qty set to " + quantity + " on select #" + qtySelectCount);
                    return;
                }
                qtySelectCount++;
            } catch (Exception ignored) { }
        }

        // Fallback: select any visible select by index
        System.out.println("  [WARN] Could not find qty select for room index " + roomIndex + " – trying fallback.");
        List<WebElement> visibleSelects = new ArrayList<>();
        for (WebElement sel : allSelects) {
            if (sel.isDisplayed()) visibleSelects.add(sel);
        }
        if (!visibleSelects.isEmpty()) {
            try {
                new Select(visibleSelects.get(0)).selectByIndex(quantity);
            } catch (Exception ignored) { }
        }
    }

    // ============ Step 5 (Negative Test): Check Error Message ============

    /**
     * Explicit Wait لظهور الـ Error Message بعد اختيار أوضتين.
     * Waits for an error/alert message to appear after selecting invalid room quantity.
     *
     * @return true if error message is displayed
     */
    public boolean waitForErrorMessage() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            ew.until(d -> {
                // Check for various error message patterns
                List<WebElement> alerts = d.findElements(By.cssSelector(
                    "div.alert, div.error, div[class*='alert'], div[class*='error'], " +
                    "div[class*='warning'], span.error, p.error, " +
                    "div[class*='toast'], div[class*='notification'], " +
                    "div[role='alert'], .alert-danger, .alert-warning, .alert-error"));
                for (WebElement alert : alerts) {
                    if (alert.isDisplayed() && !alert.getText().trim().isEmpty()) {
                        return true;
                    }
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            System.out.println("  [WARN] Error message did not appear within timeout.");
            return false;
        }
    }

    /**
     * Get the error message text.
     * جيب نص الـ Error Message
     *
     * @return error message text, or empty string if not found
     */
    public String getErrorMessageText() {
        List<WebElement> alerts = driver.findElements(By.cssSelector(
            "div.alert, div.error, div[class*='alert'], div[class*='error'], " +
            "div[class*='warning'], span.error, p.error, " +
            "div[class*='toast'], div[class*='notification'], " +
            "div[role='alert'], .alert-danger, .alert-warning, .alert-error"));
        for (WebElement alert : alerts) {
            if (alert.isDisplayed()) {
                String text = alert.getText().trim();
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

    // ============ Step 6: Click Book Button ============

    /**
     * Click the "Book" / "Select" / "Reserve" button for a room.
     * Searches broadly for any booking CTA button on the page.
     */
    public void clickBookButton(int roomIndex) {
        String[] bookSelectors = {
            "a.btn-book", "button.btn-book",
            "a[class*='book']", "button[class*='book']",
            "a[class*='select']", "button[class*='select']",
            "a[class*='reserve']", "button[class*='reserve']",
            "button.btn.btn-primary", "a.btn.btn-primary",
            "button[class*='btn'][type='submit']",
            "input[type='submit']",
            // Generic: any visible button/link that says Book/Select/Reserve/Continue
            "button.btn", "a.btn"
        };

        List<WebElement> bookBtns = new ArrayList<>();
        for (String sel : bookSelectors) {
            List<WebElement> found = driver.findElements(By.cssSelector(sel));
            for (WebElement el : found) {
                if (!el.isDisplayed()) continue;
                String txt = el.getText().toLowerCase();
                // Accept buttons with booking-related text OR any .btn if nothing else found
                if (txt.contains("book") || txt.contains("select") ||
                    txt.contains("reserve") || txt.contains("continue") ||
                    txt.contains("proceed")) {
                    bookBtns.add(el);
                }
            }
            if (!bookBtns.isEmpty()) break;
        }

        // Last fallback: grab all visible .btn elements
        if (bookBtns.isEmpty()) {
            for (WebElement el : driver.findElements(By.cssSelector("a.btn, button.btn, .btn"))) {
                if (el.isDisplayed()) bookBtns.add(el);
            }
        }

        if (bookBtns.isEmpty()) throw new RuntimeException("No Book/Select button found on: " + driver.getCurrentUrl());

        WebElement btn = (roomIndex < bookBtns.size()) ? bookBtns.get(roomIndex) : bookBtns.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

        // Save handles in case it opens a new tab
        java.util.Set<String> handlesBefore = driver.getWindowHandles();
        btn.click();
        System.out.println("  [INFO] Book button clicked. Text was: '" + btn.getText().trim() + "'");

        // Check if new tab opened
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            ew.until(d -> d.getWindowHandles().size() > handlesBefore.size());
            for (String handle : driver.getWindowHandles()) {
                if (!handlesBefore.contains(handle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
        } catch (Exception ignored) { }

        // Wait for checkout/booking page to load
        WebDriverWait waitBooking = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitBooking.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/checkout") || url.contains("/booking") ||
                   url.contains("/order") || url.contains("/payment") ||
                   url.contains("/confirm");
        });
    }
}
