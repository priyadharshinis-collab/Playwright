package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.*;

public class AddIssueFlow {

    private Browser browser;
    private Page page;

    // ================== Setup & Teardown ==================
    @BeforeClass
    public void setUp() {
        browser = LoginUtil.launchBrowser();
        page = LoginUtil.loginUser(browser, "priyadharshini.s@technotackle.com", "Priya@123");
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

    private void selectDropdownOption(Locator dropdown, String optionName) {
        dropdown.scrollIntoViewIfNeeded();
        dropdown.click();

        Locator option = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionName));
        option.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));
        option.click();
    }

    private Locator waitForModal() {
        Locator modal = page.locator("div[role='dialog'].offcanvas.show");
        modal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        return modal;
    }

    private void selectProject(Locator modal, String projectName) {
        Locator projectInput = modal.getByRole(AriaRole.COMBOBOX).first();
        projectInput.click();
        projectInput.fill(projectName);

        Locator projectOption = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(projectName));
        projectOption.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        projectOption.click();

        System.out.println("Project selected: " + projectName);
    }

    private void selectAssignee(Locator modal, String assigneeName) {
        Locator assigneeDropdown = modal.locator("div[role='combobox']#Assignee");
        selectDropdownOption(assigneeDropdown, assigneeName);
        System.out.println("Assignee selected: " + assigneeName);
    }

    private void selectPriorityAndSeverity(Locator modal, String priority, String severity) {
        Locator priorityDropdown = modal.locator("text=Priority").locator("..").locator("div[role='combobox']");
        selectDropdownOption(priorityDropdown, priority);
        System.out.println("Priority selected: " + priority);

        Locator severityDropdown = modal.locator("div#Severity[role='combobox']");
        selectDropdownOption(severityDropdown, severity);
        System.out.println("Severity selected: " + severity);
    }

    private void fillDueDate(Locator modal, String dueDate) {
        Locator dueDateInput = modal.locator("input[name='DueDate']");
        dueDateInput.click();
        dueDateInput.press("Control+A");
        dueDateInput.press("Delete");
        dueDateInput.type(dueDate);
        System.out.println("Due Date entered: " + dueDate);
    }

    // ================== Tests ==================
    @Test(priority = 1)
    public void loginAsUserTest() {
        page.navigate("https://pms.technotackle.in/");
        page.fill("input[name='email']", "priyadharshini.s@technotackle.com");
        page.fill("input[name='password']", "Priya@123");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

        String toastMsg = handleToast();
        System.out.println("Login Toast: " + toastMsg);
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"), "Expected login success toast");
        page.waitForSelector("text=Dashboard");
    }

    @Test(priority = 2)
    public void navigateToIssueTabTest() {
        page.click("img[alt='Issue Icon']");
        System.out.println("Navigated to Issue tab.");
    }

    @Test(priority = 3)
    public void openAddIssueModalTest() {
        Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD ISSUE"));
        addButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        addButton.click();
        waitForModal();
        System.out.println("Add Issue modal opened.");
    }

    @Test(priority = 4)
    public void validateAddButtonWithoutProjectTest() {
        Locator modal = waitForModal();
        Locator addButton = modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add").setExact(true));
        addButton.click();

        Locator errorLocator = modal.locator("span.errorMsg", new Locator.LocatorOptions().setHasText("Project field is required"));
        errorLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        String errorMsg = errorLocator.textContent().trim();
        System.out.println("Error message displayed: " + errorMsg);
        Assert.assertEquals(errorMsg, "Project field is required");
    }

    @Test(priority = 5)
    public void submitIssueWithMandatoryFieldsTest() {
        Locator modal = waitForModal();
        selectProject(modal, "TechnoTackle Projects");

        modal.locator("input[name='issue_name']").fill("Automation Test Issue - Mandatory");
        selectAssignee(modal, "Priyadharshini.S (YOU)");

        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();

        String toastMsg = handleToast();
        System.out.println("Submit Issue (Mandatory) Toast: " + toastMsg);
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"));
    }

    @Test(priority = 6)
    public void submitIssueWithAllFieldsTest() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD ISSUE")).click();
        Locator modal = waitForModal();

        selectProject(modal, "TechnoTackle Projects");
        modal.locator("input[name='issue_name']").fill("Automation Test Issue - All Fields");

        // Optional description
        Locator desc = modal.locator("div.ql-editor[contenteditable='true']");
        desc.click();
        desc.type("This issue was created with all fields for testing in Playwright.");

        // Followers
        Locator followers = modal.locator("input#followers");
        followers.click();
        followers.type("Priyadharshini.S (YOU)", new Locator.TypeOptions().setDelay(100));
        Locator followerOption = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Priyadharshini.S (YOU)"));
        followerOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        followerOption.click();
        followers.click(); // close dropdown
        System.out.println("Follower added.");

        selectAssignee(modal, "Priyadharshini.S (YOU)");
        fillDueDate(modal, "30/09/2025");

        selectPriorityAndSeverity(modal, "High", "Critical");

        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add more")).click();
        String toastMsg = handleToast();
        System.out.println("Submit Issue (All fields) Toast: " + toastMsg);
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"));
    }
}

