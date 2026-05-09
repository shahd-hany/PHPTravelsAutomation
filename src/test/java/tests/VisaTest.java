package tests;

import pages.VisaPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VisaTest extends TestBase {

    @Test
    public void testVisa() {

        System.out.println("\n--- Testing Visa Countries Only ---\n");
        VisaPage vp = new VisaPage(driver);
        vp.navigateToVisa();

        vp.selectFromCountry("Bangladesh");
        vp.selectToCountry("Brazil");
        vp.clickCheckVisa();

        System.out.println("\n--- Countries Selected Successfully ---\n");

        String actual = vp.getResultTitle();
        Assert.assertTrue(driver.getCurrentUrl().contains("/visa/"),
                "Did not navigate to results page");
        vp.clickBackToSearch();
        vp.waitForVisaPageToLoad();
        Assert.assertTrue(vp.isBackToVisaPage(),
                "Back to Search did not work");
        System.out.println("\n--- First Test Passed ---\n");
    }

    @Test
    public void testVisa_ValidationFlow() {

        VisaPage vp = new VisaPage(driver);

        vp.navigateToVisa();
        vp.selectFromCountry("Bangladesh");

        String currentUrlBefore = driver.getCurrentUrl();

        vp.clickCheckVisa();
        Assert.assertEquals(driver.getCurrentUrl(), currentUrlBefore,
                "Form submitted despite missing To Country");

        vp.selectToCountry("Brazil");
        vp.clickCheckVisa();

        Assert.assertTrue(driver.getCurrentUrl().contains("/visa/"),
                "Navigation after fixing error failed");
        System.out.println("\n--- Second Test Passed ---\n");
    }


    @Test
    public void testVisa_DataValidationFlow() {

        VisaPage vp = new VisaPage(driver);


        String from1 = "Bangladesh";
        String to1 = "Brazil";

        vp.navigateToVisa();

        vp.selectFromCountry(from1);
        vp.selectToCountry(to1);

        vp.clickCheckVisa();
        Assert.assertTrue(driver.getCurrentUrl().contains("/visa/"),
                "Navigation failed: To country likely not selected");

        Assert.assertEquals(vp.getResultFrom(), from1);
        Assert.assertEquals(vp.getResultTo(), to1);

        System.out.println("First validation passed");

        vp.clickBackToSearch();

        vp.waitForVisaPageToLoad();
        Assert.assertTrue(driver.getCurrentUrl().contains("/visa"),
                "Did not return to search page");

        String from2 = "Egypt";
        String to2 = "Afghanistan";

        vp.selectFromCountry(from2);
        vp.selectToCountry(to2);
        vp.clickCheckVisa();


        Assert.assertEquals(vp.getResultFrom(), from2);
        Assert.assertEquals(vp.getResultTo(), to2);

        System.out.println("Second validation passed");

    }

    @Test
    public void testVisa_BookingTypeSwitch() {

        VisaPage vp = new VisaPage(driver);
        vp.navigateToVisa();

        vp.selectFromCountry("Bangladesh");
        vp.selectToCountry("Brazil");

        vp.clickCheckVisa();

        Assert.assertTrue(driver.getCurrentUrl().contains("/visa/"),
                "Did not navigate to Visa Application page");


        Assert.assertTrue(vp.getBookingType().contains("Guest"),
                "Default booking type is not Guest");

        vp.selectGuestBooking();
        Assert.assertTrue(vp.isGuestFormDisplayed(),
                "Guest form not displayed");

        System.out.println("Guest mode verified");

        vp.selectMemberBooking();

        Assert.assertTrue(vp.getBookingType().contains("Member"),
                "Booking type did not switch to Member");

        Assert.assertTrue(vp.isMemberFormDisplayed(),
                "Member login form not displayed");

        System.out.println("Member mode verified");

        vp.selectGuestBooking();

        Assert.assertTrue(vp.getBookingType().contains("Guest"),
                "Booking type did not switch back to Guest");

        Assert.assertTrue(vp.isGuestFormDisplayed(),
                "Guest form not displayed after switching back");

        System.out.println("Guest mode restored successfully");

        vp.fillTravelerBasicData(
                "Dr",
                "Doaa",
                "Magdy",
                "D12345678"
        );

        System.out.println("Traveler basic data filled successfully");

        vp.scrollToTermsSection();
        Assert.assertTrue(vp.isSubmitDisabled(),
                "Submit button should be disabled before accepting terms");

        System.out.println("Submit is disabled before checkbox");
        vp.acceptTerms();
        vp.waitForSubmitEnabled();
        Assert.assertTrue(vp.isSubmitEnabled(),
                "Submit button did not enable after checking terms");

        System.out.println("Submit enabled after checkbox");
        System.out.println("\n--- Third Test Passed ---\n");
    }
}