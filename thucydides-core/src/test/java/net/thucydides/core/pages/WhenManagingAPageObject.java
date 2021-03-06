package net.thucydides.core.pages;

import net.thucydides.core.junit.rules.SaveWebdriverSystemPropertiesRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WhenManagingAPageObject {

    @Mock
    WebDriver driver;

    @Mock
    WebElement mockButton;
    
    @Rule
    public MethodRule saveSystemProperties = new SaveWebdriverSystemPropertiesRule();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    class BasicPageObject extends PageObject {
        
        protected WebElement button;

        public BasicPageObject(WebDriver driver) {
            super(driver);
        }

        protected WebElement getButton() {
            return mockButton;
        }

    }

    @Test
    public void the_page_gets_the_title_from_the_web_page() {

        when(driver.getTitle()).thenReturn("Google Search Page");
        BasicPageObject page = new BasicPageObject(driver);

        assertThat(page.getTitle(), is("Google Search Page"));
    }

    @Test
    public void page_will_wait_for_rendered_element_if_it_is_already_present() {

        WebElement renderedElement = mock(WebElement.class);
        List<WebElement> renderedElements = new ArrayList<WebElement>();
        renderedElements.add(renderedElement);

        when(driver.findElement(any(By.class))).thenReturn(renderedElement);
        when(driver.findElements(any(By.class))).thenReturn(renderedElements);

        when(renderedElement.isDisplayed()).thenReturn(true);

        BasicPageObject page = new BasicPageObject(driver);
        page.waitForRenderedElements(By.id("whatever"));
    }


    @Test
    public void thenReturnElementList_will_return_the_list_of_matching_elements() {

        WebElement renderedElement = mock(WebElement.class);
        List<WebElement> renderedElements = new ArrayList<WebElement>();
        renderedElements.add(renderedElement);

        when(driver.findElement(any(By.class))).thenReturn(renderedElement);
        when(driver.findElements(any(By.class))).thenReturn(renderedElements);

        BasicPageObject page = new BasicPageObject(driver);
        List<WebElement> elementList = page.thenReturnElementList(By.className("whatever"));

        assertThat(elementList, is(renderedElements));
    }


    @Test
    public void page_will_wait_for_rendered_element_to_disappear() {

        List<WebElement> emptyList = Arrays.asList();
        when(driver.findElements(any(By.class))).thenReturn(emptyList);

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(100);
        page.waitForRenderedElementsToDisappear(By.id("whatever"));
    }

    @Test
    public void page_can_delay_requests_for_a_short_period() {
        long start = System.currentTimeMillis();
        BasicPageObject page = new BasicPageObject(driver);
        page.waitABit(500);

        assertThat((int) (System.currentTimeMillis() - start), greaterThanOrEqualTo(500));
    }

    @Test(expected = UnexpectedElementVisibleException.class)
    public void wait_for_rendered_element_to_disappear_will_fail_if_element_does_not_disappear() {

        WebElement textBlock = mock(WebElement.class);
        when(textBlock.isDisplayed()).thenReturn(true);
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(driver.findElements(any(By.class))).thenReturn(listWithElements);

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(150);
        page.waitForRenderedElementsToDisappear(By.id("whatever"));
    }

    @Test
    public void page_will_wait_for_rendered_element_if_it_is_not_already_present() {

        WebElement renderedElement = mock(WebElement.class);
        List<WebElement> renderedElements = new ArrayList<WebElement>();
        renderedElements.add(renderedElement);

        when(driver.findElement(any(By.class))).thenReturn(renderedElement);
        when(driver.findElements(any(By.class))).thenReturn(renderedElements);
        when(renderedElement.isDisplayed()).thenReturn(false).thenReturn(false).thenReturn(true);

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForRenderedElements(By.id("whatever"));
    }

    @Test
    public void page_will_wait_for_text_to_appear_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(driver.findElements(any(By.class))).thenReturn(emptyList).thenReturn(listWithElements);

        page.waitForTextToAppear("hi there");
    }

    @Test(expected = ElementNotVisibleException.class)
    public void wait_for_text_to_appear_will_fail_if_the_text_doesnt_appear() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(driver.findElements(any(By.class))).thenReturn(emptyList);
        page.setWaitForTimeout(200);
        page.waitForTextToAppear("Waiting for Godot.");
    }

    @Test
    public void page_will_wait_for_title_to_appear_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        when(driver.getTitle()).thenReturn("waiting..").thenReturn("a title");

        page.waitForTitleToAppear("a title");
    }

    @Test
    public void page_will_wait_for_title_to_appear_if_already_there() {

        BasicPageObject page = new BasicPageObject(driver);
        when(driver.getTitle()).thenReturn("a title");

        page.waitForTitleToAppear("a title");
    }

    @Test
    public void page_will_wait_for_title_to_disappear_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        when(driver.getTitle()).thenReturn("a title").thenReturn("all gone");

        page.waitForTitleToDisappear("a title");
    }

    @Test(expected = ElementNotVisibleException.class)
    public void page_will_wait_for_title_to_disappear_should_fail_if_title_doesnt_disappear() {

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(100);
        when(driver.getTitle()).thenReturn("a title");

        page.waitForTitleToDisappear("a title");
    }

    @Test
    public void page_will_wait_for_text_to_appear_in_element_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList).thenReturn(listWithElements);

        page.waitForTextToAppear(searchedBlock,"hi there");
    }

    @Test
    public void wait_for_text_to_appear_in_element_will_succeed_if_element_is_already_present() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(searchedBlock.findElements(any(By.class))).thenReturn(listWithElements);

        page.waitForTextToAppear(searchedBlock,"hi there");
    }

    @Test(expected = ElementNotVisibleException.class)
    public void wait_for_text_to_appear_in_element_will_fail_if_text_does_not_appear() {

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(150);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList);

        page.waitForTextToAppear(searchedBlock,"hi there");
    }

    @Test
    public void page_will_wait_for_text_to_appear_successfully_if_already_present() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(searchedBlock.findElements(any(By.class))).thenReturn(listWithElements);

        page.waitForTextToAppear(searchedBlock,"hi there");
    }

    @Test
    public void page_will_wait_for_text_to_appear_in_an_element_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList).thenReturn(listWithElements);

        page.waitForAnyTextToAppear(searchedBlock, "hi there");
    }


    @Test(expected=ElementNotVisibleException.class)
    public void page_will_fail_if_text_fails_to_appear() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList);
        page.setWaitForTimeout(200);
        page.waitForTextToAppear("hi there");
    }

    @Test(expected=ElementNotVisibleException.class)
    public void page_will_fail_if_single_text_fails_to_appear_in_an_element_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList);
        page.setWaitForTimeout(200);
        page.waitForAnyTextToAppear(searchedBlock, "hi there");
    }

    @Test(expected=ElementNotVisibleException.class)
    public void page_will_fail_if_text_fails_to_appear_in_an_element_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();

        when(searchedBlock.findElements(any(By.class))).thenReturn(emptyList);

        page.setWaitForTimeout(200);
        page.waitForAnyTextToAppear(searchedBlock, "hi there");
    }

    @Test
    public void page_will_wait_for_text_to_disappear_if_requested() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement textBlock = mock(WebElement.class);

        List<WebElement> emptyList = Arrays.asList();
        List<WebElement> listWithElements = Arrays.asList(textBlock);

        when(driver.findElements(any(By.class))).thenReturn(listWithElements).thenReturn(emptyList);

        page.waitForTextToDisappear("hi there");
    }

    @Test(expected=NoSuchElementException.class)
    public void should_contain_text_should_throw_an_assertion_if_text_is_not_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        List<WebElement> emptyList = Arrays.asList();
        when(driver.findElements(any(By.class))).thenReturn(emptyList);

        page.shouldContainText("hi there");
    }

    @Test
    public void should_contain_text_should_do_nothing_if_text_is_present() {
        WebElement textBlock = mock(WebElement.class);
        BasicPageObject page = new BasicPageObject(driver);
        List<WebElement> emptyList = Arrays.asList(textBlock);
        when(driver.findElements(any(By.class))).thenReturn(emptyList);

        page.shouldContainText("hi there");
    }

    @Test
    public void entering_a_value_in_a_field_will_clear_it_first() {
        WebElement field = mock(WebElement.class);
        BasicPageObject page = new BasicPageObject(driver);

        page.typeInto(field, "some value");

        verify(field).clear();
        verify(field).sendKeys("some value");
    }

    @Test
    public void should_provide_a_fluent_api_for_entering_a_value_in_a_field() {
        WebElement field = mock(WebElement.class);
        BasicPageObject page = new BasicPageObject(driver);

        page.enter("some value").into(field);

        verify(field).clear();
        verify(field).sendKeys("some value");
    }

    @Test
    public void should_provide_a_fluent_api_for_entering_a_value_in_a_field_using_a_selector() {
        WebElement field = mock(WebElement.class);
        when(driver.findElement(By.id("field-id"))).thenReturn(field);
        BasicPageObject page = new BasicPageObject(driver);

        page.enter("some value").intoField(By.id("field-id"));

        verify(field).clear();
        verify(field).sendKeys("some value");
    }


    @Test(expected=NoSuchElementException.class)
    public void page_will_throw_exception_if_waiting_for_rendered_element_does_not_exist() {

        when(driver.findElement(any(By.class))).thenThrow(new NoSuchElementException("No such element"));

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForRenderedElements(By.id("whatever"));
    }


    @Test(expected=ElementNotVisibleException.class)
    public void page_will_throw_exception_if_waiting_for_rendered_element_is_not_visible() {

        WebElement renderedElement = mock(WebElement.class);
        when(driver.findElement(any(By.class))).thenReturn(renderedElement);
        when(renderedElement.isDisplayed()).thenReturn(false);

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForRenderedElements(By.id("whatever"));
    }


    @Test
    public void page_will_succeed_for_any_of_several_rendered_elements() {

        WebElement renderedElement = mock(WebElement.class);
        elementIsRendered(renderedElement, By.id("element1"));
        noElementIsRendered(By.id("element2"));

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForAnyRenderedElementOf(By.id("element1"), By.id("element2"));
    }

    @Test(expected=ElementNotVisibleException.class)
    public void page_will_fail_for_any_of_several_rendered_elements_if_element_is_displayed_but_not_rendered() {

        WebElement renderedElement = mock(WebElement.class);
        elementIsDisplayedButNotRendered(renderedElement, By.id("element1"));
        noElementIsRendered(By.id("element2"));

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForAnyRenderedElementOf(By.id("element1"), By.id("element2"));
    }



    @Test
    public void page_will_wait_for_any_of_several_rendered_elements() {

        WebElement renderedElement = mock(WebElement.class);
        elementIsRenderedWithDelay(renderedElement, By.id("element1"));
        noElementIsRendered(By.id("element2"));

        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForAnyRenderedElementOf(By.id("element1"), By.id("element2"));
    }


    @Test(expected = ElementNotVisibleException.class)
    public void page_will_fail_if_none_of_the_several_rendered_elements_are_present() {

        noElementIsRendered(By.id("element1"));
        noElementIsRendered(By.id("element2"));


        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForAnyRenderedElementOf(By.id("element1"), By.id("element2"));
    }


    @Test
    public void page_can_wait_for_an_element_to_disappear() {

        WebElement renderedElement = mock(WebElement.class);
        elementDisappearsAfterADelay(renderedElement, By.id("element1"));
        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForRenderedElementsToDisappear(By.id("element1"));
    }

    @Test
    public void page_can_wait_for_an_element_to_disappear_if_element_is_not_initially_displayed() {

        noElementIsRendered(By.id("element1"));
        BasicPageObject page = new BasicPageObject(driver);
        page.setWaitForTimeout(200);
        page.waitForRenderedElementsToDisappear(By.id("element1"));
    }


    private void noElementIsRendered(By criteria) {
        List<WebElement> emptyList = Arrays.asList();
        when(driver.findElement(criteria)).thenThrow(new NoSuchElementException("No such element"));
        when(driver.findElements(criteria)).thenReturn(emptyList);
    }

    private void elementIsRendered(WebElement renderedElement, By criteria) {
        when(renderedElement.isDisplayed()).thenReturn(true);
        List<WebElement> listWithRenderedElement = Arrays.asList((WebElement) renderedElement);
        when(driver.findElement(criteria)).thenReturn(renderedElement);
        when(driver.findElements(criteria)).thenReturn(listWithRenderedElement);
    }

    private void elementIsDisplayedButNotRendered(WebElement renderedElement, By criteria) {
        when(renderedElement.isDisplayed()).thenReturn(false);
        List<WebElement> listWithRenderedElement = Arrays.asList((WebElement) renderedElement);
        when(driver.findElement(criteria)).thenReturn(renderedElement);
        when(driver.findElements(criteria)).thenReturn(listWithRenderedElement);
    }

    private void elementIsRenderedWithDelay(WebElement renderedElement, By criteria) {
        List<WebElement> emptyList = Arrays.asList();

        when(renderedElement.isDisplayed()).thenReturn(false).thenReturn(true);
        List<WebElement> listWithRenderedElement = Arrays.asList((WebElement) renderedElement);
        when(driver.findElement(criteria)).thenThrow(new NoSuchElementException("No such element"))
                                          .thenReturn(renderedElement);
        when(driver.findElements(criteria)).thenReturn(emptyList)
                                           .thenReturn(listWithRenderedElement);
    }


    private void elementDisappearsAfterADelay(WebElement renderedElement, By criteria) {
        List<WebElement> emptyList = Arrays.asList();

        when(renderedElement.isDisplayed()).thenReturn(true).thenReturn(false);
        List<WebElement> listWithRenderedElement = Arrays.asList((WebElement) renderedElement);
        when(driver.findElement(criteria)).thenReturn(renderedElement)
                                           .thenThrow(new NoSuchElementException("No such element"));
        when(driver.findElements(criteria)).thenReturn(listWithRenderedElement)
                                           .thenReturn(emptyList);
    }


    @Test(expected=AssertionError.class)
    public void should_be_visible_should_throw_an_assertion_if_element_is_not_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement field = mock(WebElement.class);
        when(field.isDisplayed()).thenReturn(false);

        page.shouldBeVisible(field);
    }

    @Test(expected=AssertionError.class)
    public void should_be_not_visible_should_throw_an_assertion_if_element_is_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement field = mock(WebElement.class);
        when(field.isDisplayed()).thenReturn(true);

        page.shouldNotBeVisible(field);
    }

    @Test
    public void should_be_not_visible_should_do_nothing_if_element_is_not_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement field = mock(WebElement.class);
        when(field.isDisplayed()).thenReturn(false);

        page.shouldNotBeVisible(field);
    }

    @Test
    public void should_be_visible_should_do_nothing_if_element_is_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement field = mock(WebElement.class);

        when(field.isDisplayed()).thenReturn(true);
        page.shouldBeVisible(field);
    }

    @Test
    public void should_be_visible_should_handle_changing_field_state() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement field = mock(WebElement.class);

        when(field.isDisplayed()).thenReturn(true);
        page.shouldBeVisible(field);

        when(field.isDisplayed()).thenReturn(false);
        page.shouldNotBeVisible(field);
    }

    @Test(expected = WebDriverException.class)
    public void when_clicking_on_something_should_throw_exception_if_it_fails_twice() {
        BasicPageObject page = new BasicPageObject(driver);

        doThrow(new WebDriverException()).when(mockButton).click();

        page.clickOn(page.getButton());
    }

    @Test
    public void the_page_should_initially_open_at_the_systemwide_default_url() {

        System.setProperty("webdriver.base.url","http://www.google.com");

        BasicPageObject page = new BasicPageObject(driver);

        Pages pages = new Pages(driver);
        pages.start();

        verify(driver).get("http://www.google.com");
        System.setProperty("webdriver.base.url","");
    }


    @Test
    public void the_start_url_for_a_page_can_be_overridden_by_the_system_default_url() {
        BasicPageObject page = new BasicPageObject(driver);
        PageConfiguration.getCurrentConfiguration().setDefaultBaseUrl("http://www.google.com");

        Pages pages = new Pages(driver);
        pages.setDefaultBaseUrl("http://www.google.co.nz");
        pages.start();

        verify(driver).get("http://www.google.com");
    }


    @Test
    public void page_should_detect_if_a_web_element_contains_a_string() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);
        when(searchedBlock.getText()).thenReturn("red green blue");

        assertThat(page.containsTextInElement(searchedBlock, "red"), is(true));
    }

    @Test
    public void page_should_detect_if_a_web_element_does_not_contain_a_string() {

        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);
        when(searchedBlock.getText()).thenReturn("red green blue");

        assertThat(page.containsTextInElement(searchedBlock, "orange"), is(false));
    }

    @Test(expected=AssertionError.class)
    public void should_contain_text_in_element_should_throw_an_assertion_if_text_is_not_visible() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);
        when(searchedBlock.getText()).thenReturn("red green blue");

        page.shouldContainTextInElement(searchedBlock, "orange");
    }

    @Test
    public void should_contain_text_in_web_element_should_do_nothing_if_text_is_present() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);
        when(searchedBlock.getText()).thenReturn("red green blue");

        page.shouldContainTextInElement(searchedBlock, "red");
    }

    @Test
    public void should_not_contain_text_in_web_element_should_do_nothing_if_text_is_not_present() {
        BasicPageObject page = new BasicPageObject(driver);
        WebElement searchedBlock = mock(WebElement.class);
        when(searchedBlock.getText()).thenReturn("red green blue");

        page.shouldNotContainTextInElement(searchedBlock, "orange");
    }

}
