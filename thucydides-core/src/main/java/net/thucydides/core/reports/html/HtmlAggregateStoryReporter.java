package net.thucydides.core.reports.html;

import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.model.FeatureResults;
import net.thucydides.core.model.NumericalFormatter;
import net.thucydides.core.model.StoryTestResults;
import net.thucydides.core.model.UserStoriesResultSet;
import net.thucydides.core.model.features.FeatureLoader;
import net.thucydides.core.model.userstories.UserStoryLoader;
import net.thucydides.core.reports.ThucydidesReportData;
import net.thucydides.core.reports.UserStoryTestReporter;
import net.thucydides.core.reports.history.TestHistory;
import net.thucydides.core.reports.html.history.TestResultSnapshot;
import net.thucydides.core.reports.json.JSONProgressResultTree;
import net.thucydides.core.reports.json.JSONResultTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.thucydides.core.model.ReportNamer.ReportType.HTML;

/**
 * Generates an aggregate acceptance test report in XML form. Reads all the
 * reports from the output directory and generates an aggregate report
 * summarizing the results.
 */
public class HtmlAggregateStoryReporter extends HtmlReporter implements UserStoryTestReporter {

    private static final String DEFAULT_USER_STORY_TEMPLATE = "freemarker/user-story.ftl";

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlAggregateStoryReporter.class);
    private static final String STORIES_TEMPLATE_PATH = "freemarker/stories.ftl";
    private static final String HISTORY_TEMPLATE_PATH = "freemarker/history.ftl";
    private static final String FEATURES_TEMPLATE_PATH = "freemarker/features.ftl";
    private static final String COVERAGE_DATA_TEMPLATE_PATH = "freemarker/coverage.ftl";
    private static final String PROGRESS_DATA_TEMPLATE_PATH = "freemarker/progress.ftl";
    private static final String HOME_TEMPLATE_PATH = "freemarker/index.ftl";
    private static final String DASHBOARD_TEMPLATE_PATH = "freemarker/dashboard.ftl";
    private FeatureLoader featureLoader;
    private UserStoryLoader storyLoader;
    private TestHistory testHistory;
    private String projectName;

    public HtmlAggregateStoryReporter(final String projectName) {
        storyLoader = new UserStoryLoader();
        featureLoader = new FeatureLoader();
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    protected TestHistory getTestHistory() {
        if (testHistory == null) {
            testHistory = new TestHistory(getProjectName());
        }
        return testHistory;
    }
    /**
     * Generate aggregate XML reports for the test run reports in the output directory.
     * Returns the list of
     */
    public File generateReportFor(final StoryTestResults storyTestResults) throws IOException {
        
        LOGGER.info("Generating report for user story "
                    + storyTestResults.getTitle() + " to " + getOutputDirectory());

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("story", storyTestResults);
        addFormattersToContext(context);
        String htmlContents = mergeTemplate(DEFAULT_USER_STORY_TEMPLATE).usingContext(context);

        copyResourcesToOutputDirectory();

        String reportFilename = storyTestResults.getReportName(HTML);
        return writeReportToOutputDirectory(reportFilename, htmlContents);
    }

    private void addFormattersToContext(final Map<String, Object> context) {
        Formatter formatter = new Formatter(ThucydidesSystemProperty.getIssueTrackerUrl());
        context.put("formatter", formatter);
        context.put("formatted", new NumericalFormatter());
    }

    public ThucydidesReportData generateReportsForStoriesFrom(final File sourceDirectory) throws IOException {
        List<StoryTestResults> storyResults = loadStoryResultsFrom(sourceDirectory);
        List<FeatureResults> featureResults = loadFeatureResultsFrom(sourceDirectory);

        copyResourcesToOutputDirectory();

        for(StoryTestResults storyTestResults : storyResults) {
            generateReportFor(storyTestResults);
        }

        generateAggregateReportFor(storyResults, featureResults);

        return new ThucydidesReportData(featureResults, storyResults);
    }

    private List<StoryTestResults> loadStoryResultsFrom(final File sourceDirectory) throws IOException {
        return storyLoader.loadFrom(sourceDirectory);
    }

    private List<FeatureResults> loadFeatureResultsFrom(final File sourceDirectory) throws IOException {
        return featureLoader.loadFrom(sourceDirectory);
    }

    private void generateAggregateReportFor(final List<StoryTestResults> storyResults,
                                            final List<FeatureResults> featureResults) throws IOException {
        LOGGER.info("Generating summary report for user stories to "+ getOutputDirectory());

        copyResourcesToOutputDirectory();

        generateStoriesReport(storyResults);
        generateFeatureReport(featureResults);
        generateReportHomePage(storyResults, featureResults);
        
        updateHistoryFor(featureResults);
        generateHistoryReport();
    }

    private void updateHistoryFor(final List<FeatureResults> featureResults) {
        getTestHistory().updateData(featureResults);
    }

    private void generateHistoryReport()  throws IOException {
        List<TestResultSnapshot> history = getTestHistory().getHistory();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("history", history);
        context.put("rowcount", history.size());
        addFormattersToContext(context);
        String htmlContents = mergeTemplate(HISTORY_TEMPLATE_PATH).usingContext(context);
        LOGGER.debug("Writing history page");
        writeReportToOutputDirectory("history.html", htmlContents);

    }

    private void generateFeatureReport(final List<FeatureResults> featureResults) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        addFormattersToContext(context);
        context.put("features", featureResults);
        String htmlContents = mergeTemplate(FEATURES_TEMPLATE_PATH).usingContext(context);
        writeReportToOutputDirectory("features.html", htmlContents);

        for(FeatureResults feature : featureResults) {
            generateStoryReportForFeature(feature);
        }
    }

    private void generateStoryReportForFeature(FeatureResults feature) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();

        context.put("stories", feature.getStoryResults());
        context.put("storyContext", feature.getFeature().getName() );
        addFormattersToContext(context);
        LOGGER.debug("Generating stories page");
        String htmlContents = mergeTemplate(STORIES_TEMPLATE_PATH).usingContext(context);
        LOGGER.debug("Writing stories page");
        String filename = feature.getStoryReportName();
        writeReportToOutputDirectory(filename, htmlContents);
    }

    private void generateStoriesReport(final List<StoryTestResults> storyResults) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("stories", storyResults);
        context.put("storyContext", "All stories");
        addFormattersToContext(context);
        String htmlContents = mergeTemplate(STORIES_TEMPLATE_PATH).usingContext(context);
        LOGGER.debug("Writing stories page");
        writeReportToOutputDirectory("stories.html", htmlContents);
    }

    private void generateReportHomePage(final List<StoryTestResults> storyResults,
                                        final List<FeatureResults> featureResults) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("stories", new UserStoriesResultSet(storyResults));
        context.put("features", featureResults);
        addFormattersToContext(context);

        LOGGER.debug("Generating report pages");
        generateReportPage(context, HOME_TEMPLATE_PATH, "index.html");
        generateReportPage(context, DASHBOARD_TEMPLATE_PATH, "dashboard.html");

        LOGGER.debug("Generating coverage data");
        generateCoverageData(featureResults);
        generateProgressData(featureResults);
    }

    private void generateReportPage(final Map<String, Object> context,
                                    final String template,
                                    final String outputFile) throws IOException {
        String htmlContents = mergeTemplate(template).usingContext(context);
        writeReportToOutputDirectory(outputFile, htmlContents);
    }

    private void generateCoverageData(final List<FeatureResults> featureResults) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();

        JSONResultTree resultTree = new JSONResultTree();
        for(FeatureResults feature : featureResults) {
            resultTree.addFeature(feature);
        }

        context.put("coverageData", resultTree.toJSON());
        addFormattersToContext(context);

        String javascriptCoverageData = mergeTemplate(COVERAGE_DATA_TEMPLATE_PATH).usingContext(context);
        writeReportToOutputDirectory("coverage.js", javascriptCoverageData);
    }

    private void generateProgressData(final List<FeatureResults> featureResults) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();

        JSONProgressResultTree resultTree = new JSONProgressResultTree();
        for(FeatureResults feature : featureResults) {
            resultTree.addFeature(feature);
        }

        context.put("progressData", resultTree.toJSON());
        addFormattersToContext(context);

        String javascriptCoverageData = mergeTemplate(PROGRESS_DATA_TEMPLATE_PATH).usingContext(context);
        writeReportToOutputDirectory("progress.js", javascriptCoverageData);
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        if (issueTrackerUrl != null) {
            ThucydidesSystemProperty.setValue(ThucydidesSystemProperty.ISSUE_TRACKER_URL, issueTrackerUrl);
        }
    }

    public void clearHistory() {
        getTestHistory().clearHistory();
    }

    public void setJiraUrl(String jiraUrl) {
        if (jiraUrl != null) {
            ThucydidesSystemProperty.setValue(ThucydidesSystemProperty.JIRA_URL, jiraUrl);
        }
    }
}
