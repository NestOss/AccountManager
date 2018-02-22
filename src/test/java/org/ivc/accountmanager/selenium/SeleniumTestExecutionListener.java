/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.selenium;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 *
 * @author Roman Osipov
 */
public class SeleniumTestExecutionListener extends AbstractTestExecutionListener {
    //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private WebDriver webDriver;
    
    private final Map<Class<? extends WebDriver>, DesiredCapabilities> capsMap  = new HashMap<>();
    {
        capsMap.put(FirefoxDriver.class, DesiredCapabilities.firefox());
    }
    
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    //-------------------Methods--------------------------------------------------
    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        //System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        if (webDriver != null) {
            return;
        }
        ApplicationContext context = testContext.getApplicationContext();
        if (context instanceof ConfigurableApplicationContext) {
            SeleniumTest annotation = findAnnotation(
                    testContext.getTestClass(), SeleniumTest.class);
            
            Class<? extends WebDriver> driverClass = annotation.driver();
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            DesiredCapabilities caps = capsMap.get(driverClass);
            caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            Constructor<? extends WebDriver> constructor =
                    driverClass.getConstructor(Capabilities.class);
            webDriver = BeanUtils.instantiateClass(constructor, caps);
            
            // webDriver = BeanUtils.instantiate(annotation.driver());
            ConfigurableApplicationContext configurableApplicationContext =
                    (ConfigurableApplicationContext) context;
            ConfigurableListableBeanFactory bf = configurableApplicationContext.getBeanFactory();
            bf.registerResolvableDependency(WebDriver.class, webDriver);
        }
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        if (webDriver != null) {
            SeleniumTest annotation = findAnnotation(
                    testContext.getTestClass(), SeleniumTest.class);
            webDriver.get(annotation.baseUrl());
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (testContext.getTestException() == null) {
            return;
        }

        File screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
        String testName = toLowerUnderscore(testContext.getTestClass().getSimpleName());
        String methodName = toLowerUnderscore(testContext.getTestMethod().getName());

        Files.copy(screenshot.toPath(),
                Paths.get("screenshots", testName + "_" + methodName + "_" + screenshot.getName()));
    }

    private static String toLowerUnderscore(String upperCamel) {
        return Stream
                .of(upperCamel.split("(?=[A-Z])"))
                .map(s -> s.toLowerCase())
                .collect(Collectors.joining("_"));
    }
}
