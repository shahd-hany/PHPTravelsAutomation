package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.util.List;

public class VisaPage extends PageBase {

    public VisaPage(WebDriver driver) {
        super(driver);
    }

    // ========== LOCATORS ==========
    By visa_tab = By.xpath("//button[.//span[normalize-space()='Visa']]");

    By form_country = By.xpath("(//span[contains(text(),'Select Country')])[1]");
    By search_form_country = By.xpath("(//input[@placeholder='Search...'])[1]");

    By to_country = By.xpath("(//div[contains(@class,'input') and .//span[contains(text(),'Select Country')]])[2]");
    By search_to_country = By.xpath("(//input[@placeholder='Search...'])[2]");

    By all_country = By.xpath("//div[contains(@class,'input-dropdown-item') and not(contains(@style,'display: none'))]");

    By check_visa_btn = By.xpath("//button[.//span[normalize-space()='Check Visa']]");

    By result_title = By.xpath("//h1[contains(@class,'text-2xl')]");

    By back_to_search_btn = By.xpath("//a[@href='https://phptravels.net/visa']");
    By error_message = By.xpath("//div[contains(@class,'alert-error')]//p");

    By result_from = By.xpath("(//div[text()='From']/following-sibling::div)[1]");
    By result_to = By.xpath("(//div[text()='To']/following-sibling::div)[1]");

    By guest_option = By.xpath("//label[contains(.,'Guest Booking')]");
    By member_option = By.xpath("//label[contains(.,'Login your Account')]");
    By guest_first_name = By.xpath("//input[@placeholder='Enter First Name']");
    By login_email = By.id("quick_login_email");
    By login_password = By.id("quick_login_password");
    By booking_label = By.xpath("//span[contains(text(),'Guest') or contains(text(),'Member')]");
    By terms_checkbox = By.id("terms_accepted");
    By terms_label = By.xpath("//label[@for='terms_accepted']");
    By submit_btn = By.xpath("//button[contains(.,'Submit Application')]");

    By traveler_title = By.xpath("//label[contains(text(),'Title')]/following::select[1]");
    By traveler_firstName = By.xpath("(//input[@type='text'])[1]");
    By traveler_lastName = By.xpath("(//input[@type='text'])[2]");
    By traveler_passport = By.xpath("//input[@placeholder='Enter Passport Number']");

    // ========== ACTIONS ==========

    public void navigateToVisa() {
        click(visa_tab);
        waitForElement(check_visa_btn);
    }

    public void selectFromCountry(String country) {
        click(form_country);
        waitForElement(search_form_country);
        sendKeys(search_form_country, country);
        fluentWait(all_country);
        List<WebElement> countries = findElements(all_country);
        for (WebElement c : countries) {
            if (c.getText().contains(country)) {
                c.click();
                break;
            }
        }
    }

    public void selectToCountry(String country) {

        click(to_country);
        waitForElement(search_to_country);
        sendKeys(search_to_country, country);
        fluentWait(all_country);

        List<WebElement> countries = findElements(all_country);

        boolean found = false;

        for (WebElement c : countries) {
            String text = c.getText().trim();
            if (text.toLowerCase().contains(country.toLowerCase())) {
                c.click();
                found = true;
                break;
            }
        }
        if (!found && !countries.isEmpty()) {
            System.out.println("Selecting first suggestion instead");
            countries.get(0).click();
            found = true;
        }
        if (!found) {
            System.out.println("Available options:");
            for (WebElement c : countries) {
                System.out.println(c.getText());
            }

            throw new RuntimeException("Country not found in dropdown: " + country);
        }
    }

    public void clickCheckVisa() {
        click(check_visa_btn);
    }

    public String getResultTitle() {

        return getText(result_title);
    }

    public void clickBackToSearch() {
        waitForElement(By.tagName("h1"));
        waitForElement(back_to_search_btn);
        click(back_to_search_btn);
        wait.until(driver -> driver.getCurrentUrl().equals("https://phptravels.net/visa"));
    }
    public boolean isBackToVisaPage() {
        return driver.getCurrentUrl().contains("/visa");
    }

    public void waitForVisaPageToLoad() {
        waitForElement(check_visa_btn);
    }

    public String getResultFrom() {
        waitForElement(result_from);
        return getText(result_from);
    }

    public String getResultTo() {
        return getText(result_to);
    }

    public void selectGuestBooking() {
        waitForElement(guest_option);
        scrollToElement(guest_option);
        click(guest_option);
    }
    public void selectMemberBooking() {
        waitForElement(member_option);
        scrollToElement(member_option);
        click(member_option);
    }
    public boolean isGuestFormDisplayed() {
        scrollToElement(guest_first_name);
        waitForElement(guest_first_name);
        return driver.findElement(guest_first_name).isDisplayed();
    }
    public boolean isMemberFormDisplayed() {
        return isDisplayed(login_email) && isDisplayed(login_password);
    }
    public String getBookingType() {
        return getText(booking_label);
    }

    // Terms Section
    public void scrollToTermsSection() {
        scrollToElement(terms_checkbox);
    }
    public void acceptTerms() {

        WebElement checkbox = driver.findElement(terms_checkbox);

        scrollToElement(terms_checkbox);

        if (!checkbox.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        }
    }
    public boolean isSubmitDisabled() {
        return driver.findElement(submit_btn).getAttribute("disabled") != null;
    }
    public boolean isSubmitEnabled() {
        return driver.findElement(submit_btn).isEnabled();
    }
    public void waitForSubmitEnabled() {
        wait.until(driver ->
                driver.findElement(submit_btn).getAttribute("disabled") == null
        );
    }

    // Traveler Data
    public void selectTravelerTitle(String title) {

        scrollToElement(traveler_title);
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(traveler_title));
        dropdown.click();
        By optionLocator = By.xpath("//option[normalize-space()='" + title + "']");
        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);

        wait.until(driver -> new Select(dropdown).getOptions().size() > 1);
        Select select = new Select(dropdown);

    }

    public void enterTravelerFirstName(String firstName) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(traveler_firstName));
        element.clear();
        element.sendKeys(firstName);
    }
    public void enterTravelerLastName(String lastName) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(traveler_lastName));
        element.clear();
        element.sendKeys(lastName);
    }
    public void enterPassportNumber(String passport) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(traveler_passport));
        element.clear();
        element.sendKeys(passport);
    }
    public void fillTravelerBasicData(String title, String first, String last, String passport) {

        scrollToElement(traveler_title);

        selectTravelerTitle(title);
        enterTravelerFirstName(first);
        enterTravelerLastName(last);
        enterPassportNumber(passport);
    }

}