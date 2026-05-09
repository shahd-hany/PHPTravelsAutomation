package pages;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
public class ToursPage extends PageBase {
    // ===== Search =====
    private final By destinationInput  = By.cssSelector("input[placeholder='Search By City']");
    private final By destinationItems  = By.xpath("//div[contains(@class,'cursor-pointer')]//span[@x-text='d.name || d.id']/..");
    private final By durationTrigger   = By.xpath("//input[@name='duration']/following-sibling::div[contains(@class,'input')]");
    private final By durationOptions   = By.cssSelector(".input-dropdown-content .input-dropdown-item");
    private final By tourTypeTrigger   = By.xpath("//input[@name='tour_type']/following-sibling::div[contains(@class,'input')]");
    private final By tourTypeOptions   = By.cssSelector(".input-dropdown-content .input-dropdown-item");
    private final By travelersTrigger  = By.xpath("//input[@name='travelers']/following-sibling::div[contains(@class,'input')]");
    private final By adultsSpan        = By.xpath("//span[@x-text='adults']");
    private final By adultsIncr        = By.xpath("(//div[contains(.,'Adults')]/following::button[.//span[normalize-space()='add']])[1]");
    private final By childrenIncr      = By.xpath("(//div[contains(.,'Children')]/following::button[.//span[normalize-space()='add']])[1]");
    private final By startDateInput    = By.cssSelector("input[name='start_date']");
    private By getDayLocator(String day) {return By.xpath("//div[contains(@class,'day') and not(contains(@class,'old')) " + "and not(contains(@class,'disabled')) and normalize-space(text())='" + day + "']");}
    private final By searchButton      = By.cssSelector("button[type='submit']");
    private final By noToursMessage = By.xpath("//h3[normalize-space(text())='No Tours found']");
    private final By alertMessage = By.xpath("//div[contains(@class,'alert-error')]//p");
    // ===== Results =====
    private final By loadingIndicator  = By.cssSelector("[x-show='loading']");
    private final By tourCards         = By.cssSelector(".tour-card-animate .card");
    private final By tourTitles        = By.cssSelector(".tour-card-animate h3");
    private final By tourPrices        = By.cssSelector(".tour-card-animate .text-2xl.font-bold");
    // ===== Filters =====
    private final By nameInput         = By.cssSelector("input[x-model='filters.nameSearch']");
    private final By ratingToggle      = By.xpath("//button[contains(.,'Star Rating') or .//*[contains(.,'Star Rating')]]");
    private final By sortSelect        = By.cssSelector("select[x-model='sortBy']");
    By sectionBtn = By.xpath("//button[.//span[normalize-space(text())='Inclusions']]");

