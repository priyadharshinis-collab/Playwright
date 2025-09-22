package pms;

import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

public class LoginEmailVerificationandValidation {
    Playwright playwright;
    Browser browser;
    Page page;

    @BeforeClass
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        page.navigate("https://pms.technotackle.in/login");
    }

    @Test(priority = 1)
    public void verifyEmailFieldProperties() {
        Locator emailField = page.locator("input[type='email']");

        // Verify visibility
        Assert.assertTrue(emailField.isVisible(), "Email field should be visible");

        // Verify placeholder
        String placeholder = emailField.getAttribute("placeholder");
        Assert.assertEquals(placeholder, "example@technotackle.com", "Placeholder mismatch");

        // Verify maxlength
        String maxLength = emailField.getAttribute("maxlength");
        if (maxLength != null) {
            Assert.assertEquals(maxLength, "50", "Max length should be 50");
        } else {
            System.out.println("⚠️ No maxlength attribute found on email field, skipping check.");
        }

        // Verify type
        String type = emailField.getAttribute("type");
        Assert.assertEquals(type, "email", "Type is in email Format");
    }

    @Test(priority = 2)
    public void validateEmptyEmail() {
        Locator emailField = page.locator("input[type='email']");
        emailField.fill(""); // blank
        page.locator("input[type='password']").fill("Password123");
        page.locator("button[type='submit']").click();

        // browser native validation
        String validationMsg = (String) page.evaluate("el => el.validationMessage", emailField.elementHandle());
        System.out.println("Browser validation (empty email): " + validationMsg);

        Assert.assertTrue(validationMsg.toLowerCase().contains("please"),
                "Expected browser required message, but got: " + validationMsg);
    }

    @Test(priority = 3)
    public void validateInvalidEmailFormat() {
        Locator emailField = page.locator("input[type='email']");
        emailField.fill("abc@");
        page.locator("input[type='password']").fill("Password123");
        page.locator("button[type='submit']").click();

        String validationMsg = (String) page.evaluate("el => el.validationMessage", emailField.elementHandle());
        System.out.println("Browser validation (invalid email): " + validationMsg);

        Assert.assertTrue(validationMsg.toLowerCase().contains("please"),
                "Expected invalid email format message, but got: " + validationMsg);
    }

    @Test(priority = 4)
    public void validateEmptyPassword() {
        // Enter only email
        page.locator("input[type='email']").fill("invalid@technotackle.com");
        page.locator("input[type='password']").fill("");
        page.locator("button[type='submit']").click();

        // Wait for toast to appear (max 5s)
        page.waitForSelector(".Toastify__toast-body", 
            new Page.WaitForSelectorOptions().setTimeout(5000));

        // Now fetch the text
        String errorMsg = page.locator(".Toastify__toast-body").first().innerText();
        System.out.println("Toast error message: " + errorMsg);

        Assert.assertTrue(errorMsg.toLowerCase().contains("password"),
                "Expected password error, but got: " + errorMsg);
    }

    @Test(priority = 5)
    public void validateInvalidCredentials() {
        page.locator("input[type='email']").fill("invalid@technotackle.com");
        page.locator("input[type='password']").fill("WrongPass123");
        page.locator("button[type='submit']").click();

        // Wait for toast error
        Locator errorLocator = page.locator(".Toastify__toast-body").first();
        errorLocator.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        String errorMsg = errorLocator.innerText();
        System.out.println("Error for invalid credentials: " + errorMsg);

        Assert.assertTrue(errorMsg.toLowerCase().contains("user not found"),
                "Expected 'User not found' error, but got: " + errorMsg);
    }

    @Test(priority = 6)
    public void validateCorrectEmail() {
        page.locator("input[type='email']").fill("priyadharshini.s@technotackle.com");
        page.locator("input[type='password']").fill("Priya@123");
        page.locator("button[type='submit']").click();

        page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(10000));
        System.out.println("After login, URL is: " + page.url());

        Assert.assertTrue(page.url().contains("/dashboard"),
                "User should land on dashboard after successful login");
    }

    @AfterMethod
    public void takeScreenshotOnFailure(ITestResult result) {
        if (!result.isSuccess()) {
            String fileName = "screenshot_" + result.getName() + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get(fileName)));
            System.out.println("📸 Screenshot captured for failed test: " + fileName);
        }
    }

    @AfterClass
    public void tearDown() {
        browser.close();
        playwright.close();
    }
}
