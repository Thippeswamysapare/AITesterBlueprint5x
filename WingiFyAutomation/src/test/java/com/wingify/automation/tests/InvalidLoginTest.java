package com.wingify.automation.tests;

import com.wingify.automation.support.BaseTest;
import io.qameta.allure.Allure;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class InvalidLoginTest extends BaseTest {
    @Test(description = "Reject invalid credentials and retain the login page")
    public void invalidCredentialsAreRejected() {
        try {
            Allure.step("Verify login controls and both Remember me states", () -> {
                loginPage.waitUntilReady();
                loginPage.setRememberMe(true);
                Assert.assertTrue(loginPage.isRememberMeSelected(), "Remember me should be selected");
                loginPage.setRememberMe(false);
                Assert.assertFalse(loginPage.isRememberMeSelected(), "Remember me should be cleared");
            });
            Allure.step("Submit synthetic invalid credentials", () -> loginPage.login(
                    "automation.invalid@example.invalid", "Invalid-Automation-Password-42!", false));
            Allure.step("Verify the observed rejection and login URL", () -> {
                Assert.assertEquals(loginPage.waitForRejection(),
                        "Your email, password, IP address or location did not match");
                Assert.assertEquals(driver.getCurrentUrl(), baseUrl,
                        "Invalid credentials must not navigate away from login");
                loginPage.waitUntilReady();
            });
        } catch (RuntimeException exception) {
            Assert.fail("Invalid-login workflow could not complete", exception);
        }
    }
}