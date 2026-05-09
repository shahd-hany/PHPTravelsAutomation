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

public class HotelPage extends PageBase {

    private final By destinationField = By.cssSelector("input[placeholder='Search By City']");

    private final By nationalityToggle = By.cssSelector("[x-data*='nationalityDropdown'] .input.cursor-pointer");
    private final By nationalitySearchBox = By.cssSelector("input[placeholder='Search country...']");
    private final By nationalityOptions = By.cssSelector(".input-dropdown-item");

    private final By guestsToggle = By.cssSelector("[x-data*='guestsRoomsDropdown'] .input.cursor-pointer");
    private final By adultsHiddenInput = By.cssSelector("input[name='adults']");
    private final By roomsHiddenInput = By.cssSelector("input[name='rooms']");

    private final By searchButton = By.cssSelector("button[type='submit']");

    private final By hotelCards = By.cssSelector("div.card.overflow-hidden");

    private final By hotelNameElements = By.cssSelector("div.card h3.font-bold");

    private final By hotelPriceElements = By.cssSelector("div.card p.text-2xl.font-bold");

    private final By moreDetailsBtns = By.cssSelector("div.card a.btn");

    private final By starBadgeSpan = By.cssSelector("div.card div.absolute span");

    private final By fiveStarCheckbox = By.cssSelector("input#star-5[type='checkbox']");

    private final By starRatingToggleBtn = By.xpath("//button[.//span[normalize-space()='Star Rating']]");
    private final By starRatingPanel = By.cssSelector("div[x-show='filtersOpen.stars']");

    private final By fiveStarLabel = By.cssSelector("label[for='star-5']");

    private final By sortDropdown = By.cssSelector("select[name='sort'], select[id*='sort']");

    private final By headerMenuButtons = By.cssSelector("header nav button");

