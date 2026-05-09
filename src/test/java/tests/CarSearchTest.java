package tests;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CarPage;

public class CarSearchTest extends TestBase {

    private static final String CITY_QUERY = "Cairo";
    private static final String CITY_DROPDOWN_TEXT = "Cairo, Egypt (EG)";

    @Test(description = "Open Cars tab, choose pickup and return city from dropdown, search, then choose first car")
    public void searchCarsAndChooseFirstCar() {
        CarPage carPage = new CarPage(driver);

        carPage.openHomePage();
        carPage.openCarsTab();

        carPage.choosePickupCity(CITY_QUERY, CITY_DROPDOWN_TEXT);
        carPage.chooseReturnCity(CITY_QUERY, CITY_DROPDOWN_TEXT);
        carPage.clickSearchCars();
        carPage.waitForResultsPage();

        String resultsUrl = carPage.getCurrentPageUrl();
        Assert.assertTrue(resultsUrl.contains("/cars/rental/"),
                "Expected cars results URL, but got: " + resultsUrl);
        Assert.assertTrue(carPage.getBookButtonsCount() > 0,
                "No car results with Book Now button were found.");

//        carPage.chooseFirstCar();
        carPage.chooseFirstCar();
        carPage.waitForBookingForm(); // بدل waitForBookingRedirect

        Assert.assertTrue(carPage.isBookingFormVisible(),
                "Expected booking form or new page after choosing first car.");

        String afterBookingClickUrl = carPage.getCurrentPageUrl();
        Assert.assertNotEquals(afterBookingClickUrl, resultsUrl,
                "Expected redirect after choosing first car, but URL did not change.");
    }
}
//tests222