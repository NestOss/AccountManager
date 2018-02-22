package org.ivc.accountmanager.domain;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.ivc.accountmanager.config.ValidUtils;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.Organization;
import org.ivc.accountmanager.domain.User;
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
 * User tests for validator.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@TestPropertySource("classpath:/ValidationMessages.properties")
@ActiveProfiles("test")
public class UserTest {

    //-------------------Inner classes--------------------------------------------
    @Configuration
    @EnableAutoConfiguration
    @Import({ValidUtils.class, ValidatorConfig.class})
    public static class UserTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String VALID_BEAN_NAME = "user1";
    private static final String VALID_USER_ID = "user";
    private static final String INVALID_USER_ID
            = StringUtils.repeat("A", User.MAX_USERID_LENGTH + 1);
    private static final String VALID_PASSWORD = "123";
    private static final String VALID_COMMON_NAME = "common";
    private static final String INVALID_COMMON_NAME
            = StringUtils.repeat("A", User.MAX_COMMON_NAME_LENGTH + 1);
    private static final String VALID_SHORT_NAME = "short";
    private static final String INVALID_SHORT_NAME
            = StringUtils.repeat("A", User.MAX_SHORT_NAME_LENGTH + 1);
    private static final String INVALID_ORGANIZATION_NAME
            = StringUtils.repeat("A", Organization.MAX_NAME_LENGTH + 1);
    private static final String VALID_ORGANIZATION = "organization";
    private static final boolean VALID_ACTIVE_STATUS = true;

    //-------------------Fields---------------------------------------------------
    @Autowired
    ValidUtils validUtils;

    @Autowired
    Environment env;

    //-------------------Methods---------------------------------------------------
    private void validate(Object bean, String expectedMessage) {
        try {
            validUtils.validateBean(bean, VALID_BEAN_NAME);
            fail("Expected exception was not thrown.");
        } catch (IllegalArgumentException ex) {
            // assert
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    //-------------------Before tests------------------------------------------
    //-------------------Tests-------------------------------------------------
    //===================userId================================================
    @Test
    public void shouldThrowIAEForValidationWithNullUserId() {
        // arrange
        User user = new User(null, VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.UID_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithEmptyUserId() {
        // arrange
        User user = new User("", VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.UID_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithToLongUserId() {
        // arrange
        User user = new User(INVALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.UID_PROPERTY, env.getProperty("length.field")
                .replaceAll(Pattern.quote("{max}"), "" + User.MAX_USERID_LENGTH));
        // act & assert
        validate(user, expectedMessage);
    }

    //===================commonName===========================================
    @Test
    public void shouldThrowIAEForValidationWithNullCommonName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, null,
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.COMMON_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithEmptyCommonName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, "",
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.COMMON_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithToLongCommonName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, INVALID_COMMON_NAME,
                VALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.COMMON_NAME_PROPERTY, env.getProperty("length.field")
                .replaceAll(Pattern.quote("{max}"), "" + User.MAX_COMMON_NAME_LENGTH));
        // act & assert
        validate(user, expectedMessage);
    }

    //===================shortName============================================
    @Test
    public void shouldThrowIAEForValidationWithNullShortName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                null, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.SHORT_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithEmptyShortName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                "", VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.SHORT_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithToLongShortName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                INVALID_SHORT_NAME, VALID_ORGANIZATION, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.SHORT_NAME_PROPERTY, env.getProperty("length.field")
                .replaceAll(Pattern.quote("{max}"), "" + User.MAX_SHORT_NAME_LENGTH));
        // act & assert
        validate(user, expectedMessage);
    }

    //===================organizationName============================================
    @Test
    public void shouldThrowIAEForValidationWithNullOrganizationName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, null, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.ORGANIZATION_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithEmptyOrganizationName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, "", VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.ORGANIZATION_NAME_PROPERTY, env.getProperty("blank.field"));
        // act & assert
        validate(user, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithToLongOrganizationName() {
        // arrange
        User user = new User(VALID_USER_ID, VALID_PASSWORD, VALID_COMMON_NAME,
                VALID_SHORT_NAME, INVALID_ORGANIZATION_NAME, VALID_ACTIVE_STATUS);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                User.ORGANIZATION_NAME_PROPERTY, env.getProperty("length.field")
                .replaceAll(Pattern.quote("{max}"), "" + Organization.MAX_NAME_LENGTH));
        // act & assert
        validate(user, expectedMessage);
    }

}
