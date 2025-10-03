
		package pms;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class LoginUtil {

    private static Playwright playwright;

    // Launch browser once
    public static Browser launchBrowser() {
        playwright = Playwright.create();
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    // Reusable login method
    public static Page loginUser(Browser browser, String username, String password) {
        Page page = browser.newPage();
        page.navigate("https://pms.bacet.org/login");
        page.fill("input[name='email']", username);
        page.fill("input[name='password']", password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
        System.out.println("Login successful for user: " + username);
        return page;
    }

    // Close Playwright safely
    public static void closeBrowser(Browser browser) {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
