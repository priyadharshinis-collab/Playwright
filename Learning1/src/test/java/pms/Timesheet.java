package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;

import org.testng.Assert;
import org.testng.annotations.*;

public class Timesheet {

    private static Browser browser;
    private static Page page;

    @BeforeClass
    public void setUp() {
        browser = LoginUtil.launchBrowser();
        page = LoginUtil.loginUser(browser, "priyadharshini.s@technotackle.com", "Priya@123");
    }

    @AfterClass
    public void tearDown() {
        LoginUtil.closeBrowser(browser);
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
    @Parameters({"flowType"})   // pass "task" or "issue" or "general"
    public void handleTimesheetEntry(@Optional("general") String flowType) {
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

        // --- Step 2: Flow Handling ---
        if (flowType.equalsIgnoreCase("task")) {
            Locator taskDropdown = modal.getByPlaceholder("Select task or issue");
            taskDropdown.click();
            taskDropdown.fill("General task");

            Locator taskOption = page.locator("li[role='option']").filter(
                    new Locator.FilterOptions().setHasText("General task"));

            try {
                taskOption.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                taskOption.first().click();
                System.out.println("Task 'General task' selected successfully (via click).");
            } catch (PlaywrightException e) {
                page.keyboard().press("ArrowDown");
                page.keyboard().press("Enter");
                System.out.println("Task 'General task' selected successfully (via keyboard).");
            }

        } else if (flowType.equalsIgnoreCase("issue")) {
            Locator issueTab = modal.getByText("Issues", new Locator.GetByTextOptions().setExact(true));
            issueTab.click();

            Locator issueDropdown = modal.getByPlaceholder("Select task or issue");
            issueDropdown.click();
            issueDropdown.fill("General issue");

            Locator issueOption = page.locator("li[role='option']").filter(
                    new Locator.FilterOptions().setHasText("General issue"));

            try {
                issueOption.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                issueOption.first().click();
                System.out.println("Issue selected successfully (via click).");
            } catch (PlaywrightException e) {
                page.keyboard().press("ArrowDown");
                page.keyboard().press("Enter");
                System.out.println("Issue selected successfully (via keyboard).");
            }

        } else if (flowType.equalsIgnoreCase("general")) {
            // Step 1: Wait for the modal to be attached and visible
            modal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(10000));
            modal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

            // Step 2: Locate the "enter general log" element specifically by text
            Locator enterGeneralLog = modal.locator("p.enterHourse")
                                           .filter(new Locator.FilterOptions().setHasText("enter general log"));
            
            // Step 3: Wait for the element to be visible
            enterGeneralLog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

            // Step 4: Scroll into view (handles offscreen or animation issues)
            enterGeneralLog.scrollIntoViewIfNeeded();

            // Step 5: Retry clicking in case of transient issues or modal auto-close
            int attempts = 0;
            while (attempts < 3) {
                try {
                    enterGeneralLog.click();
                    break; // click succeeded
                } catch (PlaywrightException e) {
                    System.out.println("Retry clicking 'Enter General Log'...");
                    page.waitForTimeout(500); // wait 0.5s and retry
                    attempts++;
                }
            }

            // Step 6: Fill the general log input
            Locator generalLogInput = modal.getByPlaceholder("Enter general log");
            generalLogInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
            generalLogInput.fill("Testing task");
            
            // Step 7: Assert the input value
            Assert.assertEquals(generalLogInput.inputValue().trim(), "Testing task");
            System.out.println("General log entered manually.");
        }


        

        

        // --- Step 3: Date (already today, so skip) ---

        // --- Step 4: Owner (already default Priyadharshini.S) ---

     // --- Step 5: Daily Log Time ---

     // FROM HOUR
     Locator fromHour = modal.locator("input[name='startHour']");
     fromHour.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
     fromHour.click();
     fromHour.fill("09");
     page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("09")).click();
     System.out.println("From Hour selected: 09");

     // FROM MINUTE
     Locator fromMinute = modal.locator("input[name='startMinute']");
     fromMinute.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
     fromMinute.click();
     fromMinute.fill("00");
     page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("00")).click();
     System.out.println("From Minute selected: 00");

     // FROM AM/PM
     Locator fromAmPm = modal.locator("input[name='startAmPm']");
     fromAmPm.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
     fromAmPm.click();
     page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("AM")).click();
     System.out.println("From AM/PM selected: AM");

     // TO HOUR
     Locator toHour = modal.locator("input[name='endHour']");
     toHour.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
     toHour.click();
     toHour.fill("10");
     page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("10")).click();
     System.out.println("To Hour selected: 10");

     // T


        // --- Step 6: Billing Type (default Billable) ---

        // --- Step 7: Notes (mandatory) ---
        Locator notesInput = modal.locator("div.ql-editor"); 
        notesInput.click();
        notesInput.fill("Automated entry notes");
        System.out.println("Notes entered.");

     // --- Step 8: Add Button ---
        Locator addButton = modal.locator("button.add_btn.me-3");
        addButton.click();
        System.out.println("Clicked Add button successfully.");


        // --- Step 9: Verify Success Message ---
        Locator successToast = page.locator("div.toast-success:has-text('successfully')");
        successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        Assert.assertTrue(successToast.isVisible(), "Success message not visible!");
        System.out.println("Timesheet entry added successfully.");
    }

}
