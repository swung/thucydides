package net.thucydides.core.webdriver.integration;

import net.thucydides.core.pages.PageObject;
import net.thucydides.core.webdriver.SupportedWebDriver;
import net.thucydides.core.webdriver.WebDriverFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class WhenCheckingVisibilityOnAWebSiteUsingPageObjects {

    public class IndexPage extends PageObject {

        public WebElement multiselect;

        public WebElement doesNotExist;

        public IndexPage(WebDriver driver) {
            super(driver);
        }

        public IndexPage(WebDriver driver, int ajaxTimeout) {
            super(driver, ajaxTimeout);
        }
    }
    
    private static WebDriver driver;
    
    @BeforeClass
    public static void open_local_static_site() {
        driver = (new WebDriverFactory()).newInstanceOf(SupportedWebDriver.FIREFOX);
        openStaticTestSite(driver);
    }

    @AfterClass
    public static void closeBrowser() {
        driver.quit();
    }

    private static File fileInClasspathCalled(final String resourceName) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(resourceName).getPath());
    }

    private static void openStaticTestSite(WebDriver driver) {
        File testSite = fileInClasspathCalled("static-site/index.html");
        driver.get("file://" + testSite.getAbsolutePath());

    }

    @Test
    public void should_succeed_immediately_if_title_is_as_expected() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.waitForTitleToAppear("Thucydides Test Site");
    }

    @Test(expected = ElementNotVisibleException.class)
    public void should_fail_if_title_is_as_expected() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(100);
        indexPage.waitForTitleToAppear("Wrong title");
    }

    @Test
    public void should_know_when_an_element_is_visible_on_the_page() {
        IndexPage indexPage = new IndexPage(driver);

        assertThat(indexPage.isElementVisible(By.xpath("//h2[.='A visible title']")), is(true));
    }

    @Test
    public void should_succeed_when_waiting_for_an_element_that_is_already_visible_on_the_page() {
        IndexPage indexPage = new IndexPage(driver);

        indexPage.setWaitForTimeout(100);
        indexPage.waitForRenderedElements(By.xpath("//h2[.='A visible title']"));
    }

    @Test
    public void should_know_when_an_element_is_visible_on_the_page_using_should_be() {
        IndexPage indexPage = new IndexPage(driver);

        indexPage.shouldBeVisible(By.xpath("//h2[.='A visible title']"));
    }


    @Test
    public void should_know_when_an_element_is_present_but_not_visible_on_the_page() {
        IndexPage indexPage = new IndexPage(driver);

        assertThat(indexPage.isElementVisible(By.xpath("//h2[.='An invisible title']")), is(false));
    }

    @Test
    public void an_inexistant_element_should_not_be_considered_visible() {
        IndexPage indexPage = new IndexPage(driver);

        assertThat(indexPage.isElementVisible(By.xpath("//h2[.='An title that does not exist']")), is(false));
    }

    @Test
    public void should_know_when_an_element_is_present_but_not_visible_on_the_page_using_should_be() {
        IndexPage indexPage = new IndexPage(driver);

        indexPage.shouldNotBeVisible(By.xpath("//h2[.='An invisible title']"));
    }

    @Test(expected = ElementNotVisibleException.class)
    public void should_fail_when_waiting_for_an_invisible_object() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(150);

        indexPage.waitForRenderedElements(By.xpath("//h2[.='An invisible title']"));

    }

    @Test
    public void can_wait_for_one_of_several_elements_to_be_visible() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(150);
        indexPage.waitForAnyRenderedElementOf(By.id("color"), By.id("taste"), By.id("sound"));
    }

    @Test(expected = ElementNotVisibleException.class)
    public void fails_if_waiting_for_text_to_disappear_too_long() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(150);

        indexPage.waitForTextToDisappear("A visible title");
    }


    @Test(expected = ElementNotVisibleException.class)
    public void should_fail_when_waiting_for_an_undisplayed_text() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(150);

        indexPage.waitForTextToAppear("This text never appears");
    }

    @Test
    public void should_succeed_when_waiting_for_displayed_text() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.setWaitForTimeout(150);

        indexPage.waitForTextToAppear("A visible title");
    }


    @Test
    public void should_know_when_an_element_is_not_present_on_the_page() {
        IndexPage indexPage = new IndexPage(driver);

        assertThat(indexPage.isElementVisible(By.xpath("//h2[.='Non-existant title']")), is(false));
    }
    
    @Test
    public void should_detect_if_a_web_element_contains_a_string() {
        IndexPage indexPage = new IndexPage(driver);
        assertThat(indexPage.containsTextInElement(indexPage.multiselect, "Label 1"), is(true));
    }

    @Test
    public void should_detect_if_a_web_element_does_not_contain_a_string() {
        IndexPage indexPage = new IndexPage(driver);
        assertThat(indexPage.containsTextInElement(indexPage.multiselect, "Red"), is(false));
    }

    @Test(expected = AssertionError.class)
    public void should_fail_assert_if_a_web_element_does_not_contain_a_string() {
        IndexPage indexPage = new IndexPage(driver);
        indexPage.shouldContainTextInElement(indexPage.multiselect, "Red");
    }

    @Test
    public void should_return_meaningful_error_messages_when_an_element_is_not_found() {
        IndexPage indexPage = new IndexPage(driver, 1);
        boolean exceptionThrown = false;
        try {
            indexPage.doesNotExist.click();
        } catch(Throwable e) {
            assertThat(e.getCause(), is(notNullValue()));
            assertThat(e.getCause().getMessage(), containsString("doesNotExist"));
            exceptionThrown = true;
        }

        assertThat(exceptionThrown, is(true));
    }

}
