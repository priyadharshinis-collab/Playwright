package playwrightLearning;

import org.xml.sax.Locator;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Inputs {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 Playwright playwright= Playwright.create();
	      Browser browser=playwright.chromium().launch(
	    		  new BrowserType.LaunchOptions()
	    		  .setHeadless(false)
	    		  .setChannel("chrome"));
	      Page page=browser.newPage();
	      page.navigate("https://letcode.in/edit");
	      
	      page.waitForTimeout(2000);  
          page.locator("#fullName").type("priya");
          com.microsoft.playwright.Locator locator = page.locator("#join");
          locator.press("End");
          locator.fill(" man");
          locator.press("Tab");
          String attribute=page.locator("id=getMe").getAttribute("value");
          System.out.println(attribute);
          //clear the text
          page.locator("//*[@id=\"clearMe\"]").clear();
          
          
	}

}