    public HotelPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToStaysViaServicesMenu() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));

        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(headerMenuButtons));
        WebElement servicesBtn = null;
        List<WebElement> btns = driver.findElements(headerMenuButtons);
        for (WebElement b : btns) {
            if (b.isDisplayed() && b.getText().contains("Services")) {
                servicesBtn = b;
                break;
            }
        }
        if (servicesBtn == null) {
            throw new RuntimeException("Services button not found on: " + driver.getCurrentUrl());
        }

        Actions actions = new Actions(driver);
        actions.moveToElement(servicesBtn).click().perform();
        By staysLink = By.cssSelector("a[href*='/stays']");
        WebElement link = ew.until(ExpectedConditions.elementToBeClickable(staysLink));
        link.click();

        ew.until(d -> d.getCurrentUrl().contains("/stays"));

        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(40))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        fw.until(d -> d.findElement(destinationField).isDisplayed());
    }

    public void openHotelsPage() {
        navigateTo("https://phptravels.net/stays");
        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        fw.until(d -> d.findElement(destinationField).isDisplayed());
    }

    public void selectNationality(String country) {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement toggle = waitForClickable(nationalityToggle);
        toggle.click();

        WebElement searchBox = ew.until(ExpectedConditions.visibilityOfElementLocated(nationalitySearchBox));
        searchBox.clear();
        searchBox.sendKeys(country);

        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        fw.until(d -> {
            List<WebElement> options = d.findElements(nationalityOptions);
            for (WebElement opt : options) {
                if (opt.isDisplayed() && opt.getText().toLowerCase().contains(country.toLowerCase())) {
                    opt.click();
                    return true;
                }
            }
            return false;
        });
    }

    public void openGuestsDropdown() {
        WebElement toggle = waitForClickable(guestsToggle);
        toggle.click();
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
        }
    }

    public int getCurrentAdults() {
        WebElement hiddenAdults = driver.findElement(adultsHiddenInput);
        try {
            return Integer.parseInt(hiddenAdults.getAttribute("value"));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public int getCurrentRooms() {
        WebElement hiddenRooms = driver.findElement(roomsHiddenInput);
        try {
            return Integer.parseInt(hiddenRooms.getAttribute("value"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public void closeGuestsDropdown() {
        try {
            driver.findElement(guestsToggle).click();
        } catch (Exception ignored) {
        }
    }

    public void enterDestinationByKeys(String city) {
        WebElement field = waitForElement(destinationField);
        field.clear();
        field.sendKeys(city);
        field.sendKeys(Keys.ESCAPE);
    }

    public void clickSearch() {
        click(searchButton);
    }

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

    public void applyFiveStarFilter() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));

        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(hotelCards));

        WebElement panel = ew.until(ExpectedConditions.presenceOfElementLocated(starRatingPanel));
        if (!panel.isDisplayed()) {
            WebElement toggle = waitForClickable(starRatingToggleBtn);
            toggle.click();
            ew.until(ExpectedConditions.visibilityOfElementLocated(starRatingPanel));
        }

        ew.until(ExpectedConditions.presenceOfElementLocated(fiveStarCheckbox));
        WebElement cb = driver.findElement(fiveStarCheckbox);
        if (cb.isSelected())
            return;

        WebElement label = ew.until(ExpectedConditions.presenceOfElementLocated(fiveStarLabel));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", label);

        ew.until(d -> d.findElement(fiveStarCheckbox).isSelected());
    }

    public void waitForFilteredResults() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(hotelCards));

    }

    public List<WebElement> getHotelResults() {
        waitForResultsToLoad();
        return findElements(hotelCards);
    }

    public List<WebElement> getHotelNames() {
        return findElements(hotelNameElements);
    }

    public List<WebElement> getHotelPrices() {
        return driver.findElements(hotelPriceElements);
    }

    public int getResultsCount() {
        return getHotelResults().size();
    }

    public List<WebElement> getStarBadges() {
        List<WebElement> spans = driver.findElements(starBadgeSpan);
        List<WebElement> ratingBadges = new ArrayList<>();

        for (WebElement span : spans) {
            try {
                if (!span.isDisplayed())
                    continue;
                String t = span.getText();
                if (t == null)
                    continue;
                t = t.trim();

                if (t.matches("\\d+\\.\\d+")) {
                    ratingBadges.add(span);
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }

        return ratingBadges;
    }

    public boolean allResultsAreFiveStars() {
        List<WebElement> badges = getStarBadges();
        if (badges.isEmpty()) {
            return false;
        }

        for (WebElement badge : badges) {
            try {
                String badgeText = badge.getText().trim();
                if (badgeText.isEmpty() || !badgeText.matches(".*\\d.*"))
                    continue;

                double starRating = Double.parseDouble(badgeText);
                if (starRating < 5.0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return true;
    }

    public void sortByPriceLowToHigh() {
        WebElement selectElement = driver.findElement(By.cssSelector("select.select"));
        Select s = new Select(selectElement);
        s.selectByValue("price_low");
    }

    public void waitForSortedResults() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        ew.until(ExpectedConditions.presenceOfAllElementsLocatedBy(hotelCards));
    }

    public double extractPrice(String priceText) {
        if (priceText == null || priceText.isBlank())
            return 0.0;
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getFirstHotelPrice() {
        List<WebElement> prices = getHotelPrices();
        if (prices.isEmpty())
            return "";
        return prices.get(0).getText().trim();
    }

    public String getSecondHotelPrice() {
        List<WebElement> prices = getHotelPrices();
        if (prices.size() < 2)
            return "";
        return prices.get(1).getText().trim();
    }

    public void hoverOnFirstHotel() {
        List<WebElement> cards = getHotelResults();
        if (cards.isEmpty())
            throw new RuntimeException("No hotel results to hover on.");
        WebElement first = cards.get(0);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", first);
        new Actions(driver).moveToElement(first).perform();
    }

    public void clickMoreDetailsOnFirstHotel() {
        clickMoreDetailsOnHotel(0);
    }

    public void clickMoreDetailsOnHotel(int index) {
        List<WebElement> btns = findElements(moreDetailsBtns);
        if (btns.isEmpty())
            throw new RuntimeException("No 'More Details' button found.");
        if (index < 0)
            index = 0;
        if (index >= btns.size())
            index = 0;

        WebElement btn = btns.get(index);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();

        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(15));
        ew.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/stay/") || url.contains("/hotel/");
        });
    }

    public void waitForHotelDetailPage() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        ew.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/stay/") || url.contains("/hotel/");
        });
         for (int attempt = 1; attempt <= 2; attempt++) {
            String title = "";
            try { title = driver.getTitle(); } catch (Exception ignored) { }
            String url = driver.getCurrentUrl();

            boolean looksLike504 =
                    (title != null && title.toLowerCase().contains("504")) ||
                    (url != null && url.toLowerCase().contains("504"));

            if (!looksLike504) break;

            driver.navigate().refresh();}
    }

    public String getHotelDetailName() {
        try {
            WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement nameEl = ew.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1, h2, h3")));
            return nameEl.getText().trim();
        } catch (Exception e) {
            return driver.getTitle().replace(" - PHPTRAVELS", "").trim();
        }
    }
}
