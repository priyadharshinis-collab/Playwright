package playwrightLearning;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Addtimesheetflow {

    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeClass
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterClass
    public void teardown() {
        if (playwright != null) playwright.close();
    }

    // --- Utility Methods ---
    private int convertHHMMToMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private int getTotalMinutes() {
        String totalText = page.locator("xpath=//div[contains(., 'Total Log Hours')]").textContent();
        Pattern p = Pattern.compile("(\\d{1,2}:\\d{2})");
        Matcher m = p.matcher(totalText);
        if (m.find()) return convertHHMMToMinutes(m.group(1));
        return 0;
    }

    /**
     * Handle toast notifications:
     * Waits for toast, captures text, and closes it if close button is present.
     */
    private String handleToast() {
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

    // --- Test Cases ---
    @Test(priority = 1)
    public void addLogHours_successFlow() {
        // --- Login ---
        page.navigate("https://pms.technotackle.in/");
        page.fill("input[name='email']", "priyadharshini.s@technotackle.com");
        page.fill("input[name='password']", "Priya@123");
        page.click("button:has-text('Login')");

        // ✅ Handle toast after login
        String loginToast = handleToast();
        System.out.println("Login Toast: " + loginToast);
        Assert.assertTrue(loginToast.toLowerCase().contains("success"),
                "Expected login success toast, got: " + loginToast);

        // ✅ Wait for dashboard after toast disappears
        page.waitForSelector("text=Dashboard");

        // --- Navigate Timesheet ---
        page.click("img[alt='Timesheet Icon']");
        //page.waitForSelector("text=Total Log Hours");

        int beforeMinutes = getTotalMinutes();

     // Wait for Add Log Hours modal to appear and stabilize
        Locator addLogModal = page.locator("div[role='dialog'].offcanvas.show");
        addLogModal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        addLogModal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        // --- PROJECT DROPDOWN ---
        // Locate the dropdown input
        Locator projectDropdown = page.locator("#project_id");

        // Locate the clickable dropdown icon (adornment)
        Locator projectIcon = page.locator("#project_id + div.MuiAutocomplete-endAdornment");

        // Click the icon instead of input
        projectIcon.click();

        // Wait for the options list to appear
        Locator projectOptionsList = page.locator("ul[role='listbox']");
        projectOptionsList.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        // Scroll to and click the desired project
        Locator projectOption = projectOptionsList.locator("text=TechnoTackle Projects");
        projectOption.scrollIntoViewIfNeeded();
        projectOption.click();

        // --- TASK DROPDOWN ---
        // Locate the task dropdown input and icon
        Locator taskIcon = page.locator("#task_id + div.MuiAutocomplete-endAdornment");
        taskIcon.click();

        // Wait for the options list
        Locator taskOptionsList = page.locator("ul[role='listbox']");
        taskOptionsList.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        // Scroll to and click the desired task
        Locator taskOption = taskOptionsList.locator("text=General task");
        taskOption.scrollIntoViewIfNeeded();
        taskOption.click();


        // From: 01:00 AM
        page.selectOption("select[name='startHour']", "01");
        page.selectOption("select[name='startMinute']", "00");
        page.selectOption("select[name='startAmPm']", "AM");

        // To: 03:00 AM
        page.selectOption("select[name='endHour']", "03");
        page.selectOption("select[name='endMinutes']", "00");
        page.selectOption("select[name='endAmPm']", "AM");

        page.click("text=BILLABLE");
        page.fill("textarea[placeholder*='Notes']", "Automation Test Notes");

        // --- Submit ---
        page.click("button:has-text('Add')");

        // --- Verify Success Message via toast ---
        String toastMsg = handleToast();
        Assert.assertTrue(toastMsg.toLowerCase().contains("success"),
                "Expected success toast, got: " + toastMsg);

        // --- Verify Entry Present ---
        boolean entryVisible = page.locator("xpath=//tr[.//text()[contains(.,'15/09/2025')]]")
                .locator("text=02:00").isVisible();
        Assert.assertTrue(entryVisible, "Newly added time entry should be visible in timesheet list.");

        // --- Verify Total Hours Updated ---
        int afterMinutes = getTotalMinutes();
        Assert.assertEquals(afterMinutes, beforeMinutes + 120, "Total hours should increase by 120 minutes.");
    }

    @Test(priority = 2)
    public void addLogHours_validationErrors() {
        // --- Navigate Timesheet ---
        page.click("img[alt='Timesheet Icon']");
        page.waitForSelector("text=Total Log Hours");

        // --- Open Add Log Hours Modal ---
        page.click("button:has-text('ADD LOG HOURS')");
        page.waitForSelector("text=Add Time Log");

        // --- Submit Without Filling ---
        page.click("button:has-text('Add')");

        // --- Verify Error Messages ---
        boolean projectError = page.locator("text=Project field is required").isVisible();
        boolean dailyLogError = page.locator("text=Task or issue name is required").isVisible();
        boolean notesError = page.locator("text=Notes field is required").isVisible();

        Assert.assertTrue(projectError, "Expected 'Project field is required' error");
        Assert.assertTrue(dailyLogError, "Expected 'Task or issue name is required' error");
        Assert.assertTrue(notesError, "Expected 'Notes field is required' error");
    }
}
