package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class Addprojectflow {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeClass
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();

        // 1) Login
        page.navigate("https://pms.bacet.org/");
        page.fill("input[name='email']", "admin@example.com");
        page.fill("input[name='password']", "123456789");
        page.locator("button:has-text('Login')").click();

        handleToast(); // dismiss any login toast
        page.waitForSelector("text=Dashboard");

        // 2) Click on the 'Projects' menu
        Locator projectsIcon = page.locator("svg[aria-label='Projects']");
        projectsIcon.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        projectsIcon.scrollIntoViewIfNeeded();
        projectsIcon.click();
    }

    private String handleToast() {
        Locator toast = page.locator(".Toastify__toast-body");
        if (toast.count() > 0) { // safer than isVisible
            toast.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
            String message = toast.textContent();

            Locator closeBtn = page.locator("button.Toastify__close-button");
            if (closeBtn.count() > 0 && closeBtn.isVisible()) closeBtn.click();

            page.waitForSelector(".Toastify__toast-body",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
            return message;
        }
        return "";
    }

    @Test(priority = 1)
    public void testPositive_AddProjectSuccessfully() {
        // Open Add Project modal
        Locator addButton = page.locator("button:has-text('Add New Project')");
        addButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        addButton.scrollIntoViewIfNeeded();
        addButton.click();

        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        modal.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));

        modal.locator("input[placeholder='Enter Project Title']").fill("Automation Test Project");

        Locator ownerInput = modal.locator("input[placeholder='Select owner']");
        ownerInput.click();

        Locator ownerOption = page.locator("li:has-text('Nithya R.')");
        ownerOption.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        ownerOption.click();

        modal.locator("input[placeholder='DD/MM/YYYY']").first().fill("07/10/2025");
        modal.locator("input[placeholder='DD/MM/YYYY']").nth(1).fill("15/10/2025");

        modal.locator("div.ql-editor").fill("This is a valid project description.");

        Locator addModalButton = modal.locator("button.add_btn:has-text('Add')");
        addModalButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        addModalButton.click();

        handleToast(); // wait and close success toast
    }

    @Test(priority = 2)
    public void testNegative_EmptyProjectTitle() {
        handleToast(); // dismiss leftover toast from previous test

        // Open Add Project modal
        Locator addButton1 = page.locator("button:has-text('Add New Project')");
        addButton1.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        addButton1.scrollIntoViewIfNeeded();
        addButton1.click();

       Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        //modal.waitFor(new Locator.WaitForOptions()
            //.setState(WaitForSelectorState.VISIBLE)
            //.setTimeout(10000));

        modal.locator("input[placeholder='Enter Project Title']").fill("");

        Locator ownerInput = modal.locator("input[placeholder='Select owner']");
        ownerInput.click();

        Locator ownerOption = page.locator("li:has-text('Nithya R.')");
        ownerOption.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        ownerOption.click();

        Locator addModalButton = modal.locator("button.add_btn:has-text('Add')");
        addModalButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        addModalButton.click();

        Locator error = modal.locator("text=The title field is required.");
        Assert.assertTrue(error.isVisible(), "Error message for empty title should be visible");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
