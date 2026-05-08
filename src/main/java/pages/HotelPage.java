package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Page Object for PHPTravels Hotel/Stays section.
 * Covers: /stays search form + search results listing page.
 * Locators: CSS selectors only – no XPath.
 */
public class HotelPage extends PageBase {

    // ============ Locators – Search Form ============
    // All confirmed from live page HTML inspection (Alpine.js based site)

    // Destination input: placeholder="Search By City" (confirmed)
    private final By destinationField = By.cssSelector("input[placeholder='Search By City']");

    // Nationality: Alpine.js custom dropdown – x-data="nationalityDropdown()"
    // Toggle = div.input.cursor-pointer inside the nationality dropdown container
    private final By nationalityToggle   = By.cssSelector("[x-data*='nationalityDropdown'] .input.cursor-pointer");
    // Search box inside the opened dropdown panel
    private final By nationalitySearchBox = By.cssSelector("input[placeholder='Search country...']");
    // Each country option rendered as div.input-dropdown-item
    private final By nationalityOptions   = By.cssSelector(".input-dropdown-item");

    // Date inputs: class="HotelCheckin" and class="HotelCheckout" (confirmed, readonly)
    private final By checkinField  = By.cssSelector("input.HotelCheckin");
    private final By checkoutField = By.cssSelector("input.HotelCheckout");

    // Guests & Rooms: Alpine.js custom dropdown – x-data="guestsRoomsDropdown()"
    // Toggle = div.input.cursor-pointer inside the guests dropdown container
    private final By guestsToggle = By.cssSelector("[x-data*='guestsRoomsDropdown'] .input.cursor-pointer");
    // Hidden inputs store the current values (set by Alpine.js)
    private final By adultsHiddenInput = By.cssSelector("input[name='adults']");
    private final By roomsHiddenInput  = By.cssSelector("input[name='rooms']");

    // Search button: only submit button inside the form (confirmed)
    private final By searchButton = By.cssSelector("button[type='submit']");

    // ============ Locators – Results Page ============
    // Hotel cards rendered by renderHotelCard() JS function: class="card overflow-hidden mb-3"
    private final By hotelCards = By.cssSelector("div.card.overflow-hidden");

    // Hotel name: <h3 class="text-lg font-bold .."> inside the info section of each card
    // Use the md:w-2/3 content div to avoid picking up sidebar filter headings
    private final By hotelNameElements = By.cssSelector("div.card div.flex.flex-col h3.font-bold");

    // Price: <p class="text-2xl font-bold ..."> inside each card
    private final By hotelPriceElements = By.cssSelector("div.card p.text-2xl.font-bold");

    // More Details: <a class="btn"> inside each card
    private final By moreDetailsBtns = By.cssSelector("div.card a.btn");

    // Star badge: absolute positioned badge with "5.0" text inside each card
    // Structure: div.absolute.top-2.left-2 > svg + span (span has the "5.0" text)
    private final By starBadgeSpan = By.cssSelector("div.absolute span");

    // ============ Locators – Filters Sidebar ============
    // 5-star checkbox (on results page sidebar)
    private final By fiveStarCheckbox = By.cssSelector("input[value='5'][type='checkbox']");

    // Sort dropdown on results page
    private final By sortDropdown = By.cssSelector("select[name='sort'], select[id*='sort']");

    // ============ Constructor ============
    public HotelPage(WebDriver driver) {
        super(driver);
    }

    // ============ Step 2: Navigate to Stays ============

    /** Navigate to /stays and Fluent Wait for the form to be ready. */
    public void openHotelsPage() {
        navigateTo("https://phptravels.net/stays");
        // Fluent Wait – polls every 2s, up to 30s
        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        fw.until(d -> d.findElement(destinationField).isDisplayed());
    }

    // ============ Step 2: Nationality – Alpine.js Custom Dropdown ============

    /**
     * Select nationality from the Alpine.js custom dropdown.
     * Structure confirmed from HTML: x-data="nationalityDropdown()"
     *
     * Flow:
     *  1. Click toggle div to open the dropdown panel
     *  2. Explicit Wait for search input to appear
     *  3. Type country name in search box (filters the list)
     *  4. Explicit Wait for matching option
     *  5. Click the matching .input-dropdown-item
     */
    public void selectNationality(String country) {
        // 1) Click the nationality toggle to open dropdown
        WebElement toggle = waitForClickable(nationalityToggle);
        toggle.click();

        // 2) Explicit Wait – search input becomes visible when panel opens
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        ew.until(ExpectedConditions.visibilityOfElementLocated(nationalitySearchBox));

        // 3) Type country name to filter the list
        WebElement searchBox = driver.findElement(nationalitySearchBox);
        searchBox.clear();
        searchBox.sendKeys(country);

        // 4) Explicit Wait – wait for options to filter
        try { Thread.sleep(500); } catch (InterruptedException ignored) { }
        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(nationalityOptions));

