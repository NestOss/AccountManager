/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.domain;

import java.util.regex.Pattern;
import org.codehaus.plexus.util.StringUtils;
import org.ivc.accountmanager.config.ValidUtils;
import org.ivc.accountmanager.config.ValidatorConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test organization bean.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@TestPropertySource("classpath:/ValidationMessages.properties")
@ActiveProfiles("test")
public class OrganizationTest {

    //-------------------Logger---------------------------------------------------
    //-------------------Inner classes--------------------------------------------
    @Configuration
    @EnableAutoConfiguration
    @Import({ValidUtils.class, ValidatorConfig.class})
    public static class OrganizationTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String VALID_BEAN_NAME = "org1";
    private static final String INVALID_ORGANIZATION_NAME
            = StringUtils.repeat("A", Organization.MAX_NAME_LENGTH + 1);

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
    public void shouldThrowIAEForValidationWithEmptyName() {
        // arrange
        Organization organization = new Organization("");
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                Organization.NAME_PROPERTY_NAME, env.getProperty("blank.field"));
        // act & assert
        validate(organization, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithNullName() {
        // arrange
        Organization organization = new Organization(null);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                Organization.NAME_PROPERTY_NAME, env.getProperty("blank.field"));
        // act & assert
        validate(organization, expectedMessage);
    }

    @Test
    public void shouldThrowIAEForValidationWithToLongName() {
        // arrange
        Organization organization = new Organization(INVALID_ORGANIZATION_NAME);
        String expectedMessage = String.format("%s %s %s", VALID_BEAN_NAME,
                Organization.NAME_PROPERTY_NAME, env.getProperty("length.field")
                .replaceAll(Pattern.quote("{max}"), "" + Organization.MAX_NAME_LENGTH));
        // act & assert
        validate(organization, expectedMessage);
    }
}
