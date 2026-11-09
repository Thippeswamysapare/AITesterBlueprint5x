package com.wingify.automation.tests;

import com.wingify.automation.support.BaseTest;
import io.qameta.allure.Allure;
import java.net.URI;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public final class ValidLoginTest extends BaseTest {
    @Test(description = "Authenticate a supplied valid account and verify its expected destination")
    public void validCredentialsAuthenticate() {
        String email = System.getenv("WINGIFY_EMAIL");
        String password = System.getenv("WINGIFY_PASSWORD");
        String expectedUrl = System.getenv("WINGIFY_SUCCESS_URL");
        if (email == null || email.isBlank() || password == null || password.isBlank()
                || expectedUrl == null || expectedUrl.isBlank()) {
            throw new SkipException("Configure WINGIFY_EMAIL, WINGIFY_PASSWORD and WINGIFY_SUCCESS_URL");
        }
        try {
            URI destination = URI.create(expectedUrl);
            Assert.assertEquals(destination.getScheme(), "https", "Success URL must use HTTPS");
            Assert.assertEquals(destination.getHost(), URI.create(baseUrl).getHost(),
                    "Success URL must belong to the target application");
            Assert.assertNotEquals(expectedUrl, baseUrl, "Success URL must not be the login URL");
            Assert.assertFalse(destination.getFragment() != null
                    && destination.getFragment().startsWith("/login"),
                    "Success URL must identify an authenticated destination");
            Allure.step("Sign in with the configured account", () -> loginPage.login(email, password, true));
            Allure.step("Verify the configured authenticated destination", () -> {
                loginPage.waitForAuthenticatedUrl(expectedUrl);
                Assert.assertEquals(driver.getCurrentUrl(), expectedUrl);
            });
        } catch (RuntimeException exception) {
            Assert.fail("Valid-login workflow could not complete", exception);
        }
    }
}