package com.wingify.automation.pages;

import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//input[@id='login-username']")
    private WebElement email;

    @FindBy(xpath = "//input[@id='login-password']")
    private WebElement password;

    @FindBy(xpath = "//button[@id='js-login-btn']")
    private WebElement signIn;

    @FindBy(xpath = "//input[@id='checkbox-remember']")
    private WebElement rememberMe;

    @FindBy(xpath = "//label[@for='checkbox-remember']")
    private WebElement rememberMeLabel;

    @FindBy(xpath = "//*[normalize-space(text())='Your email, password, IP address or location did not match']")
    private WebElement rejectionMessage;

    @FindBy(xpath = "//button[@aria-label='Toggle password visibility']")
    private WebElement passwordVisibility;

    public LoginPage(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "Driver is required");
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    public void open(String url) {
        try {
            driver.get(url);
            waitUntilReady();
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to open the Wingify login page", exception);
        }
    }

    public void waitUntilReady() {
        try {
            wait.until(ExpectedConditions.visibilityOf(email));
            wait.until(ExpectedConditions.visibilityOf(password));
            wait.until(ExpectedConditions.elementToBeClickable(signIn));
            wait.until(ExpectedConditions.visibilityOf(rememberMeLabel));
            wait.until(ExpectedConditions.elementToBeClickable(passwordVisibility));
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Wingify login controls are not ready", exception);
        }
    }

    public boolean isRememberMeSelected() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath("//input[@id='checkbox-remember']")));
            return rememberMe.isSelected();
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to read Remember me state", exception);
        }
    }

    public void setRememberMe(boolean selected) {
        try {
            if (isRememberMeSelected() != selected) {
                wait.until(ExpectedConditions.elementToBeClickable(rememberMeLabel)).click();
            }
            wait.until(ignored -> rememberMe.isSelected() == selected);
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to update Remember me state", exception);
        }
    }

    public void login(String emailAddress, String secret, boolean remember) {
        Objects.requireNonNull(emailAddress, "Email is required");
        Objects.requireNonNull(secret, "Password is required");
        try {
            waitUntilReady();
            email.clear();
            email.sendKeys(emailAddress);
            password.clear();
            password.sendKeys(secret);
            setRememberMe(remember);
            wait.until(ExpectedConditions.elementToBeClickable(signIn)).click();
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to submit the Wingify login form", exception);
        }
    }

    public String waitForRejection() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(rejectionMessage)).getText().trim();
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Expected invalid-login response was not displayed", exception);
        }
    }

    public void waitForAuthenticatedUrl(String expectedUrl) {
        try {
            wait.until(ExpectedConditions.urlToBe(expectedUrl));
            wait.until(ExpectedConditions.invisibilityOf(email));
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Login did not reach the configured authenticated URL", exception);
        }
    }
}