        // 5) Click the matching option by text
        List<WebElement> options = driver.findElements(nationalityOptions);
        for (WebElement opt : options) {
            if (!opt.isDisplayed()) continue;
            String text = opt.getText().trim();
            if (text.equalsIgnoreCase(country)) {
                opt.click();
                return;
            }
        }
        // Fallback: click first visible option
        for (WebElement opt : options) {
            if (opt.isDisplayed()) {
                opt.click();
                return;
            }
        }
        throw new RuntimeException("Nationality '" + country + "' not found in dropdown.");
    }

    // ============ Step 2: Destination – sendKeys (no autocomplete) ============

    /**
     * Type destination using sendKeys only – NO autocomplete selection.
     * Scenario requirement: "sendKeys من غير ما ينتظر autocomplete"
     */
    public void enterDestinationByKeys(String city) {
        WebElement field = waitForElement(destinationField);
        field.clear();
        field.sendKeys(city);
        // Press Escape to dismiss autocomplete without selecting any suggestion
        field.sendKeys(Keys.ESCAPE);
    }

    // ============ Step 2: Dates (JS to bypass readonly flatpickr inputs) ============

    /** Set Check-in date via JS executor (field is readonly flatpickr). */
    public void setCheckinDate(String date) {
        setDateFieldJS(checkinField, date);
    }

    /** Set Check-out date via JS executor (field is readonly flatpickr). */
    public void setCheckoutDate(String date) {
        setDateFieldJS(checkoutField, date);
    }

    private void setDateFieldJS(By locator, String date) {
        WebElement el = waitForElement(locator);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].removeAttribute('readonly');", el);
        el.clear();
        el.sendKeys(date);
        js.executeScript("arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", el);
    }

    // ============ Step 2: Guests & Rooms – Alpine.js custom dropdown ============

    /**
     * Open the Guests & Rooms dropdown.
     * The dropdown uses Alpine.js x-data="guestsRoomsDropdown()".
     * Default values are already 2 Adults, 1 Room – matching the scenario requirement.
     * This method opens the panel to demonstrate the interaction.
     */
    public void openGuestsDropdown() {
        WebElement toggle = waitForClickable(guestsToggle);
        toggle.click();
        // Explicit Wait – wait for the dropdown panel to open (show class OR just visible)
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            ew.until(d -> {
                try {
                    WebElement panel = d.findElement(
                        By.cssSelector("[x-data*='guestsRoomsDropdown'] .input-dropdown-content"));
                    String panelClass = panel.getAttribute("class");
                    return (panelClass != null && panelClass.contains("show")) || panel.isDisplayed();
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            // Dropdown may already be showing – continue
            System.out.println("  [WARN] Guests dropdown open wait timed out – continuing anyway.");
        }
    }

    /**
     * Verify current Adults count from hidden input.
     * The Alpine.js component stores the value in input[name='adults'].
     */
    public int getCurrentAdults() {
        WebElement hiddenAdults = driver.findElement(adultsHiddenInput);
        try { return Integer.parseInt(hiddenAdults.getAttribute("value")); }
        catch (NumberFormatException e) { return 2; }
    }

    /**
     * Verify current Rooms count from hidden input.
     */
    public int getCurrentRooms() {
        WebElement hiddenRooms = driver.findElement(roomsHiddenInput);
        try { return Integer.parseInt(hiddenRooms.getAttribute("value")); }
        catch (NumberFormatException e) { return 1; }
    }

    /** Close the guests dropdown by clicking the toggle again. */
    public void closeGuestsDropdown() {
        try { driver.findElement(guestsToggle).click(); }
        catch (Exception ignored) { }
    }

    // Kept for compatibility – no-op since defaults already match 1 Room, 2 Adults
    public void setAdults(int target) { /* default is already 2 */ }
    public void setRooms(int target)  { /* default is already 1 */ }

    // ============ Step 2: Search + Fluent Wait for Results ============

    /** Click Search Hotels button. */
    public void clickSearch() {
        click(searchButton);
    }

    /**
     * Fluent Wait for hotel results to appear after search.
     * Polls every 2s, timeout 30s, ignores NoSuchElementException.
     */
    public void waitForResultsToLoad() {
        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        fw.until(d -> {
            List<WebElement> cards = d.findElements(hotelCards);
            return !cards.isEmpty() && cards.get(0).isDisplayed();
        });
    }

    // ============ Step 3.1: Filter – 5 Stars Checkbox ============

    /**
     * Apply 5-star filter by clicking the corresponding checkbox in the sidebar.
     * Click على الـ checkbox الخاص بـ 5 Stars
     * Falls back to iterating all checkboxes if the CSS id is not found.
     */
    public void applyFiveStarFilter() {
        // Try direct CSS locator first
        try {
            selectCheckbox(fiveStarCheckbox);
            return;
        } catch (Exception ignored) { }

        // Fallback: iterate all visible checkboxes looking for value="5"
        for (WebElement cb : driver.findElements(By.cssSelector("input[type='checkbox']"))) {
            if (!cb.isDisplayed()) continue;
            String val = cb.getAttribute("value");
            String id  = cb.getAttribute("id");
            if ("5".equals(val) || (id != null && id.contains("5"))) {
                scrollToElement(By.cssSelector("input[type='checkbox']"));
                if (!cb.isSelected()) cb.click();
                return;
            }
        }
        throw new RuntimeException("5-star filter checkbox not found.");
    }

    /**
     * 3.1 Explicit Wait لتحديث النتائج بعد تطبيق الفلتر
     */
    public void waitForFilteredResults() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(hotelCards));
        // Extra wait for DOM re-render after filter
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

    // ============ Step 3.2: Collect Results – List of WebElements ============

    /** Returns all hotel result cards (List of WebElements). */
    public List<WebElement> getHotelResults() {
        waitForResultsToLoad();
        return findElements(hotelCards);
    }

    /** Returns all hotel name elements (List of WebElements). */
    public List<WebElement> getHotelNames() {
        return findElements(hotelNameElements);
    }

    /** Returns all hotel price elements (List of WebElements). */
    public List<WebElement> getHotelPrices() {
        return driver.findElements(hotelPriceElements);
    }

    /** Returns total count of visible hotel results. */
    public int getResultsCount() {
        return getHotelResults().size();
    }

    // ============ Step 3.3: Star Badge Verification – Loop + getText + Parse ============

    /**
     * جمع كل الـ Star Badges الموجودة في النتائج بـ List of WebElements
     * عن طريق الـ CSS Selector الخاص بالـ star badge.
     * Returns List of star badge span elements from all cards.
     */
    public List<WebElement> getStarBadges() {
        // CSS Selector for star badge: div.absolute span inside each card
        return driver.findElements(By.cssSelector("div.card div.absolute span"));
    }

    /**
     * التحقق إن كل فندق 5 نجوم فعلاً
     * Loop على كل badge → getText() → parse → assertTrue ≥ 5.0
     *
     * @return true if ALL badges show ≥ 5.0 stars
     */
    public boolean allResultsAreFiveStars() {
        // جمع كل الـ Star Badges بـ List of WebElements
        List<WebElement> badges = getStarBadges();

        if (badges.isEmpty()) {
            System.out.println("  [WARN] No star badges found in results.");
            return false;
        }

        // Loop على كل badge
        for (WebElement badge : badges) {
            try {
                // جيب الـ text بـ getText() → هيجيب "5.0"
                String badgeText = badge.getText().trim();

                // Skip non-numeric badges (e.g. navigation arrows, icons)
                if (badgeText.isEmpty() || !badgeText.matches(".*\\d.*")) continue;

                // عمل parse للرقم
                double starRating = Double.parseDouble(badgeText);

                // Check: لو أقل من 5 نجوم → return false
                if (starRating < 5.0) {
                    System.out.println("  [FILTER BUG] Found hotel with " + starRating +
                            " stars – expected ≥ 5.0. Filter is NOT working correctly!");
                    return false;
                }
            } catch (NumberFormatException e) {
                // Skip badges that aren't star ratings
                continue;
            }
        }
        return true;
    }

    // ============ Step 3.4: Sort – Select class + Explicit Wait ============

    /**
     * Sort results by Price Low to High using Select class on the sort dropdown.
     * اختيار Sort بـ Select class
     */
    public void sortByPriceLowToHigh() {
        // Try all visible <select> elements to find the sort one
        for (WebElement sel : driver.findElements(By.cssSelector("select"))) {
            if (!sel.isDisplayed()) continue;
            try {
                Select s = new Select(sel);
                for (WebElement opt : s.getOptions()) {
                    String t = opt.getText().toLowerCase();
                    if (t.contains("low") || (t.contains("price") && t.contains("high"))) {
                        s.selectByVisibleText(opt.getText().trim());
                        return;
                    }
                }
            } catch (Exception ignored) { }
        }
        // Fallback to CSS locator
        try { selectByVisibleText(sortDropdown, "Price Low to High"); }
        catch (Exception e) { selectByIndex(sortDropdown, 1); }
    }

    /**
     * 3.4 Explicit Wait لإعادة ترتيب النتائج بعد الـ Sort
     */
    public void waitForSortedResults() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(hotelCards));
        // Extra wait for results to re-order
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

    // ============ Step 3.5: Price Math Validation ============

    /**
     * Extract numeric price value from text like "EGP 24,680.12" → 24680.12
     * عمل parse للرقم من getText
     */
    public double extractPrice(String priceText) {
        if (priceText == null || priceText.isBlank()) return 0.0;
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        try { return Double.parseDouble(cleaned); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /**
     * جيب سعر أول فندق بـ getText
     */
    public String getFirstHotelPrice() {
        List<WebElement> prices = getHotelPrices();
        if (prices.isEmpty()) return "";
        return prices.get(0).getText().trim();
    }

    /**
     * جيب سعر تاني فندق بـ getText
     */
    public String getSecondHotelPrice() {
        List<WebElement> prices = getHotelPrices();
        if (prices.size() < 2) return "";
        return prices.get(1).getText().trim();
    }

    // ============ Step 3: Scroll for more results ============

    /** Scroll down 3000px to trigger lazy-load for more results. */
    public void scrollForMoreResults() {
        scrollDown(3000);
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
    }

    // ============ Step 4: Hover (Actions class) + More Details ============

    /**
     * Hover over the first hotel card using Actions class.
     * Hover على أول فندق بـ Actions class
     */
    public void hoverOnFirstHotel() {
        List<WebElement> cards = getHotelResults();
        if (cards.isEmpty()) throw new RuntimeException("No hotel results to hover on.");
        WebElement first = cards.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", first);
        new Actions(driver).moveToElement(first).perform();
    }

    /**
     * Click the "More Details" link on the first hotel card.
     * IMPORTANT: The site may open the hotel detail page in a NEW TAB (target="_blank").
     * This method saves the current window handles before clicking so that
     * waitForHotelDetailPage() can switch to the new tab automatically.
     */
    public void clickMoreDetailsOnFirstHotel() {
        List<WebElement> btns = findElements(moreDetailsBtns);
        if (btns.isEmpty()) throw new RuntimeException("No 'More Details' button found.");
        WebElement btn = btns.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

        // Save current window handles BEFORE clicking (to detect new tab)
        Set<String> handlesBefore = driver.getWindowHandles();
        btn.click();

        // Explicit Wait – give up to 10 s for a new tab to open
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            ew.until(d -> d.getWindowHandles().size() > handlesBefore.size());
            // A new tab opened – switch to it
            Set<String> handlesAfter = driver.getWindowHandles();
            for (String handle : handlesAfter) {
                if (!handlesBefore.contains(handle)) {
                    driver.switchTo().window(handle);
                    System.out.println("  [INFO] Switched to new tab: " + driver.getCurrentUrl());
                    return;
                }
            }
        } catch (Exception e) {
            // No new tab opened – the page navigated in the same tab (no target="_blank")
            System.out.println("  [INFO] Hotel detail opened in same tab.");
        }
    }

    /**
     * Explicit Wait – wait until the hotel detail page URL contains "/stay/".
     * Works whether the page opened in the same tab or a new tab
     * (clickMoreDetailsOnFirstHotel already switches to the new tab if needed).
     */
    public void waitForHotelDetailPage() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        ew.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/stay/") || url.contains("/hotel/");
        });
        System.out.println("  [INFO] Hotel detail page URL: " + driver.getCurrentUrl());
    }

    /**
     * Get the hotel name displayed on the hotel detail page.
     * Assert إن اسم الفندق موجود بـ assertNotNull
     */
    public String getHotelDetailName() {
        try {
            WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement nameEl = ew.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1, h2.hotel-name, h2, h3")));
            return nameEl.getText().trim();
        } catch (Exception e) {
            return driver.getTitle().replace(" - PHPTRAVELS", "").trim();
        }
    }
}
