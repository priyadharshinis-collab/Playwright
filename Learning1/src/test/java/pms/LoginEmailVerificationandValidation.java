package pms;

import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.nio.file.Paths;

public class LoginEmailVerificationandValidation {

    private Browser browser;
    private Page page;

    // ================== Setup & Teardown ==================
    @BeforeClass
    public void setUp() {
        browser = LoginUtil.launchBrowser();
        page = browser.newPage();
        page.navigate("https://pms.technotackle.in/login");
    }

    @AfterClass
    public void tearDown() {
        LoginUtil.closeBrowser(browser);
    }

    @AfterMethod
    public void takeScreenshotOnFailure(ITestResult result) {
        if (!result.isSuccess()) {
            String fileName = "screenshot_" + result.getName() + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fileName)));
            System.out.println("📸 Screenshot captured for failed test: " + fileName);
        }
    }

    // ================== Helper Methods ==================
    private Locator emailField() {
        return page.locator("input[type='email']");
    }

    private Locator passwordField() {
        return page.locator("input[type='password']");
    }

    private Locator submitButton() {
        return page.locator("button[type='submit']");
    }

    private void fillLogin(String email, String password) {
        emailField().fill(email);
        passwordField().fill(password);
        submitButton().click();
    }

    private String getBrowserValidationMessage(Locator field) {
        return (String) page.evaluate("el => el.validationMessage", field.elementHandle());
    }

    private String getToastMessage() {
        Locator toast = page.locator(".Toastify__toast-body").first();
        toast.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        return toast.innerText();
    }

    // ================== Tests ==================
    @Test(priority = 1)
    public void verifyEmailFieldProperties() {
        Locator email = emailField();

        Assert.assertTrue(email.isVisible(), "Email field should be visible");

        String placeholder = email.getAttribute("placeholder");
        Assert.assertEquals(placeholder, "example@technotackle.com", "Placeholder mismatch");

        String maxLength = email.getAttribute("maxlength");
        if (maxLength != null) {
            Assert.assertEquals(maxLength, "50", "Max length should be 50");
        } else {
            System.out.println("⚠️ No maxlength attribute found, skipping check.");
        }

        String type = email.getAttribute("type");
        Assert.assertEquals(type, "email", "Type should be email");
    }

    @Test(priority = 2)
    public void validateEmptyEmail() {
        fillLogin("", "Password123");

        String validationMsg = getBrowserValidationMessage(emailField());
        System.out.println("Browser validation (empty email): " + validationMsg);
        Assert.assertTrue(validationMsg.toLowerCase().contains("please"), "Expected required field message");
    }

    @Test(priority = 3)
    public void validateInvalidEmailFormat() {
        fillLogin("abc@", "Password123");

        String validationMsg = getBrowserValidationMessage(emailField());
        System.out.println("Browser validation (invalid email): " + validationMsg);
        Assert.assertTrue(validationMsg.toLowerCase().contains("please"), "Expected invalid email format message");
    }

    @Test(priority = 4)
    public void validateEmptyPassword() {
        fillLogin("invalid@technotackle.com", "");

        String errorMsg = getToastMessage();
        System.out.println("Toast error message: " + errorMsg);
        Assert.assertTrue(errorMsg.toLowerCase().contains("password"), "Expected password error");
    }

    @Test(priority = 5)
    public void validateInvalidCredentials() {
        fillLogin("invalid@technotackle.com", "WrongPass123");

        String errorMsg = getToastMessage();
        System.out.println("Error for invalid credentials: " + errorMsg);
        Assert.assertTrue(errorMsg.toLowerCase().contains("user not found"), "Expected 'User not found' error");
    }

    @Test(priority = 6)
    public void validateCorrectEmail() {
        fillLogin("priyadharshini.s@technotackle.com", "Priya@123");

        page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(10000));
        System.out.println("After login, URL is: " + page.url());

        Assert.assertTrue(page.url().contains("/dashboard"), "User should land on dashboard after login");
    }
}
