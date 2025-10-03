package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.*;

public class Addtaskflow{

    private Browser browser;
    private Page page;

    // ================== Setup & Teardown ==================
    @BeforeClass
    public void setUp() {
        browser = LoginUtil.launchBrowser();
        page = LoginUtil.loginUser(browser,
                "admin@example.com",
                "123456789");
    }

    @AfterClass
    public void tearDown() {
        LoginUtil.closeBrowser(browser);
    }

    // ================== Helper Methods ==================
    private String handleToast() {
        Locator toast = page.locator(".Toastify__toast-body");
        toast.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        String message = toast.textContent();

        Locator closeBtn = page.locator("button.Toastify__close-button");
        if (closeBtn.isVisible()) closeBtn.click();

        page.waitForSelector(".Toastify__toast-body",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
        return message;
    }

    private Locator waitForModal() {
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        modal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        return modal;
    }

    private void selectDropdownOption(Locator dropdown, String optionName) {
        dropdown.scrollIntoViewIfNeeded();
        dropdown.click();

        Locator option = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(optionName));
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();
    }

    private void selectProject(Locator modal, String projectName) {
        Locator projectDropdown = modal.getByRole(AriaRole.COMBOBOX).first();
        projectDropdown.click();
        projectDropdown.fill(projectName);

        Locator projectOption = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(projectName));
        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        projectOption.click();
        System.out.println("Project selected: " + projectName);
    }

    private void selectTaskList(Locator modal, String taskListName) {
        Locator taskListDropdown = modal.getByRole(AriaRole.COMBOBOX).nth(1); // adjust index if needed
        selectDropdownOption(taskListDropdown, taskListName);
        System.out.println("Task List selected: " + taskListName);
    }

    private void createTaskListIfNotExists(String project, String taskListName) {
        // Cancel modal
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Cancel")).click();

        // Open Add Task List
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Options")).click();
        page.getByText("Add Task List").click();

        Locator modal = waitForModal();
        selectProject(modal, project);

        Locator nameInput = modal.locator("input[name='taskListName']");
        nameInput.fill(taskListName);

        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();

        String toastMsg = handleToast();
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"),
                "Task list should be created successfully");
        System.out.println("Task List created: " + taskListName);
    }

    // ================== Tests ==================
    @Test(priority = 1)
    public void openTaskMenuTest() {
        page.click("img[alt='Task Icon']");
        System.out.println("Navigated to Task tab.");
    }

    @Test(priority = 2)
    public void validateAddButtonWithoutProjectTest() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Task")).click();

        Locator modal = waitForModal();
        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();

        Locator errorLocator = modal.locator("span.errorMsg",
                new Locator.LocatorOptions().setHasText("Project field is required"));
        errorLocator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        String errorMsg = errorLocator.textContent().trim();
        Assert.assertEquals(errorMsg, "Project field is required");
        System.out.println("Validation: " + errorMsg);
    }

    @Test(priority = 3)
    public void addTaskWithMandatoryFieldsTest() {
        
    	
    	
    	
    	
    	
    	
        Locator modal = waitForModal();
        selectProject(modal, "24-14.1 - TDISDI Project");

        String taskListName = "AutomationList1";
        createTaskListIfNotExists("24-14.1 - TDISDI Project", taskListName);

        // Re-open Add Task
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Task")).click();
        modal = waitForModal();
        selectProject(modal, "24-14.1 - TDISDI Project");

        modal.locator("input[name='task_title']").fill("Automation Task - Mandatory");
        selectTaskList(modal, taskListName);

        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();

        String toastMsg = handleToast();
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"));
        System.out.println("Task created (Mandatory only).");
    }
    
    @Test(priority = 4)
    public void addTaskWithAllFieldsTest() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Task")).click();

        Locator modal = waitForModal();
        selectProject(modal, "24-14.1 - TDISDI Project");

        modal.locator("input[name='task_title']").fill("Automation Task - All Fields");
        modal.locator("textarea[name='description']").fill("Description for automation task.");

        selectTaskList(modal, "AutomationList1");

        // Owner is default, skip selection

        // Dates
        modal.locator("input[name='start_date']").fill("01/10/2025");
        modal.locator("input[name='end_date']").fill("05/10/2025");

        // Estimated hours
        modal.locator("input[name='estimated_hours']").fill("12");

        // Priority
        Locator priorityDropdown = modal.getByText("Priority").locator("..").locator("div[role='combobox']");
        selectDropdownOption(priorityDropdown, "High");

        // Billing method (default Billable, no action)

        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add more").setExact(true)).click();

        String toastMsg = handleToast();
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"));
        System.out.println("Task created (All fields).");
    }
}
