package net.thucydides.core.reports.integration;

import net.thucydides.core.annotations.Feature;
import net.thucydides.core.annotations.Issue;
import net.thucydides.core.annotations.Issues;
import net.thucydides.core.annotations.Story;
import net.thucydides.core.annotations.Title;
import net.thucydides.core.model.StoryTestResults;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.reports.UserStoryTestReporter;
import net.thucydides.core.reports.html.HtmlAggregateStoryReporter;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static net.thucydides.core.model.TestStepFactory.failingTestStepCalled;
import static net.thucydides.core.model.TestStepFactory.skippedTestStepCalled;
import static net.thucydides.core.model.TestStepFactory.successfulTestStepCalled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class WhenGeneratingUserStoryHtmlReports {

    @Rule
    public TemporaryFolder temporaryDirectory = new TemporaryFolder();

    private net.thucydides.core.model.Story userStory = net.thucydides.core.model.Story.from(AUserStory.class);
    private StoryTestResults storyTestResults;

    private UserStoryTestReporter reporter;

    private File outputDirectory;


    class AUserStory {};

    @Story(AUserStory.class)
    @Issue("#100")
    class SomeTestScenario {
        public void a_simple_test_case() {};
        @Issues({"#300","#400"})
        public void should_do_this() {};
        @Issue("#200")
        public void should_do_that() {};
        public void should_also_do_this() {};
    }

    @Feature
    class AFeature {
        class AUserStoryInAFeature {};
    }

    @Story(AFeature.AUserStoryInAFeature.class)
    class SomeTestScenarioInAFeature {
        public void should_do_this() {};
        public void should_do_that() {};
    }

    @Before
    public void setupTestReporter() {
        reporter = new HtmlAggregateStoryReporter("project");
        outputDirectory = temporaryDirectory.newFolder("temp");
        reporter.setOutputDirectory(outputDirectory);

        storyTestResults = new StoryTestResults(userStory);
        storyTestResults.recordTestRun(thatFailsCalled("should_do_this"));
        storyTestResults.recordTestRun(thatSucceedsCalled("should_do_that"));
        storyTestResults.recordTestRun(thatFailsCalled("should_also_do_this"));
    }

    @Test
    public void should_write_aggregate_reports_to_output_directory() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        assertThat(userStoryReport.exists(), is(true));
    }

    @Test
    public void should_write_aggregate_report_to_a_file_named_after_the_user_story() throws Exception {
        Set<String> issues = storyTestResults.getIssues();
        assertThat(issues, allOf(hasItem("#100"), hasItem("#200"), hasItem("#300"), hasItem("#400")));
    }

    @Test
    public void should_return_formatted_issue_numbers() throws Exception {
        assertThat(storyTestResults.getFormattedIssues(), is("(#100, #200, #300, #400)"));
    }

    @Test
    public void should_find_issues_for_a_story() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        assertThat(userStoryReport.getName(), is("a_user_story.html"));
    }

    @Test
    public void aggregate_report_should_contain_the_user_story_name_as_a_title() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        String reportText = getStringFrom(userStoryReport);
        assertThat(reportText, containsString("A user story"));
    }

    @Test
    public void aggregate_report_should_contain_links_to_the_test_runs() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        String reportText = getStringFrom(userStoryReport);
        assertThat(reportText, containsString("href=\"a_user_story_should_do_this.html\""));
        assertThat(reportText, containsString("href=\"a_user_story_should_do_that.html\""));
        assertThat(reportText, containsString("href=\"a_user_story_should_also_do_this.html\""));
    }

    @Test
    public void can_generate_aggregate_reports_from_xml_files_in_a_directory() throws Exception {
        HtmlAggregateStoryReporter reporter = new HtmlAggregateStoryReporter("project");
        reporter.setOutputDirectory(outputDirectory);  
        File sourceDirectory = new File("src/test/resources/multiple-user-story-reports");
        reporter.generateReportsForStoriesFrom(sourceDirectory);

        File expectedStoryReport1 = new File(outputDirectory, "a_user_story_in_a_feature.html");
        assertThat(expectedStoryReport1.exists(), is(true));
        
        File expectedStoryReport2 = new File(outputDirectory, "another_user_story_in_a_feature.html");
        assertThat(expectedStoryReport2.exists(), is(true));
        
        File expectedStoryReport3 = new File(outputDirectory, "yet_another_user_story.html");
        assertThat(expectedStoryReport3.exists(), is(true));
    }
    
    @Test
    public void should_generate_stories_html_report() throws Exception {
        HtmlAggregateStoryReporter reporter = new HtmlAggregateStoryReporter("project");
        reporter.setOutputDirectory(outputDirectory);  
        File sourceDirectory = new File("src/test/resources/multiple-user-story-reports");
        reporter.generateReportsForStoriesFrom(sourceDirectory);
        File expectedStoryHtmlReport = new File(outputDirectory, "stories.html");
        assertThat(expectedStoryHtmlReport.exists(), is(true));
    }

    @Test
    public void should_copy_resources_to_target_directory() throws Exception {
        HtmlAggregateStoryReporter reporter = new HtmlAggregateStoryReporter("project");
        reporter.setOutputDirectory(outputDirectory);

        URL dir = Thread.currentThread().getContextClassLoader().getResource("multiple-user-story-reports");
        dir.getPath();
        File sourceDirectory = new File(dir.getPath());
        //File sourceDirectory = new File("src/test/resources/multiple-user-story-reports");
        reporter.generateReportsForStoriesFrom(sourceDirectory);
        File expectedCssStylesheet = new File(new File(outputDirectory,"css"), "core.css");
        assertThat(expectedCssStylesheet.exists(), is(true));
    }

    @Test
    public void aggregate_failing_story_should_display_failing_icon() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        String reportText = getStringFrom(userStoryReport);
        assertThat(reportText, containsString("fail.png"));
    }

    @Test
    public void aggregate_failing_story_should_display_test_titles() throws Exception {
        File userStoryReport = reporter.generateReportFor(storyTestResults);
        String reportText = getStringFrom(userStoryReport);
        assertThat(reportText, containsString("A user story"));
    }

    private String getStringFrom(File reportFile) throws IOException {
        return FileUtils.readFileToString(reportFile);
    }
    
    private TestOutcome thatFailsCalled(String testName) {
        TestOutcome testOutcome = TestOutcome.forTest(testName, SomeTestScenario.class);
        testOutcome.recordStep(successfulTestStepCalled("Step 1"));
        testOutcome.recordStep(failingTestStepCalled("Step 2", new AssertionError("Oh bother!")));
        testOutcome.recordStep(skippedTestStepCalled("Step 3"));
        return testOutcome;
    }

    private TestOutcome thatSucceedsCalled(String testName) {
        TestOutcome testOutcome = TestOutcome.forTest(testName, SomeTestScenario.class);
        testOutcome.recordStep(successfulTestStepCalled("Step 1"));
        testOutcome.recordStep(successfulTestStepCalled("Step 2"));
        return testOutcome;
    }

}
