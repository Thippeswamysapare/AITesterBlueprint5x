package com.wingify.automation.support;

import com.wingify.automation.pages.LoginPage;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public abstract class BaseTest {
    protected WebDriver driver;
    protected LoginPage loginPage;
    protected String baseUrl;
    private Duration timeout;
    private String browser;
    private boolean headless;

    @BeforeTest(alwaysRun = true)
    public void configure() {
        baseUrl = System.getProperty("base.url", "https://app.wingify.com/#/login");
        browser = System.getProperty("browser", "chrome").toLowerCase(Locale.ROOT);
        headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        long seconds = Long.parseLong(System.getProperty("wait.seconds", "30"));
        if (seconds <= 0 || (!browser.equals("chrome") && !browser.equals("edge"))) {
            throw new IllegalArgumentException("Use browser chrome or edge and a positive wait.seconds");
        }
        timeout = Duration.ofSeconds(seconds);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        try {
            if (browser.equals("edge")) {
                EdgeOptions options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                driver = new EdgeDriver(options);
            } else {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                driver = new ChromeDriver(options);
            }
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().window().maximize();
            loginPage = new LoginPage(driver, timeout);
            loginPage.open(baseUrl);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to initialize the Wingify browser session", exception);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver == null) {
            return;
        }
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment("Failure screenshot", "image/png",
                            new ByteArrayInputStream(screenshot), ".png");
                } catch (WebDriverException exception) {
                    Allure.addAttachment("Screenshot capture", "Browser screenshot unavailable");
                }
            }
        } finally {
            try {
                driver.quit();
            } catch (WebDriverException exception) {
                throw new IllegalStateException("Unable to close the browser session", exception);
            } finally {
                driver = null;
                loginPage = null;
            }
        }
    }
}