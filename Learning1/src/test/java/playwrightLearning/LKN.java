package playwrightLearning;

import com.microsoft.playwright.*;

public class LKN {
    public static void main(String[] args) {
        // Create a Playwright instance
        try (Playwright playwright = Playwright.create()) {
            // Launch a browser instance
        	Playwright playwright1= Playwright.create();
            Browser browser=playwright1.chromium().launch(
          		  new BrowserType.LaunchOptions()
          		  .setHeadless(false)
          		  .setChannel("chrome"));
            Page page=browser.newPage();
            page.navigate("https://dev.lakshmikrishnanaturals.com/login");
            // Fill in the username and password fields
            page.locator("#mobile_number").fill("9787656765"); 
            page.locator("#login_sumbit").click(); 

            page.locator("#verify_number").fill("1234"); 

            // Click the login button
            Locator loginButton = page.locator("#login_submit"); // Replace with correct selector
            loginButton.scrollIntoViewIfNeeded();
            loginButton.waitFor();
            loginButton.click();
 

            // Optional: Verify successful login
            if (page.title().equals("Dashboard")) { // Replace "Dashboard" with the expected title after login
                System.out.println("Login successful!");
            } else {
                System.out.println("Login failed. Please check credentials or selectors.");
            }

            // Close the browser
            browser.close();
        }
    }
}
