package org.ivc.accountmanager.selenium;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.test.context.TestExecutionListeners;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;
  
/**
 * Annotation for mark Selenium test.
 * @author Roman Osipov
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) 
@TestExecutionListeners(
        listeners = SeleniumTestExecutionListener.class,
        mergeMode = MERGE_WITH_DEFAULTS)
public @interface SeleniumTest {
     Class<? extends WebDriver> driver() default FirefoxDriver.class;
     String baseUrl() default "http://localhost:8080"; 
}
