package playwrightLearning;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;

import org.testng.Assert;
import org.testng.annotations.*;

public class Timesheet {

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
        page.click("img[alt='Timesheet Icon']");
    }

    @Test(priority = 3)
    public void openAddTimeLogModal() {
        //  Locate 'ADD LOG HOURS' button by role + text
        Locator addLogButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("ADD LOG HOURS"));

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
    public void handleTasksIssuesOrGeneralLog() {
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");

        // --- Select Project ---
        Locator projectInput = modal.getByRole(AriaRole.COMBOBOX).first();
        projectInput.click();
        projectInput.fill("TechnoTackle Projects");

        // Wait and click matching option
        Locator projectOption = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName("TechnoTackle Projects"));
        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        projectOption.click();

        // --- Step 1: Try Tasks Dropdown ---
        Locator taskDropdown = modal.getByPlaceholder("Select task or issue");
        taskDropdown.click();

        Locator taskOptions = page.locator("div[role='option']");
        int taskCount = taskOptions.count();

        if (taskCount > 0) {
            taskOptions.first().click();
            System.out.println("Task selected successfully.");
            return;
        } else if (modal.locator("span.errorMsg:has-text('No tasks available')").isVisible()) {
            System.out.println("No tasks available for this project.");
        }

        // --- Step 2: Try Issues Tab ---
        Locator issueTab = modal.getByRole(AriaRole.TAB,
                new Locator.GetByRoleOptions().setName("Issues"));
        issueTab.click();

        Locator issueDropdown = modal.getByPlaceholder("Select task or issue");
        issueDropdown.click();

        Locator issueOptions = page.locator("div[role='option']");
        int issueCount = issueOptions.count();

        if (issueCount > 0) {
            issueOptions.first().click();
            System.out.println("Issue selected successfully.");
            return;
        } else if (modal.locator("span.errorMsg:has-text('No issues available')").isVisible()) {
            System.out.println("No issues available for this project.");
        }

        // --- Step 3: Enter General Log ---
        Locator generalCheckbox = modal.getByText("General task");
        generalCheckbox.click();

        Locator generalLogInput = modal.getByPlaceholder("Enter task name");
        generalLogInput.fill("General Log Task - Auto Entry");

        // Validate the input is entered
        String enteredValue = generalLogInput.inputValue();
        Assert.assertEquals(enteredValue, "General Log Task - Auto Entry",
                "General log task name was not entered properly!");

        System.out.println("Entered general log as fallback.");
    }


    @Test(priority = 6)
    public void validateTaskOrIssueSelection() {
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");

        // 1. Verify default tab is "Tasks"
        Locator tasksTab = modal.getByRole(AriaRole.TAB);
        Assert.assertTrue(tasksTab.getAttribute("class").contains("active"), "Tasks tab should be selected by default");

        // 2. Check if tasks exist
        Locator taskDropdown = modal.locator("div[role='combobox']");
        taskDropdown.click();
        Locator taskOptions = page.getByRole(AriaRole.OPTION);

        if (taskOptions.count() == 0) {
            // Verify error message
            Locator noTasksMsg = modal.locator("span.errorMsg", 
                new Locator.LocatorOptions().setHasText("No tasks available for this user"));
            noTasksMsg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            Assert.assertTrue(noTasksMsg.isVisible(), "Expected 'No tasks available for this user' message");
        } else {
            // Select first available task
            taskOptions.nth(0).click();
        }

        // 3. Switch to Issues tab
        modal.getByRole(AriaRole.TAB).click();

        // 4. Check if issues exist
        Locator issueDropdown = modal.locator("div[role='combobox']");
        issueDropdown.click();
        Locator issueOptions = page.getByRole(AriaRole.OPTION);

        if (issueOptions.count() == 0) {
            Locator noIssuesMsg = modal.locator("span.errorMsg", 
                new Locator.LocatorOptions().setHasText("No issues available for this user"));
            noIssuesMsg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            Assert.assertTrue(noIssuesMsg.isVisible(), "Expected 'No issues available for this user' message");
        } else {
            issueOptions.nth(0).click();
        }

        // 5. If neither available → Enter General Log
        Locator generalLogLink = modal.locator("a", new Locator.LocatorOptions().setHasText("enter general log "));
        if (generalLogLink.isVisible()) {
            generalLogLink.click();
            Locator generalLogTextbox = modal.locator("textarea[placeholder='Enter general log']");
            Assert.assertTrue(generalLogTextbox.isVisible(), "General log textbox should be visible");
            generalLogTextbox.fill("Testing manual log entry");
        }
    }

    
    
}
