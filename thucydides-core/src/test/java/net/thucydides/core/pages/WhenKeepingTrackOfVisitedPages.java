package net.thucydides.core.pages;


import net.thucydides.core.junit.rules.SaveWebdriverSystemPropertiesRule;
import net.thucydides.core.webdriver.UnsupportedDriverException;
import net.thucydides.core.webdriver.WebDriverFacade;
import net.thucydides.core.webdriver.WebDriverFactory;
import net.thucydides.core.webdriver.WebdriverProxyFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WhenKeepingTrackOfVisitedPages {

    @Mock
    WebDriver driver;
    
    @Mock
    WebDriverFacade driverProxy;

    @Mock
    WebdriverProxyFactory proxyFactory;

    @Rule
    public MethodRule saveSystemProperties = new SaveWebdriverSystemPropertiesRule();

    @Before
    public void initMocksAndClearSystemwideDefaultUrl() {
        MockitoAnnotations.initMocks(this);
        noSystemwideDefaultUrlIsDefined();
    }
    
    @Test
    public void the_pages_object_should_have_a_default_starting_point_url() {

        final String baseUrl = "http://www.google.com";
        final Pages pages = new Pages(driver);

        pages.setDefaultBaseUrl(baseUrl);

        pages.start();
        
        verify(driver).get(baseUrl);    
    }

    private void noSystemwideDefaultUrlIsDefined() {
        PageConfiguration.getCurrentConfiguration().setDefaultBaseUrl(null);
    }


    @Test
    public void the_default_starting_point_url_can_refer_to_a_file_on_the_classpath() {

        final String baseUrl = "classpath:static-site/index.html";
        final Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl(baseUrl);

        URL staticSiteUrl = Thread.currentThread().getContextClassLoader().getResource("static-site/index.html");

        pages.start();

        verify(driver).get(staticSiteUrl.toString());
    }

    @Test
    public void the_default_starting_point_url_can_be_overriden_by_a_system_property() {

        final String defaultBaseUrl = "http://www.google.com";
        final String systemDefinedBaseUrl = "http://www.google.com.au";
        final Pages pages = new Pages(driver);
        
        System.setProperty("webdriver.base.url", systemDefinedBaseUrl);
        
        pages.start();
        
        verify(driver).get(systemDefinedBaseUrl);    
    }

    @Test
    public void the_pages_object_knows_when_we_are_on_the_right_page() {

        when(driver.getCurrentUrl()).thenReturn("http://www.apache.org");
        final Pages pages = new Pages(driver);
        pages.start();

        assertThat(pages.isCurrentPageAt(ApacheHomePage.class), is(true));
    }

    @Test
    public void the_pages_object_knows_when_we_are_not_on_the_right_page() {

        when(driver.getCurrentUrl()).thenReturn("http://www.google.org");
        final Pages pages = new Pages(driver);
        pages.start();

        assertThat(pages.isCurrentPageAt(ApacheHomePage.class), is(false));
    }

    @Test
    public void the_get_method_is_shorthand_for_currentPageAt() {

        when(driver.getCurrentUrl()).thenReturn("http://www.apache.org");
        final Pages pages = new Pages(driver);
        pages.start();

        assertThat(pages.get(ApacheHomePage.class).getClass().getName(),
                    is(ApacheHomePage.class.getName()));
    }

    @Test
    public void the_getAt_method_is_Groovy_shorthand_for_currentPageAt() {

        when(driver.getCurrentUrl()).thenReturn("http://www.apache.org");
        final Pages pages = new Pages(driver);
        pages.start();

        assertThat(pages.getAt(ApacheHomePage.class).getClass().getName(),
                    is(ApacheHomePage.class.getName()));
    }


    @Test(expected = WrongPageError.class)
    public void the_pages_object_throws_a_wrong_page_error_when_we_expect_the_wrong_page() {

        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        final Pages pages = new Pages(driver);
        pages.start();

        pages.currentPageAt(ApacheHomePage.class);
    }


    public final class InvalidHomePage extends PageObject {
        public InvalidHomePage() {
            super(null);
        }
    }

    @Test(expected = WrongPageError.class)
    public void the_pages_object_throws_a_wrong_page_error_when_the_page_object_is_invalid() {

        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        final Pages pages = new Pages(driver);
        pages.start();

        pages.currentPageAt(InvalidHomePage.class);
    }

    public final class ExplodingHomePage extends PageObject {
        public ExplodingHomePage(final WebDriver driver) throws InstantiationException {
            super(null);
            throw new InstantiationException();
        }
    }

    @Test(expected = WrongPageError.class)
    public void the_pages_object_throws_a_wrong_page_error_when_the_page_object_cant_be_instantiated() {

        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        final Pages pages = new Pages(driver);
        pages.start();

        pages.currentPageAt(ExplodingHomePage.class);
    }

    public class PageObjectWithNoDriverConstructor extends PageObject {

        public PageObjectWithNoDriverConstructor() {
            super(null);
        }
    }

    @Test(expected = WrongPageError.class)
    public void the_pages_object_throws_a_wrong_page_error_when_the_page_object_doesnt_have_a_webdriver_constructor() {

        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        final Pages pages = new Pages(driver);
        pages.currentPageAt(PageObjectWithNoDriverConstructor.class);
    }

    @Test
    public void should_open_initial_page_when_driver_opens() {
        Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl("http://www.google.com");
        pages.notifyWhenDriverOpens();

        verify(driver).get("http://www.google.com");
    }

    static final class GooglePage extends PageObject {

        public GooglePage(final WebDriver driver) {
            super(driver);
        }
    }

    static final class SomeOtherPage extends PageObject {

        public SomeOtherPage(final WebDriver driver) {
            super(driver);
        }
    }

    @Test
    public void should_requery_driver_for_each_page_request() {
        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl("http://www.google.com");

        GooglePage page1 = pages.get(GooglePage.class);
        GooglePage page2 = pages.get(GooglePage.class);
        assertThat(page2, is(not(page1)));
    }

    @Test
    public void should_use_the_same_page_object_if_we_indicate_that_are_on_the_same_unchanged_page() {
        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl("http://www.google.com");

        GooglePage page1 = pages.get(GooglePage.class);
        pages.onSamePage();
        GooglePage page2 = pages.get(GooglePage.class);
        assertThat(page2, is(page1));
    }

    @Test
    public void should_use_a_new_page_object_if_we_indicate_that_are_on_the_same_unchanged_page_but_we_are_not() {
        when(driver.getCurrentUrl()).thenReturn("http://www.google.com");
        Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl("http://www.google.com");

        GooglePage page1 = pages.get(GooglePage.class);
        pages.get(SomeOtherPage.class);
        pages.onSamePage();
        GooglePage page2 = pages.get(GooglePage.class);
        assertThat(page2, is(not(page1)));
    }

    @Test
    public void should_not_open_initial_page_when_driver_opens_if_using_a_proxied_driver() {
        Pages pages = new Pages(driverProxy);
        pages.setDefaultBaseUrl("http://www.google.com");
        pages.notifyWhenDriverOpens();

        verify(driver, never()).get("http://www.google.com");
    }

    @Test
    public void should_register_proxy_drivers_when_driver_opens() {
        Pages pages = new Pages(driverProxy) {
            @Override
            protected WebdriverProxyFactory getProxyFactory() {
                return proxyFactory;
            }
        };
        pages.setDefaultBaseUrl("http://www.google.com");
        pages.notifyWhenDriverOpens();

        verify(proxyFactory).registerListener(any(PagesEventListener.class));
    }


    class InvalidWebDriverClass extends FirefoxDriver {
        InvalidWebDriverClass() throws IllegalAccessException {
            throw new IllegalAccessException();
        }
    }

    @Test(expected = UnsupportedDriverException.class)
    public void should_throw_exception_if_invalid_driver_used() {
        WebDriverFacade facade = new WebDriverFacade(InvalidWebDriverClass.class, new WebDriverFactory());
        facade.getProxiedDriver();
    }


}
