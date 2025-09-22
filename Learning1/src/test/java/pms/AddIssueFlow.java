package pms;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

import org.testng.Assert;
import org.testng.annotations.*;

		public class AddIssueFlow {

		    private static Browser browser;
		    private static Page page;

		    @BeforeClass
		    public static void setUp() {
		        // Initialize Playwright and launch the browser
		        Playwright playwright = Playwright.create();
		        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
		        page = browser.newPage();
		    }

		    private String handleToast1() {
		        Locator toast = page.locator(".Toastify__toast-body");
		        toast.waitFor(new Locator.WaitForOptions()
		                .setState(WaitForSelectorState.VISIBLE)
		                .setTimeout(5000));

		        String toastMsg = toast.textContent();

		        // If toast has a close button, click it
		        Locator closeBtn = page.locator("button.Toastify__close-button");
		        if (closeBtn.isVisible()) {
		            closeBtn.click();
		        }

		        // Wait for toast to disappear
		        page.waitForSelector(".Toastify__toast-body",
		                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED)
		        );

		        return toastMsg;
		    }

		    @Test(priority = 1)
		    public void loginAsUser() {
		        // Open the login page and log in as the user
		        page.navigate("https://pms.technotackle.in/");
		        page.fill("input[name='email']", "priyadharshini.s@technotackle.com");
		        page.fill("input[name='password']", "Priya@123");

		        // Correct login button selector
		        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

		        // Handle toast after login
		        String loginToast = handleToast1();
		        System.out.println("Login Toast: " + loginToast);
		        Assert.assertTrue(loginToast.toLowerCase().contains("success"),
		                "Expected login success toast, got: " + loginToast);

		        // Wait for dashboard after toast disappears
		        page.waitForSelector("text=Dashboard");
		    }
	
		    @Test(priority = 2)
		    public void navigateToTimesheetTab() {
		        // Click on the Timesheet tab
		        page.click("img[alt='Issue Icon']");
		    }
		    @Test(priority = 3)
		    public void openAddTimeLogModal() {
		        //  Locate 'ADD LOG HOURS' button by role + text
		        Locator addLogButton = page.getByRole(AriaRole.BUTTON,
		                new Page.GetByRoleOptions().setName("ADD ISSUE"));

		        // Wait until visible
		        addLogButton.waitFor(new Locator.WaitForOptions()
		                .setState(WaitForSelectorState.VISIBLE));

		        // Click safely
		        addLogButton.click();

		        // Wait for modal/dialog
		        Locator addLogModal = page.locator("div[role='dialog'].offcanvas.show");
		        addLogModal.waitFor(new Locator.WaitForOptions()
		                .setState(WaitForSelectorState.VISIBLE));
		    }
		    
		    @Test(priority = 4)
		    public void validateAddButtonWithoutProjectSelection() {
		        // Scope inside modal
		        Locator modal = page.locator("div[role='dialog'].offcanvas.show");

		        // Click Add button inside modal
		        Locator addButton = modal.getByRole(AriaRole.BUTTON,
		            new Locator.GetByRoleOptions().setName("Add").setExact(true));
		        addButton.click();

		        // Target only the actual error message, not the "*"
		        Locator errorLocator = modal.locator("span.errorMsg",
		            new Locator.LocatorOptions().setHasText("Project field is required"));

		        // Wait until visible
		        errorLocator.waitFor(new Locator.WaitForOptions()
		                .setState(WaitForSelectorState.VISIBLE)
		                .setTimeout(5000));

		        // Assert text
		        String errorMessage = errorLocator.textContent().trim();
		        System.out.println("Error message displayed: " + errorMessage);

		        Assert.assertEquals(errorMessage, "Project field is required",
		                "Validation message did not match expected text!");
		    }

		    @Test(priority = 5)
		    public void submitIssueWithMandatoryFields() {
		    	Locator modal = page.locator("div[role='dialog'].offcanvas.show");

		        // --- Step 1: Select Project ---
		        Locator projectInput = modal.getByRole(AriaRole.COMBOBOX).first();
		        projectInput.click();
		        projectInput.fill("TechnoTackle Projects");

		        Locator projectOption = page.getByRole(AriaRole.OPTION,
		                new Page.GetByRoleOptions().setName("TechnoTackle Projects"));
		        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		        projectOption.click();
		        System.out.println("Project selected: TechnoTackle Projects");

		        // Enter Issue Name
		        modal.locator("input[name='issue_name']").fill("Automation Test Issue - Mandatory");

		        // Select Assignee
		     // --- Select Assignee ---
		        Locator assigneeDropdown = modal.locator("div[role='combobox']#Assignee");
		        assigneeDropdown.click(); // Open dropdown

		        // Wait for options to appear
		        Locator assigneeOption = page.getByRole(AriaRole.OPTION, 
		            new Page.GetByRoleOptions().setName("Priyadharshini.S (YOU)"));

		        assigneeOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		        assigneeOption.click();

		        System.out.println("Assignee selected: Priyadharshini.S (YOU)");

		        // Click Add
		        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();

		        // Capture toast
		        String toastMsg = handleToast1();
		        System.out.println("Submit Issue (Mandatory only) Toast: " + toastMsg);

		        Assert.assertTrue(toastMsg.toLowerCase().contains("success"), 
		            "Expected success toast, got: " + toastMsg);
		    }

		    @Test(priority = 6)
		    public void submitIssueWithAllFields() {
		        // Re-open modal
		        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD ISSUE")).click();
		        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
		        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

		     // --- Step 1: Select Project ---
		        Locator projectInput = modal.getByRole(AriaRole.COMBOBOX).first();
		        projectInput.click();
		        projectInput.fill("TechnoTackle Projects");

		        Locator projectOption = page.getByRole(AriaRole.OPTION,
		                new Page.GetByRoleOptions().setName("TechnoTackle Projects"));
		        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		        projectOption.click();
		        System.out.println("Project selected: TechnoTackle Projects");

		        // Issue Name
		        modal.locator("input[name='issue_name']").fill("Automation Test Issue - All Fields");

		        // Description (not mandatory)
		        Locator descriptionField = modal.locator("div.ql-editor[contenteditable='true']");
		        descriptionField.click(); // focus into editor
		        descriptionField.type("This issue was created with all fields for testing in Playwright.");

		        // Followers dropdown (optional)
		     // Followers dropdown (Material UI Autocomplete)
		        Locator followersInput = modal.locator("input#followers");
		        followersInput.click();
		        followersInput.type("Priyadharshini"); // type name or keyword

		        // Wait for the suggestion list to appear and select first match
		        Locator option = page.locator("li.MuiAutocomplete-option").first();
		        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
		        option.click();


		        // Assignee (mandatory)
		        modal.locator("select[name='assignee']").selectOption(new SelectOption().setLabel("Priyadharshini"));

		        // Due Date (optional)
		        modal.locator("input[name='DueDate']").fill("2025-09-30");

		        // Priority dropdown (optional)
		        modal.locator("select[name='priority']").selectOption(new SelectOption().setLabel("High"));

		        // Severity dropdown (optional)
		        modal.locator("select[name='severity']").selectOption(new SelectOption().setLabel("Critical"));

		        // Click Add More
		        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add more")).click();

		        // Expect success toast
		        String toastMsg = handleToast1();
		        System.out.println("Submit Issue (All fields) Toast: " + toastMsg);

		        Assert.assertTrue(toastMsg.toLowerCase().contains("success"), 
		            "Expected success toast, got: " + toastMsg);

		        // After "Add more", modal should reopen
		        Assert.assertTrue(modal.isVisible(), "Modal did not reopen after Add more!");
		    }

		    
		    
		    
		    
		    
		    
		    
		    
}
