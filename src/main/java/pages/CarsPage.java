package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class CarsPage extends PageBase {

    public CarsPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "select2-car_from-container")
    public WebElement pickupLocation;

    @FindBy(xpath = "//button[@type='submit']")
    public WebElement searchBtn;
}