    // ===== Details =====
    private final By moreDetailsButtons = By.xpath("//a[.//span[text()='More Details']]");
//   count individuals
    private final By detailsAdultsSelect   = By.cssSelector("select[x-ref='adultSelect']");
    private final By detailsChildrenSelect = By.cssSelector("select[x-ref='childSelect']");
    private final By detailsLoading        = By.xpath("//span[contains(.,'Loading price...') or contains(.,'Processing...')]");
    private final By priceH3               = By.xpath("//h3[.//span[@x-text='tourData.currency']]");
    private final By adultPrice = By.xpath("(//div[contains(@x-show,'display_price_per_adult')]//div[@x-html])[1]");
    private final By childPrice = By.xpath("(//div[contains(@x-show,'display_price_per_child')]//div[@x-html])[1]");
    public ToursPage(WebDriver driver) { super(driver); }
    // SEARCH
    public void openToursSection() {
        navigateTo("https://phptravels.net/tours");
        wait.until(ExpectedConditions.urlContains("/tours"));
        waitForToLoad();
        System.out.println("Tours page opened");
    }
    public void searchTourByDestination(String destination) {
        try {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(destinationInput));
            input.sendKeys(destination);
            wait.until(ExpectedConditions.visibilityOfElementLocated(destinationItems));
            List<WebElement> items = driver.findElements(destinationItems);
            boolean citySelected = false;
            for (WebElement item : items) {
                String text = item.getText().trim();
                if (text.toLowerCase().contains(destination.toLowerCase())) {
                    wait.until(ExpectedConditions.elementToBeClickable(item)).click();
                    citySelected = true;
                    System.out.println("Destination selected: " + text);
                    break;
                }
            }
            if (!citySelected) {
                System.out.println("Destination '" + destination + "' not found in dropdown");
            }
        } catch (Exception e) {
            System.out.println("Destination selection failed: " + e.getMessage());
        }
    }
    public boolean isNoToursMessageVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(noToursMessage)
            ).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    public String getAlertMessage() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(alertMessage)
            ).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
    public void selectDuration(String value) {
        driver.findElement(durationTrigger).click();
        for (WebElement opt : findElements(durationOptions)) {
            if (opt.getText().contains(value)) {
                opt.click();
                System.out.println("Duration: " + opt.getText().trim());
                return;
            }
        }
    }
    public void selectTourType(String type) {
        driver.findElement(tourTypeTrigger).click();
        for (WebElement opt : findElements(tourTypeOptions)) {
            if (opt.getText().toLowerCase().contains(type.toLowerCase())) {
                opt.click();
                return;
            }
        }
    }
    public void setTravelers(int adults, int children) {
        WebElement trigger = wait.until(ExpectedConditions.presenceOfElementLocated(travelersTrigger));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", trigger);
        wait.until(ExpectedConditions.elementToBeClickable(trigger)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(adultsSpan));
        for (int i = 0; i < adults - 1;   i++) {click(adultsIncr);}
        for (int i = 0; i < children; i++) {click(childrenIncr);}
        driver.findElement(By.tagName("body")).click();
    }
    public String getTravelerText() {
        return getText(By.xpath("//span[@x-text='getTravelerText']"));
    }
    public void setStartDate(String date) {
        wait.until(ExpectedConditions.elementToBeClickable(startDateInput)).click();
        String day = String.valueOf(Integer.parseInt(date.split("-")[0]));
        wait.until(ExpectedConditions.elementToBeClickable(getDayLocator(day))).click();
        // عشان اتاكد انها اتقفلت
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.datepicker")));
    }
    public void clickSearchTours() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        try { btn.click(); }
        catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
    }
    // RESULTS
    public List<WebElement> getAllTours() {
        waitForToLoad();
        List<WebElement> result = new ArrayList<>();
        for (WebElement card : driver.findElements(tourCards)) {
            try { if (card.isDisplayed()) result.add(card); }
            catch (StaleElementReferenceException ignored) {}
        }
        System.out.println("Total tours: " + result.size());
        return result;
    }
    public void printAllTours() {
        List<WebElement> titles = driver.findElements(tourTitles);
        List<WebElement> prices = driver.findElements(tourPrices);
        for (int i = 0; i < titles.size(); i++) {
            try {
                System.out.printf("[%d] %s | %s%n", i + 1,
                        titles.get(i).getText().trim(),
                        i < prices.size() ? prices.get(i).getText().trim() : "N/A");
            } catch (StaleElementReferenceException ignored) {}
        }
    }
    public boolean openFirstTourByMoreDetails() {
        waitForToLoad();
        try {
            List<WebElement> buttons =
                    wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(moreDetailsButtons));
            if (buttons.isEmpty()) {
                System.out.println("No More Details buttons found");
                return false;
            }
            WebElement firstButton = buttons.get(0);
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", firstButton);
            wait.until(ExpectedConditions.elementToBeClickable(firstButton));
            try {
                firstButton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", firstButton);
            }
            wait.until(ExpectedConditions.urlContains("/tour/"));
            System.out.println("Tour details opened: " + driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            System.out.println("Failed to open tour details: " + e.getMessage());
            return false;
        }
    }

    // FILTERS
    public void sortByPriceHighToLow() {
        selectByVisibleText(sortSelect, "Price: high To low");}
    public void filterByName(String keyword) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        input.clear();
        input.sendKeys(keyword);
        waitForToLoad();
        System.out.println("Name filter: '" + keyword );
    }
    public void filterByRating(int stars) {
        try { wait.until(ExpectedConditions.elementToBeClickable(ratingToggle)).click(); }
        catch (Exception ignored) {}
        By input = By.cssSelector("input#rating-" + stars);
        By label = By.cssSelector("label[for='rating-" + stars + "']");
        WebElement target;
        try { target = wait.until(ExpectedConditions.elementToBeClickable(label)); }
        catch (Exception e) { target = wait.until(ExpectedConditions.presenceOfElementLocated(input)); }
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", target);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);

        try {
            WebElement cb = driver.findElement(input);
            if (!cb.isSelected()) ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
        } catch (Exception ignored) {}

        waitForToLoad();
        System.out.println("Rating: " + stars );
    }
    public void filterByInclusion(String inclusionValue) {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(sectionBtn));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            if (!btn.findElement(By.cssSelector("span.material-symbols-outlined")).getAttribute("class").contains("rotate-180"))
                btn.click();
        } catch (Exception e) { System.out.println("Inclusions toggle failed: "); }
