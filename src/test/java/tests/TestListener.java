package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExtentReportManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {

    private ExtentReports extent = ExtentReportManager.getInstance();
    private ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, "Test Failed: " + result.getThrowable());
        
        try {
            Object currentClass = result.getInstance();
            WebDriver driver = ((TestBase) currentClass).getDriver();
            
            if (driver != null) {
                String screenshotPath = takeScreenshot(driver, result.getMethod().getMethodName());
                test.get().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            }
        } catch (Exception e) {
            test.get().log(Status.WARNING, "Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
    
    private String takeScreenshot(WebDriver driver, String methodName) throws IOException {
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        
        String destinationPath = System.getProperty("user.dir") + "/test-output/Screenshots/" + methodName + "_" + dateName + ".png";
        File finalDestination = new File(destinationPath);
        
        File parentDir = finalDestination.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        Files.copy(source.toPath(), finalDestination.toPath());
        
        // Return absolute or relative path as needed by ExtentReports
        return finalDestination.getAbsolutePath();
    }
}
