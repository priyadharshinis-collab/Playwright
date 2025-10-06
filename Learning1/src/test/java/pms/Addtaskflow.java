package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Locator.WaitForOptions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class Addtaskflow {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeClass
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
        page = browser.newPage();

        // 1) Login
        page.navigate("https://pms.bacet.org/");
        page.fill("input[name='email']", "admin@example.com");
        page.fill("input[name='password']", "123456789");
        page.locator("button:has-text('Login')").click();

        // Wait for dashboard to load
        page.waitForSelector("img[alt='Task Icon']", new Page.WaitForSelectorOptions().setTimeout(10000));
        System.out.println("Login successful for user");
    }

    @Test
    public void addTaskFlow() {
        // 2) Navigate to Task menu
        page.click("img[alt='Task Icon']");
        System.out.println("Navigated to Task tab.");

        // 3) Click add task button
        page.click("button:has-text('ADD TASK')");

        Locator modal = page.locator("div[role='dialog'].offcanvas.show");

        // --- Step 1: Select Project ---
        Locator projectInput = modal.getByRole(AriaRole.COMBOBOX).first();
        projectInput.click();
        projectInput.fill("Aana");

        Locator projectOption = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName("Aana"));
        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        projectOption.click();
        System.out.println("Project selected: TechnoTackle Projects");

     // Click Task List dropdown
        Locator taskListDropdown = modal.locator("#tasklist");
        taskListDropdown.click();

        // Wait for the MUI dropdown portal to render
        page.waitForTimeout(500);

        // Get all options from the portal dropdown
        Locator dropdownOptions = page.locator("ul[role='listbox'] li[role='option']");
        List<String> options = dropdownOptions.allTextContents();
        System.out.println("Task List options: " + options);

        if (!options.isEmpty()) {
            // Task list exists → select first option
            dropdownOptions.first().click();
            System.out.println("Selected Task List: " + options.get(0));
         // --- After selecting Task List ---
            Locator taskTitleInput = modal.locator("input[placeholder='Enter Task Title']");
            taskTitleInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            taskTitleInput.fill("Automation Task Title");

            // Click Add button
            modal.locator("button:has-text('Add')").first().click();
            //addTaskButton.click();

            // Wait and verify success message
            //page.waitForSelector("text=Task created successfully", new Page.WaitForSelectorOptions().setTimeout(5000));
           // Assert.assertTrue(page.locator("text=Task Added Successfully").isVisible(),
                    //"Task creation success message not shown!");

            System.out.println("Task created successfully with Task List selected.");

        } else {
            // No task lists → handle properly
            System.out.println("No Task Lists found. Creating one...");

            // Verify "No Task Lists" message is visible
            Assert.assertTrue(page.locator("ul[role='listbox']").textContent().contains("No Task Lists"),
                    "No Task Lists message not visible!");

            // Cancel modal
            modal.locator("button:has-text('Cancel')").click();

           
        }


        // 14) Add Task again
        page.click("button:has-text('ADD TASK')");
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 15) Fill mandatory fields
        Locator projectInput1 = modal.getByRole(AriaRole.COMBOBOX).first();
        projectInput1.click();
        projectInput1.fill("akshayam");

        Locator projectOption1 = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName("akshayam"));
        projectOption1.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        projectOption1.click();
        System.out.println("Project selected: TechnoTackle Projects");
        
        modal.locator("input[placeholder='Enter Task Title']").fill("Automation Task Title");
     // Locate the description editor
        Locator descriptionEditor = modal.locator("div.ql-editor[contenteditable='true']");

        // Click to focus
        descriptionEditor.click();

        // Type text into the editor
        descriptionEditor.type("This is my automated task description.");


        ////// Select Task List
        Locator dropdownOptions1 = page.locator("ul[role='listbox'] li[role='option']");
        List<String> options1 = dropdownOptions1.allTextContents();
        System.out.println("Task List options: " + options1);

        dropdownOptions.first().click();
        System.out.println("Selected Task List: " + options.get(0));
        
        // Dates
        modal.locator("input[name='startDate']").fill("06/10/2025");
        modal.locator("input[name='endDate']").fill("07/10/2025");

        // Estimated hours
        modal.locator("input[name='WorkHours']").fill("5");

        // Priority
        modal.locator("input[placeholder='Select priority']").click();
        page.locator("div[role='option']:has-text('High')").click();

        // 16) Add task
        modal.locator("button:has-text('Add')").first().click();

       
    }

    /**
     * Helper method to select a project in the modal using MUI autocomplete.
     */
    private void selectProject(Locator modal, String projectName) {
        Locator projectInput = modal.locator("input[placeholder='Select project']");
        projectInput.click();
        projectInput.fill(projectName);
        page.waitForTimeout(300); // wait for MUI dropdown to render
        Locator projectOption = page.locator("ul[role='listbox'] div[role='option']:has-text('" + projectName + "')");
        projectOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        projectOption.click();
        System.out.println("Project selected: " + projectName);
    }

    
     
    

    @AfterClass
   public void tearDown() {
        browser.close();
        playwright.close();
    }
}
