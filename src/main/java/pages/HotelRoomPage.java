package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HotelRoomPage extends PageBase {

    public HotelRoomPage(WebDriver driver) {
        super(driver);
    }

    public void waitForRoomListingToLoad() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(30));
        for (int attempt = 1; attempt <= 2; attempt++) {
            ew.until(d -> {
                String url = d.getCurrentUrl();
                if (!url.contains("/stay/") && !url.contains("/hotel/")) return false;
                try {
                    List<WebElement> roomRows = d.findElements(By.cssSelector("table tbody tr"));
                    if (!roomRows.isEmpty()) return true;
                    List<WebElement> candidates = d.findElements(
                        By.cssSelector("table, div.card, div[class*='room'], section[class*='room']"));
                    return !candidates.isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });

            String title = "";
            try { title = driver.getTitle(); } catch (Exception ignored) { }
            if (title != null && title.toLowerCase().contains("504") && attempt == 1) {
                driver.navigate().refresh();
                try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
                continue;
            }
            break;
        }

        FluentWait<WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        fw.until(d -> {
            List<WebElement> rows = d.findElements(By.cssSelector("table tbody tr"));
            for (WebElement row : rows) {
                List<WebElement> interactives = row.findElements(
                        By.cssSelector("button, select, a.btn, input"));
                if (!interactives.isEmpty()) return true;
            }
            List<WebElement> cards = d.findElements(By.cssSelector("div.card button, div.card a.btn"));
            return !cards.isEmpty();
        });
    }

    public List<WebElement> getRoomOptions() {
        waitForRoomListingToLoad();

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (!rows.isEmpty()) {
            return rows;
        }

        List<WebElement> cards = driver.findElements(
            By.cssSelector("div.room-box, div[class*='room-item'], div[class*='room-card']"));
        if (!cards.isEmpty()) {
            return cards;
        }

        List<WebElement> allCards = driver.findElements(By.cssSelector("div.card"));
        List<WebElement> visible = new ArrayList<>();
        for (WebElement c : allCards) {
            if (c.isDisplayed()) visible.add(c);
        }
        if (!visible.isEmpty()) {
            return visible;
        }

        return new ArrayList<>();
    }

    public int getRoomCount() {
        return getRoomOptions().size();
    }

    public String getRoomTypeName(int index) {
        List<WebElement> rooms = getRoomOptions();
        if (rooms.isEmpty()) return "Unknown Room";
        if (index >= rooms.size()) index = 0;
        WebElement room = rooms.get(index);

        String[] nameSelectors = {"h4", "h3", "h5", "h2", "td:first-child", "td"};
        for (String sel : nameSelectors) {
            try {
                WebElement el = room.findElement(By.cssSelector(sel));
                String text = el.getText().trim();
                if (!text.isEmpty()) return text;
            } catch (NoSuchElementException ignored) { }
        }

        String fullText = room.getText().trim();
        if (!fullText.isEmpty()) {
            for (String line : fullText.split("\\n")) {
                if (!line.trim().isEmpty()) return line.trim();
            }
        }
        return "Room " + (index + 1);
    }

    public String getRoomPrice(int index) {
        List<WebElement> rooms = getRoomOptions();
        if (rooms.isEmpty()) return "N/A";
        if (index >= rooms.size()) index = 0;
        WebElement room = rooms.get(index);

        String[] priceSelectors = {
            "span.price", "div.price", "span[class*='price']",
            "div[class*='price']", "strong", "b"
        };
        for (String sel : priceSelectors) {
            try {
                WebElement el = room.findElement(By.cssSelector(sel));
                String text = el.getText().trim();
                if (!text.isEmpty() && text.matches(".*\\d.*")) return text;
            } catch (NoSuchElementException ignored) { }
        }

        String fullText = room.getText();
        for (String line : fullText.split("\\n")) {
            if (line.matches(".*[A-Z]{3}\\s*[\\d,.]+.*")) return line.trim();
        }
        return "Price not found";
    }

    public void selectRoomQuantity(int roomIndex, int quantity) {
        try {
            List<WebElement> rooms = getRoomOptions();
            if (!rooms.isEmpty()) {
                if (roomIndex < 0) roomIndex = 0;
                if (roomIndex >= rooms.size()) roomIndex = 0;
                WebElement room = rooms.get(roomIndex);

                List<WebElement> selectsInRoom = room.findElements(By.cssSelector("select"));
                for (WebElement sel : selectsInRoom) {
                    if (!sel.isDisplayed()) continue;
                    try {
                        Select s = new Select(sel);
                        List<WebElement> opts = s.getOptions();
                        if (opts.isEmpty()) continue;
                        boolean numeric = opts.stream().allMatch(o -> o.getText().trim().matches("\\d+"));
                        if (!numeric) continue;
                        s.selectByVisibleText(String.valueOf(quantity));
                        return;
                    } catch (Exception ignored) { }
                }
            }
        } catch (Exception ignored) { }

        List<WebElement> allSelects = driver.findElements(By.cssSelector("select"));
        int qtySelectCount = 0;

        for (WebElement sel : allSelects) {
            if (!sel.isDisplayed()) continue;
            try {
                Select s = new Select(sel);
                List<WebElement> opts = s.getOptions();
                if (opts.isEmpty()) continue;

                boolean isQtySelect = opts.stream()
                        .allMatch(o -> o.getText().trim().matches("\\d+"));
                if (!isQtySelect) continue;

                if (qtySelectCount == roomIndex) {
                    s.selectByVisibleText(String.valueOf(quantity));
                    return;
                }
                qtySelectCount++;
            } catch (Exception ignored) { }
        }

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

    private final By vtWarnCard = By.cssSelector("div.vt-card.warn");

    public boolean waitForErrorMessage() {
        WebDriverWait ew = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            ew.until(d -> {
                List<WebElement> alerts = d.findElements(By.cssSelector(
                    "div.alert, div[class*='alert'], div[class*='error'], " +
                    "div[class*='warning'], div[role='alert'], .alert-danger, .alert-error, div.vt-card.warn"));
                for (WebElement alert : alerts) {
                    if (alert.isDisplayed() && !alert.getText().trim().isEmpty()) {
                        return true;
                    }
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessageText() {
        List<WebElement> alerts = driver.findElements(By.cssSelector(
            "div.alert, div[class*='alert'], div[class*='error'], " +
            "div[class*='warning'], div[role='alert'], .alert-danger, .alert-error, div.vt-card.warn"));
        for (WebElement alert : alerts) {
            if (alert.isDisplayed()) {
                String text = alert.getText().trim();
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

     private final By continueBookingBtn = By.xpath(
            "//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue')]" +
            " | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue booking')]"
    );

    public void selectRoom(int roomIndex) {
        List<WebElement> rooms = getRoomOptions();
        if (rooms.isEmpty()) throw new RuntimeException("No room options to select.");
        if (roomIndex < 0) roomIndex = 0;
        if (roomIndex >= rooms.size()) roomIndex = 0;

        WebElement room = rooms.get(roomIndex);

        List<WebElement> candidates = new ArrayList<>();
        try {
            candidates.addAll(room.findElements(By.cssSelector("button, a")));
        } catch (Exception ignored) { }

        WebElement selectBtn = null;
        boolean alreadySelected = false;
        for (WebElement el : candidates) {
            try {
                if (!el.isDisplayed() || !el.isEnabled()) continue;
                String t = (el.getText() == null) ? "" : el.getText().trim().toLowerCase();
                if (t.contains("selected")) {
                    selectBtn = el;
                    alreadySelected = true;
                    break;
                }
                if (t.contains("select")) {
                    selectBtn = el;
                    break;
                }
            } catch (StaleElementReferenceException ignored) { }
        }
        if (selectBtn == null) {
            for (WebElement el : candidates) {
                try {
                    if (el.isDisplayed() && el.isEnabled()) { selectBtn = el; break; }
                } catch (StaleElementReferenceException ignored) { }
            }
        }
        if (selectBtn == null) {
            List<WebElement> pageBtns = driver.findElements(By.cssSelector("button, a"));
            for (WebElement el : pageBtns) {
                try {
                    if (!el.isDisplayed() || !el.isEnabled()) continue;
                    String t = (el.getText() == null) ? "" : el.getText().trim().toLowerCase();
                    if (t.contains("selected")) { alreadySelected = true; selectBtn = el; break; }
                    if (t.contains("select")) { selectBtn = el; break; }
                } catch (StaleElementReferenceException ignored) { }
            }
        }
        if (selectBtn == null) {
            throw new NoSuchElementException("Could not find a Select/Selected button for the room.");
        }

        if (alreadySelected) {
        } else {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", selectBtn);

            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(selectBtn))
                        .click();
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectBtn);
            }
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(continueBookingBtn));
        } catch (Exception ignored) { }
    }

    public void clickContinueBookingNoWait() {
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.elementToBeClickable(continueBookingBtn));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try {
            btn.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickContinueBookingAndWaitForNavigation() {
        clickContinueBookingNoWait();
        WebDriverWait waitBooking = new WebDriverWait(driver, Duration.ofSeconds(25));
        waitBooking.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/checkout") || url.contains("/booking") ||
                   url.contains("/order") || url.contains("/payment") ||
                   url.contains("/confirm");
        });
    }

    public void clickBookButtonNoNavigationWait(int roomIndex) {
        clickContinueBookingNoWait();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(vtWarnCard));
        } catch (Exception ignored) { }
    }

    public void clickBookButton(int roomIndex) {
        String[] bookSelectors = {
            "a[class*='book']", "button[class*='book']",
            "a[class*='select']", "button[class*='select']",
            "a[class*='reserve']", "button[class*='reserve']",
            "button.btn", "a.btn"
        };

        List<WebElement> bookBtns = new ArrayList<>();
        for (String sel : bookSelectors) {
            List<WebElement> found = driver.findElements(By.cssSelector(sel));
            for (WebElement el : found) {
                if (!el.isDisplayed()) continue;
                String txt = el.getText().toLowerCase();
                if (txt.contains("book") || txt.contains("select") ||
                    txt.contains("reserve") || txt.contains("continue")) {
                    bookBtns.add(el);
                }
            }
            if (!bookBtns.isEmpty()) break;
        }

        if (bookBtns.isEmpty()) {
            for (WebElement el : driver.findElements(By.cssSelector("a.btn, button.btn"))) {
                if (el.isDisplayed()) bookBtns.add(el);
            }
        }

        if (bookBtns.isEmpty()) throw new RuntimeException("No Book button found.");

        WebElement btn = (roomIndex < bookBtns.size()) ? bookBtns.get(roomIndex) : bookBtns.get(0);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", btn);

        java.util.Set<String> handlesBefore = driver.getWindowHandles();
        btn.click();

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

        WebDriverWait waitBooking = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitBooking.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/checkout") || url.contains("/booking") ||
                   url.contains("/order") || url.contains("/payment") ||
                   url.contains("/confirm");
        });
    }
}
