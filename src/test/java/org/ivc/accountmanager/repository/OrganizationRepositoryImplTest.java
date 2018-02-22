package org.ivc.accountmanager.repository;

import java.io.IOException;
import static org.fest.assertions.api.Assertions.*;

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.ArrayUtils;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.Organization;
import org.ivc.accountmanager.repository.OrganizationRepository;
import org.ivc.accountmanager.repository.OrganizationRepositoryImpl;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * OrganizationRepositoryImpl integration tests.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class OrganizationRepositoryImplTest {

    //-------------------Inner classes--------------------------------------------
    @Import({LdapConfig.class, OrganizationRepositoryImpl.class, ValidatorConfig.class})
    @SpringBootApplication
    public static class OrganizationRepositoryImplTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String[] ORGANIZATION_VALID_NAMES = new String[]{
        "TestOrganization1", "TestOrganization2"};

    private static final String LDIF_FILE_NAME = "ivc.ldif";

    private static final String VALID_ORGANIZATION_NAME = "Это организация №3";

    private static final String NOT_EXISTENS_ORGANIZATION_NAME = "InvalidOrganization";

    private static final String EXISTENS_ORGANIZATION_NAME = "TestOrganization1";
    //-------------------Fields---------------------------------------------------

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private LdapContextSource contextSource;

    //-------------------Before test---------------------------------------------
    @Before
    public void reloadLdapDirectory() throws javax.naming.NamingException, IOException {
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
        LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName(LdapConfig.LDAP_BASE),
                new ClassPathResource(LDIF_FILE_NAME));
    }

    //-------------------Tests-------------------------------------------------
    //======================FindAll============================================
    @Test
    public void happyPathFindAll() {
        // act
        List<Organization> organizations = organizationRepository.findAll();
        // assert
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly((Object[]) ORGANIZATION_VALID_NAMES);
    }

    //======================Create=============================================
    @Test
    public void happyPathCreate() {
        // arrange
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        // act
        organizationRepository.create(organization);
        // assert
        List<Organization> organizations = organizationRepository.findAll();
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length + 1)
                .containsOnly((Object[]) ArrayUtils.add(ORGANIZATION_VALID_NAMES, VALID_ORGANIZATION_NAME));
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithNullArgument() {
        // act
        organizationRepository.create(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithInvalidArgument() {
        // arrange
        Organization organization = new Organization("");
        // act
        organizationRepository.create(organization);
    }

    //======================Delete=============================================
    @Test
    public void happyPathDelete() {
        // arrange
        Organization organization = new Organization(ORGANIZATION_VALID_NAMES[0]);
        // act
        organizationRepository.delete(organization);
        // assert
        List<Organization> organizations = organizationRepository.findAll();
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length - 1)
                .containsOnly(ORGANIZATION_VALID_NAMES[1]);
    }

    @Test
    public void shouldNotChangeDirectoryWhenDeleteNotExistOrganization() {
        // arrange
        Organization organization = new Organization(NOT_EXISTENS_ORGANIZATION_NAME);
        // act
        organizationRepository.delete(organization);
        // assert
        List<Organization> organizations = organizationRepository.findAll();
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly((Object[]) ORGANIZATION_VALID_NAMES);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForDeleteWithNullArgument() {
        // act
        organizationRepository.delete(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForDeleteWithNotValidArgument() {
        // arrange
        Organization organization = new Organization("");
        // act
        organizationRepository.delete(organization);
    }

    //======================Replace=============================================
    @Test
    public void happyPathReplace() {
        // arrange
        Organization oldOrganization = new Organization(ORGANIZATION_VALID_NAMES[0]);
        Organization newOrganization = new Organization(VALID_ORGANIZATION_NAME);
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
        // assert
        List<Organization> organizations = organizationRepository.findAll();
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly(ORGANIZATION_VALID_NAMES[1], VALID_ORGANIZATION_NAME);
    }

    @Test(expected = NameNotFoundException.class)
    public void shouldThrowNFEWhenReplaceNotExistsOrganization() {
        // arrange
        Organization oldOrganization = new Organization(NOT_EXISTENS_ORGANIZATION_NAME);
        Organization newOrganization = new Organization(VALID_ORGANIZATION_NAME);
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
        // assert
        List<Organization> organizations = organizationRepository.findAll();
        assertThat(extractProperty(Organization.NAME_PROPERTY_NAME).from(organizations))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly(ORGANIZATION_VALID_NAMES);
    }

     @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForReplaceWithOldOrganizationNullArgument() {
        // arrange
        Organization oldOrganization = null;
        Organization newOrganization = new Organization(VALID_ORGANIZATION_NAME);
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForReplaceWithOldOrganizationEmptyArgument() {
        // arrange
        Organization oldOrganization = new Organization("");
        Organization newOrganization = new Organization(VALID_ORGANIZATION_NAME);
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForReplaceWithNewOrganizationNullArgument() {
        // arrange
        Organization oldOrganization = new Organization(VALID_ORGANIZATION_NAME);
        Organization newOrganization = null;
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForReplaceWithNewOrganizationEmptyArgument() {
        // arrange
        Organization oldOrganization = new Organization(VALID_ORGANIZATION_NAME);
        Organization newOrganization = new Organization("");
        // act
        organizationRepository.replace(oldOrganization, newOrganization);
    }
    
    //======================IsExistByName=============================================
    @Test
    public void testIsExistByNameTrue() {
        assertTrue(organizationRepository.isExistsByName(EXISTENS_ORGANIZATION_NAME));
    }

    @Test
    public void testIsExistByNameFalse() {
        assertTrue(!organizationRepository.isExistsByName(NOT_EXISTENS_ORGANIZATION_NAME));
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForIsExistByNameWithNullArgument() {
        organizationRepository.isExistsByName(null);
    }

}
