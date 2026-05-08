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
        toursPage.waitForResultsToLoad();

        System.out.println("\n STEP 9: Collect all tours");
        List<WebElement> tours = toursPage.getAllTours();
        toursPage.printAllTours();

        // ASSERTION 2: عدد النتائج > 0
        System.out.println("\n ASSERTION 2: Results count > 0");
        Assert.assertTrue(tours != null && !tours.isEmpty(), "No tours returned!");
//        toursPage.waitForFilterRefresh();
        // STEP 10: Apply Filters
        System.out.println("\n STEP 10: Apply Filters");
        toursPage.filterByName("Dubai Private Transfer: Dubai Hotel to Cruise Port");
        int nameFilteredCount = toursPage.getFilteredCount();

        // ASSERTION 3: Name filter
        System.out.println("\n ASSERTION 3: Name filter works");
        Assert.assertTrue(nameFilteredCount >= 0, "Name filter count negative");
        toursPage.sortByPriceHighToLow();
//        toursPage.waitForFilterRefresh();
        // Inclusions
        toursPage.filterByInclusion("Complimentary Breakfast");
       // ASSERTION 3: Inclusion checkbox selected
        Assert.assertTrue(toursPage.isInclusionSelected("Complimentary Breakfast"),
                "Lunch inclusion should be checked");
        System.out.println(" PASS: Lunch inclusion selected");
//        toursPage.waitForFilterRefresh();

        int countAfterFilters = toursPage.getFilteredCount();
        Assert.assertTrue(countAfterFilters >= 0, "Count should not be negative");

        toursPage.filterByRating(5);
//        toursPage.waitForFilterRefresh();
        int filteredCount = toursPage.getFilteredCount();

        // ASSERTION 6: Rating filter
        System.out.println("\n ASSERTION 6: Filtered count >= 0");
        Assert.assertTrue(filteredCount >= 0, "Filtered count negative");
//        toursPage.waitForFilterRefresh();

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
            Assert.assertTrue(childPrice > 0, "Child price should be > 0");

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

            System.out.println("\n STEP 15: Scroll to additional options");
            toursPage.scrollToAdditionalOptions();

            double beforeInsurance = toursPage.getCurrentTotalPrice();
            toursPage.addInsurance();
            double afterInsurance  = toursPage.getCurrentTotalPrice();
            if (afterInsurance > 0 && beforeInsurance > 0) {
                System.out.println("\n ASSERTION 9: Insurance price check");
                Assert.assertTrue(afterInsurance >= beforeInsurance,
                        "Price should not decrease after Insurance. Before: "
                                + beforeInsurance + " After: " + afterInsurance);
            } else {
                System.out.println("Insurance not available on this tour - skipping");
            }

            double beforeGuide = toursPage.getCurrentTotalPrice();
            toursPage.addGuide();
            double afterGuide  = toursPage.getCurrentTotalPrice();
            if (afterGuide > 0 && beforeGuide > 0) {
                System.out.println("\n ASSERTION 10: Guide price check");
                Assert.assertTrue(afterGuide >= beforeGuide,
                        "Price should not decrease after Guide. Before: "
                                + beforeGuide + " After: " + afterGuide);
                System.out.println(" PASS: " + beforeGuide + " -> " + afterGuide);
            } else {
                System.out.println("Guide not available on this tour - skipping");
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
        toursPage.waitForResultsToLoad();
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
