package net.thucydides.samples;

import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
public class MultipleTestScenario {
    
    @Managed
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "http://www.google.com")
    public Pages pages;
    
    @Steps
    public SampleScenarioSteps steps;
        
    @Test
    public void happy_day_scenario() {
        steps.stepThatUsesABrowser();
        steps.stepThatSucceeds();
        steps.stepThatIsIgnored();
        steps.stepThatIsPending();
        steps.anotherStepThatSucceeds();
        steps.stepThatFails();
        steps.stepThatShouldBeSkipped();
    }

    @Test
    public void scenario_2() {
        steps.stepThatUsesABrowser();
        steps.stepThatSucceeds();
        steps.stepThatIsIgnored();
        steps.stepThatIsPending();
        steps.anotherStepThatSucceeds();
        steps.stepThatFails();
        steps.stepThatShouldBeSkipped();
    }

    @Test
    public void scenario_3() {
        steps.stepThatUsesABrowser();
        steps.stepThatSucceeds();
        steps.stepThatIsIgnored();
        steps.stepThatIsPending();
        steps.anotherStepThatSucceeds();
        steps.stepThatFails();
        steps.stepThatShouldBeSkipped();
    }
}