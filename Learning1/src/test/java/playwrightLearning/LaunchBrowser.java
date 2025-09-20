package playwrightLearning;

import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
@Test
public class LaunchBrowser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
      Playwright playwright= Playwright.create();
      Browser browser=playwright.chromium().launch(
    		  new BrowserType.LaunchOptions()
    		  .setHeadless(false)
    		  .setChannel("chrome"));
      Page page=browser.newPage();
      page.navigate("https://letcode.in/");
      String title=page.title();
      System.out.println("Page title:" + title);
      page.close();
      browser.close();
      playwright.close();
      
	}

}
 