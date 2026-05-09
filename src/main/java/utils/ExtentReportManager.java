package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

public class ExtentReportManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            
            sparkReporter.config().setDocumentTitle("PHPTravels Automation Report");
            sparkReporter.config().setReportName("Test Execution Results");
            sparkReporter.config().setTheme(Theme.STANDARD);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Project", "PHPTravels Automation");
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Tester", "Automation Engineer");
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }
}
