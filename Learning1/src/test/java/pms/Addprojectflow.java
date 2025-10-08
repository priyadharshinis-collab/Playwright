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
        if (toast.count() > 0) {
            try {
                toast.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
                String message = toast.textContent();

                Locator closeBtn = page.locator("button.Toastify__close-button");
                if (closeBtn.count() > 0 && closeBtn.isVisible()) closeBtn.click();

                page.waitForSelector(".Toastify__toast-body",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
                return message;
            } catch (PlaywrightException e) {
                // Ignore if toast not found in time
            }
        }
        return "";
    }

    @Test(priority = 1)
    public void testPositive_AddProjectSuccessfully() {
        // Small pause to let UI stabilize
        page.waitForTimeout(2000);
        handleToast();

        // Open Add Project modal (use :visible to avoid strict mode)
        Locator addButton = page.locator("button:has-text('Add New Project'):visible").first();
        addButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        addButton.scrollIntoViewIfNeeded();
        addButton.click();

        // Wait for modal
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
        handleToast(); // clear any previous success toast
        page.waitForTimeout(1500);

        // Open Add Project modal
        Locator addButton1 = page.locator("button:has-text('Add New Project'):visible").first();
        addButton1.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        addButton1.scrollIntoViewIfNeeded();
        addButton1.click();

        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        modal.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));

        // Leave title empty
        Locator titleField = modal.locator("input[placeholder='Enter Project Title']");
        titleField.fill("");

        Locator ownerInput = modal.locator("input[placeholder='Select owner']");
        ownerInput.click();

        Locator ownerOption = page.locator("li:has-text('Nithya R.')");
        ownerOption.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        ownerOption.click();

        // Click Add
        Locator addModalButton = modal.locator("button.add_btn:has-text('Add')");
        addModalButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        addModalButton.click();

        // Wait up to 5 seconds for the validation message to appear
        Locator error = modal.locator("text=Project Title is required");
        error.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));

        Assert.assertTrue(error.isVisible(), "Error message for empty title should be visible");
    }
    @Test(priority = 3)
    public void testNegative_EmptyOwner() {
        // 1️⃣ Close previous modal if open
        Locator oldModal = page.locator("div[role='dialog'].offcanvas.show");
        if (oldModal.count() > 0) {
            Locator cancelButton = oldModal.locator("button:has-text('Cancel')");
            cancelButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
            cancelButton.click();
        }

        // 2️⃣ Open Add Project modal again
        Locator addButton = page.locator("button:has-text('Add New Project'):visible").first();
        addButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
        addButton.scrollIntoViewIfNeeded();
        addButton.click();

        // 3️⃣ Locate the new modal (after reopening)
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        modal.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(10000));

        // 4️⃣ Fill only Project Title
        modal.locator("input[placeholder='Enter Project Title']").fill("Hi this is new project ");

        // 5️⃣ Do NOT select Owner to trigger validation
        Locator addModalButton = modal.locator("button.add_btn:has-text('Add')");
        addModalButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        addModalButton.click();

        // 6️⃣ Wait for the validation error
        Locator error = modal.locator("span.errorMsg:has-text('Owner field is required')");
        error.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(7000)); // slightly longer timeout

        // 7️⃣ Assert the error is visible
        Assert.assertTrue(error.isVisible(), "Owner field is required");
        System.out.println("Validation displayed correctly: " + error.textContent());
    }
    
    @Test(priority = 4)
    public void testNegative_InvalidDateOrder() {
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");

        // Select Owner
        Locator ownerInput = modal.locator("input[placeholder='Select owner']");
        ownerInput.click();

        Locator ownerOption = page.locator("li:has-text('Nithya R.')");
        ownerOption.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(5000));
        ownerOption.click();

        // Fill invalid dates
        Locator startDate = modal.locator("input[placeholder='DD/MM/YYYY']").first();
        Locator endDate = modal.locator("input[placeholder='DD/MM/YYYY']").nth(1);
        startDate.fill("15/10/2025");
        endDate.fill("07/10/2025");
        page.locator("body").click(); // trigger validation

        // Click Add
        Locator addButton = modal.locator("button.add_btn:has-text('Add')");
        addButton.click();

        // Handle Toastify message
        Locator toast = page.locator("div.Toastify__toast-body")
                             .filter(new Locator.FilterOptions()
                             .setHasText("The end date field must be a date after start date."));

        try {
            toast.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(7000));

            System.out.println("Validation displayed correctly: " + toast.textContent());
            Assert.assertTrue(toast.isVisible());
        } catch (PlaywrightException e) {
            Assert.fail("Error toast message not displayed — this is a BUG");
        }
    }

    
    @Test(priority = 5)
    public void filledAllFieldsAndCreatedProject() {
    	 Locator modal = page.locator("div[role='dialog'].offcanvas.show");
    	Locator endDate = modal.locator("input[placeholder='DD/MM/YYYY']").nth(1);
    	endDate.fill("10/10/2025");
    	
    	//description
    	modal.locator("div.ql-editor").fill("This is a valid project description.");
    	
    	// Select Add Users dropdown
    	Locator addUsers = modal.locator("input[placeholder='Enter Users Name']");
    	addUsers.click();

    	// Wait for dropdown options to appear
    	Locator userOption = page.locator("li:has-text('admin a (YOU)')");

    	// Wait up to 5 seconds, then check visibility before clicking
    	try {
    	    userOption.waitFor(new Locator.WaitForOptions()
    	        .setState(WaitForSelectorState.VISIBLE)
    	        .setTimeout(5000));

    	    userOption.first().click();
    	    System.out.println("User selected: admin a (YOU)");

    	} catch (PlaywrightException e) {
    	    // If the dropdown didn't open or user already added
    	    String value = addUsers.inputValue();
    	    System.out.println("User field pre-filled or dropdown not shown. Current value: " + value);
    	}
    	 modal.locator("input[placeholder='Enter Users Name']");
    	addUsers.click();
        
        
    	//select add client dropdown
    	
    	
    	Locator addClient = modal.locator("input[placeholder='Enter Client Name']");
    	addClient.click();
    	addClient.fill("abishek"); // type partial name to trigger list

    	// wait for dropdown to load
    	Locator clientOption = page.locator("//li[contains(.,'abishek')]");
    	clientOption.waitFor(new Locator.WaitForOptions()
    	    .setState(WaitForSelectorState.VISIBLE)
    	    .setTimeout(10000));

    	clientOption.click();
    	 modal.locator("input[placeholder='Enter Client Name']");
     	addClient.click();

        
        //select  budget type
     // Open the dropdown
        Locator budgetDropdown = page.locator("div[role='combobox'][id='budgetType']");
        budgetDropdown.click();

        // Select the option
        Locator option = page.locator("li[role='option'][data-value='TIME_AND_MATERIAL']");
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        option.click();

    	
    	
    	
    }
    
    
    
    
    
    
    
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
