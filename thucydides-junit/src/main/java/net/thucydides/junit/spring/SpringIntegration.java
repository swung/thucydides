package net.thucydides.junit.spring;


import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.test.context.TestContextManager;

/**
 * Use the Spring test annotations in Thucydides tests.
 *
 * @author johnsmart
 */
public class SpringIntegration extends TestWatchman {

    private final Class<?> clazz;

    private TestContextManager testContextManager;

    public static SpringIntegration forClass(Class<?> clazz) {
        return new SpringIntegration(clazz);
    }

    private SpringIntegration(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        TestContextManager contextManager = getTestContextManager(method.getMethod().getDeclaringClass());
        try {
            contextManager.prepareTestInstance(target);
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate test instance", e);
        }
        Statement statement = super.apply(base, method, target);
        statement = withBefores(method, target, statement, contextManager);
        statement = withAfters(method, target, statement, contextManager);
        return statement;
    }

    protected TestContextManager getTestContextManager(Class<?> clazz) {
        if (testContextManager == null) {
            testContextManager = new TestContextManager(clazz);
        }
        return testContextManager;
    }

    protected Statement withBefores(FrameworkMethod frameworkMethod,
                                    Object testInstance,
                                    Statement statement,
                                    TestContextManager testContextManager) {
        return new RunBeforeTestMethodCallbacks(statement,
                testInstance,
                frameworkMethod.getMethod(),
                testContextManager);
    }

    protected Statement withAfters(FrameworkMethod frameworkMethod,
                                   Object testInstance,
                                   Statement statement,
                                   TestContextManager testContextManager) {
        return new RunAfterTestMethodCallbacks(statement,
                testInstance,
                frameworkMethod.getMethod(),
                testContextManager);
    }

}