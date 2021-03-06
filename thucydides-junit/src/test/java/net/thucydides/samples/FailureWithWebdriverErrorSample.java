package net.thucydides.samples;

import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * This is a very simple scenario of testing a single page.
 * @author johnsmart
 *
 */
@RunWith(ThucydidesRunner.class)
public class FailureWithWebdriverErrorSample {

    @Managed(uniqueSession=true)
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "http://www.google.com")
    public Pages pages;
    
    @Steps
    public DemoSiteSteps steps;
        
    @Test
    public void the_user_opens_the_page() {
    	try {
    	steps.enter_values("Label 1", true);
        steps.should_have_selected_value("Label 2");
        steps.do_something();
    	} catch(Throwable e) {
    		e.printStackTrace();
    	}
    }    
    
    @Test
    public void the_user_opens_another_page() {
        steps.enter_values("Label 2", true);
        steps.do_something_else();
    }

    @Test
    public void the_user_opens_a_third_page() {
        steps.enter_values("Label 3", true);
        steps.do_something_else();
    }


}
