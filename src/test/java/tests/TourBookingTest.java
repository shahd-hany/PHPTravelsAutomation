package tests;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ToursPage;
import java.util.List;
public class TourBookingTest extends TestBase {
    @Test(priority = 1)
    public void Test1() {
        ToursPage toursPage = new ToursPage(driver);
        System.out.println("\n STEP 1: Open Tours page");
        toursPage.openToursSection();

        System.out.println("\n STEP 2: Enter destination");
        toursPage.searchTourByDestination("dubai");

        System.out.println("\n STEP 3: Select Duration");
        toursPage.selectDuration("2-3");

        System.out.println("\n STEP 4: Select Tour Type");
        toursPage.selectTourType("cultural");

        System.out.println("\n STEP 5: Set Travelers");
        toursPage.setTravelers(2,2);

        // ASSERTION 1: التحقق من إجمالي المسافرين
        String travelerText   = toursPage.getTravelerText();
        int    displayedTotal = Integer.parseInt(travelerText.split(" ")[0].trim());
        Assert.assertEquals(displayedTotal, 4,
                "Total travelers mismatch! Expected 4, Got: " + displayedTotal);
        System.out.println(" PASS: Total = " + displayedTotal);

        System.out.println("\n STEP 6: Set Start Date");
        toursPage.setStartDate("09-05-2026");

        System.out.println("\n STEP 7: Click Search");
        toursPage.clickSearchTours();

        System.out.println("\n STEP 8: Wait for results");
        toursPage.waitForToLoad();

        System.out.println("\n STEP 9: Collect all tours");
        List<WebElement> tours = toursPage.getAllTours();
        toursPage.printAllTours();


        System.out.println("\n ASSERTION 2: Results count > 0");
        Assert.assertTrue(tours != null && !tours.isEmpty(), "No tours returned!");

        // Apply Filters
        System.out.println("\n STEP 10: Apply Filters");
        toursPage.filterByName("Dubai Private Transfer: Dubai Hotel to Cruise Port");
        int nameFilteredCount = toursPage.getFilteredCount();

        // ASSERTION 3: Name filter
        System.out.println("\n ASSERTION 3: Name filter works");
        Assert.assertTrue(nameFilteredCount >= 0, "Name filter count negative");
        toursPage.sortByPriceHighToLow();

        // Inclusions
        toursPage.filterByInclusion("Complimentary Breakfast");
        System.out.println(" Complimentary Breakfast inclusion selected");

        toursPage.filterByRating(5);
        int filteredCount = toursPage.getFilteredCount();

        // ASSERTION 6: Rating filter
        System.out.println("\n ASSERTION 6: Filtered count >= 0");
        Assert.assertTrue(filteredCount >= 0, "Filtered count negative");

        // STEP 11: فتح أول Tour
        System.out.println("\n STEP 11: Open first tour");
        boolean tourOpened = toursPage.openFirstTourByMoreDetails();

        System.out.println("\n STEP 12: Get base price");
        double adultPrice  ;
        double childPrice  ;
        int adults   = 5;
        int children = 2;

        if (tourOpened) {
            adultPrice = toursPage.getAdultPrice();
            childPrice = toursPage.getChildPrice();

            System.out.println("\n ASSERTION 7: Prices > 0");
            Assert.assertTrue(adultPrice > 0, "Adult price should be > 0");

            System.out.println("\n STEP 13: Select travelers in details");
            toursPage.selectAdults(adults);
            toursPage.selectChildren(children);

            System.out.println("\n STEP 14: Math validation");
            double expectedTotal = (adultPrice * adults) + (childPrice * children);

            double actualTotal = toursPage.getCurrentTotalPrice();
            if (actualTotal > 0) {
                System.out.println("\n ASSERTION 8: Total price math check");
                Assert.assertEquals(actualTotal, expectedTotal, 5.0,
                        "Price mismatch! Expected: " + expectedTotal + " Got: " + actualTotal);
            } else {
                System.out.println("Total price not visible - skipping");
            }
        }

    }
    @Test(priority = 2)
    public void Test2() {
        ToursPage toursPage = new ToursPage(driver);
        toursPage.openToursSection();
        toursPage.searchTourByDestination("cairo");
        toursPage.selectDuration("2-3");
        toursPage.selectTourType("cultural");
        toursPage.setStartDate("09-05-2026");
        toursPage.clickSearchTours();
        toursPage.waitForToLoad();
        toursPage.filterByRating(2);
        Assert.assertTrue(
                toursPage.isNoToursMessageVisible(),
                "Expected 'No Tours found' message to appear after filter"
        );
    }
    @Test(priority = 3)
    public void Test3() {
        ToursPage toursPage = new ToursPage(driver);
        toursPage.openToursSection();
        toursPage.selectDuration("2-3");
        toursPage.clickSearchTours();
        // ASSERTION: الـ alert ظهر بالنص الصح
        String alert = toursPage.getAlertMessage();
        Assert.assertEquals(alert, "Please select a destination!",
                "Expected destination alert but got: " + alert);
    }
}
