package playwrightLearning;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class DynamicTable {
    
	static Page page;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Playwright playwright= Playwright.create();
	      Browser browser=playwright.chromium().launch(new BrowserType.LaunchOptions()
	              .setHeadless(false)
	    		  .setChannel("chrome"));
	      Page page=browser.newPage();
	      page.navigate("https://datatables.net/extensions/select/examples/initialisation/checkbox.html");
	      
          
	}

}
