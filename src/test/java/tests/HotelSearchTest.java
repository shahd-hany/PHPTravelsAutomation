package tests;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.HotelPage;
import pages.HotelRoomPage;
import pages.LoginPage;

import java.util.List;

public class HotelSearchTest extends TestBase {

    private LoginPage loginPage;
    private HotelPage hotelPage;
    private HotelRoomPage hotelRoomPage;
    private BookingPage bookingPage;

    private static final String EMAIL = "user@phptravels.com";
    private static final String PASSWORD = "demouser";

    @BeforeMethod
    public void initPages() {
        loginPage = new LoginPage(driver);
        hotelPage = new HotelPage(driver);
        hotelRoomPage = new HotelRoomPage(driver);
        bookingPage = new BookingPage(driver);
    }

    @Test(description = "Hotel Search, Filter, Room Selection & Booking – Scenario 2")
    public void hotelSearchFilterAndBooking() {

        loginPage.login(EMAIL, PASSWORD);

        hotelPage.navigateToStaysViaServicesMenu();

        hotelPage.selectNationality("Egypt");

        hotelPage.openGuestsDropdown();
        int currentAdults = hotelPage.getCurrentAdults();
        int currentRooms = hotelPage.getCurrentRooms();
        Assert.assertEquals(currentAdults, 2, "Adults should be 2.");
        Assert.assertEquals(currentRooms, 1, "Rooms should be 1.");
        hotelPage.closeGuestsDropdown();

        hotelPage.enterDestinationByKeys("Dubai");

        hotelPage.clickSearch();

        hotelPage.waitForResultsToLoad();

        hotelPage.applyFiveStarFilter();

        hotelPage.waitForFilteredResults();

        List<WebElement> hotelResults = hotelPage.getHotelResults();
        int resultsCount = hotelResults.size();

        Assert.assertTrue(resultsCount > 0,
                "Expected at least 1 hotel after 5-star filter, but found: " + resultsCount);

        List<WebElement> starBadges = hotelPage.getStarBadges();

        for (WebElement badge : starBadges) {
            try {
                String badgeText = badge.getText().trim();

                if (badgeText.isEmpty() || !badgeText.matches(".*\\d.*"))
                    continue;

                double starRating = Double.parseDouble(badgeText);

                Assert.assertTrue(starRating >= 5.0,
                        "FILTER BUG: Found hotel with " + starRating +
                                " stars after applying 5-star filter. Expected ≥ 5.0!");

            } catch (NumberFormatException e) {
                continue;
            }
        }

        hotelPage.sortByPriceLowToHigh();

        hotelPage.waitForSortedResults();

        String firstPriceText = hotelPage.getFirstHotelPrice();
        String secondPriceText = hotelPage.getSecondHotelPrice();

        if (!firstPriceText.isEmpty() && !secondPriceText.isEmpty()) {
            double firstPrice = hotelPage.extractPrice(firstPriceText);
            double secondPrice = hotelPage.extractPrice(secondPriceText);

            if (firstPrice > 0 && secondPrice > 0) {
                Assert.assertTrue(firstPrice <= secondPrice,
                        "Sort validation failed! Price[0]=" + firstPrice +
                                " should be ≤ Price[1]=" + secondPrice);
            } else {
            }
        }

        hotelPage.hoverOnFirstHotel();

        hotelPage.clickMoreDetailsOnFirstHotel();

        hotelPage.waitForHotelDetailPage();

        String hotelDetailName = hotelPage.getHotelDetailName();
        Assert.assertNotNull(hotelDetailName,
                "Hotel name on detail page should not be null.");

        hotelRoomPage.waitForRoomListingToLoad();
        List<WebElement> roomOptions = hotelRoomPage.getRoomOptions();
        int roomCount = roomOptions.size();

        Assert.assertTrue(roomCount > 0,
                "Expected at least 1 room option, found: " + roomCount);

        hotelRoomPage.selectRoomQuantity(0, 2);

        hotelRoomPage.selectRoom(0);
        hotelRoomPage.clickBookButtonNoNavigationWait(0);

        boolean errorAppeared = hotelRoomPage.waitForErrorMessage();

        Assert.assertTrue(errorAppeared,
                "Expected warning after clicking Continue with 2 rooms (searched for 1).");

        String errorText = hotelRoomPage.getErrorMessageText();
        Assert.assertTrue(
                errorText.toLowerCase().contains("searched for 1 room") ||
                errorText.toLowerCase().contains("must select 1 room") ||
                errorText.toLowerCase().contains("update your search"),
                "Unexpected warning text. Got: " + errorText);

        hotelRoomPage.selectRoomQuantity(0, 1);

        hotelRoomPage.selectRoom(0);

        String roomTypeName = hotelRoomPage.getRoomTypeName(0);
        Assert.assertNotNull(roomTypeName, "Room type name should not be null.");
        Assert.assertFalse(roomTypeName.isEmpty(), "Room type name should not be empty.");

        String roomPrice = hotelRoomPage.getRoomPrice(0);
        Assert.assertNotNull(roomPrice, "Room price should not be null.");

        hotelRoomPage.clickContinueBookingAndWaitForNavigation();

        bookingPage.waitForBookingPageToLoad();

        bookingPage.clickConfirmBooking();

        boolean confirmed = bookingPage.isConfirmationDisplayed();
        Assert.assertTrue(confirmed,
                "Expected a booking confirmation message.");

    }
}
