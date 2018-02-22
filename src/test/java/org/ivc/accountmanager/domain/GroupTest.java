/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.domain;

import org.ivc.accountmanager.config.ValidUtils;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.Group;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test group bean.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@TestPropertySource("classpath:/ValidationMessages.properties")
@ActiveProfiles("test")
public class GroupTest {
    //-------------------Logger---------------------------------------------------

    //-------------------Inner classes--------------------------------------------
    @Configuration
    @EnableAutoConfiguration
    @Import({ValidUtils.class, ValidatorConfig.class})
    public static class GroupTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private final static String VALID_BEAN_NAME = "group1";

    //-------------------Fields---------------------------------------------------
    @Autowired
    ValidUtils validUtils;

    @Autowired
    Environment env;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    private void validate(Object bean, String expectedMessage) {
        try {
            validUtils.validateBean(bean, VALID_BEAN_NAME);
            fail("Expected exception was not thrown.");
        } catch (IllegalArgumentException ex) {
            // assert
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    @Test
    public void shouldThrowIAEForValidationWithEmptyCommonName() {
        // arrange
        Group group = new Group("");
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                Group.COMMON_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(group, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithNullCommonName() {
        // arrange
        Group group = new Group(null);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                Group.COMMON_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(group, expectedMessage);
    }
}
