package playwrightLearning;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class WhatIsBrowserContext {

    public static void main(String[] args) {

        // Create Playwright instance
        Playwright playwright = Playwright.create();
        
        // Launch the browser (Chrome in non-headless mode)
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setChannel("chrome")
        );

        // Create a new browser context with video recording options
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/"))   // Set directory for recorded videos
                .setRecordVideoSize(1280, 720)             // Set resolution for video
        );

        // Open a new page
        Page page = context.newPage();
        
        // Navigate to the URL
        page.navigate("https://letcode.in/");
        
        // Get the page title and print it
        String title = page.title();
        System.out.println("Page title: " + title);
        
        // Close the context (this will finalize video recording)
        context.close();
        
        // Close Playwright
        playwright.close();
    }
}





		

		
