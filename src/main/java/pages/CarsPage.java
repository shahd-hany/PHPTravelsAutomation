package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CarsPage extends PageBase {

    public CarsPage(WebDriver driver) {
        super(driver);
    }

    By pickupLocation = By.id("select2-car_from-container");
    By searchBtn = By.xpath("//button[@type='submit']");
    By pickUpDate = By.id("datefrom");

    public void selectPickupLocation(String locationName) {
        click(pickupLocation);
        sendKeys(pickupLocation, locationName);
    }

    public void selectDate(String date) {
        sendKeys(pickUpDate, date);
    }
    public void clickSearch() {
        click(searchBtn);
    }
}