//        input
        WebElement checkbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inclusion-" + inclusionValue)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", checkbox);
        if (!checkbox.isSelected()) ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        waitForToLoad();
        System.out.println("Inclusion '" + inclusionValue );
    }
    public int getFilteredCount() {
        int count = 0;
        for (WebElement c : driver.findElements(tourCards)) {
            try { if (c.isDisplayed()) count++; }
            catch (StaleElementReferenceException ignored) {}
        }
        return count;
    }
    // Prices
    public double getAdultPrice() { return readPrice(adultPrice, "Adult"); }
    public double getChildPrice() { return readPrice(childPrice, "Child"); }
    private double readPrice(By locator, String label) {
        waitForDetailsPage();
        try {
            String txt = wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
            double price = parsePrice(txt);
            System.out.println(label + " Price: " + price);
            return price;
        } catch (Exception e) { System.out.println(label + " price not found"); return 0.0; }
    }

    // Travelers (Details Page) هختار عدد الاشخاص
    public void selectAdults(int count)   { selectTraveler(detailsAdultsSelect,   count, "Adults"); }
    public void selectChildren(int count) { selectTraveler(detailsChildrenSelect, count, "Children"); }
    private void selectTraveler(By sel, int count, String label) {
        try {
            WebDriverWait sw = new WebDriverWait(driver, Duration.ofSeconds(5));
            sw.until(ExpectedConditions.elementToBeClickable(sel));
            if (!String.valueOf(count).equals(driver.findElement(sel).getAttribute("value")))
                selectByValue(sel, String.valueOf(count));
            try { sw.until(ExpectedConditions.invisibilityOfElementLocated(detailsLoading)); } catch (Exception ignored) {}
            System.out.println(label + ": " + count);
        } catch (Exception e) { System.out.println("select" + label + " failed: " + e.getMessage()); }
    }

    // Total Price & Extra Services
    public double getCurrentTotalPrice() {
        try { new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.invisibilityOfElementLocated(detailsLoading)); } catch (Exception ignored) {}
        try { double p = parsePrice(driver.findElement(priceH3).getText());
        if (p > 0) { System.out.println("Total: " + p); return p; } } catch (Exception ignored) {}
        return 0.0;
    }
    // HELPERS
    public void waitForToLoad() {
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator)); }
        catch (Exception ignored) {}
        try { wait.until(ExpectedConditions.presenceOfElementLocated(tourCards));
        }
        catch (Exception e) { System.out.println("No tour cards found"); }
    }
    private void waitForDetailsPage() {
        WebDriverWait sw = new WebDriverWait(driver, Duration.ofSeconds(15));
        try { sw.until(ExpectedConditions.urlContains("/tour/")); } catch (Exception ignored) {}
        try { sw.until(ExpectedConditions.invisibilityOfElementLocated(detailsLoading)); } catch (Exception ignored) {}
        try { sw.until(ExpectedConditions.presenceOfElementLocated(detailsAdultsSelect)); } catch (Exception ignored) {}
    }
    private double parsePrice(String text) {
        try {
//            اي حاجه مش رقم همسحها
            String n = text.replaceAll("[^0-9.]", "");
            return n.isEmpty() ? 0.0 : Double.parseDouble(n);
        } catch (Exception e) {
            return 0.0;
        }
    }
}