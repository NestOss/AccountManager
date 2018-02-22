package org.ivc.accountmanager.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.ArrayUtils;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.PrefixShaPasswordEncoder;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.UserRepository;
import org.ivc.accountmanager.repository.UserRepositoryImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.unitils.reflectionassert.ReflectionAssert.*;

/**
 * UserRepositoryImpl integration tests.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class UserRepositoryImplTest {
//-------------------Inner classes--------------------------------------------

    @Configuration
    @EnableAutoConfiguration
    @Import({LdapConfig.class, UserRepositoryImpl.class, ValidatorConfig.class})
    public static class UserRepositoryImplTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String[] VALID_USER_IDS = new String[]{"000", "111", "222"};
    private static final String[] USER_IDS_WHO_MEMBER_OF_ORGANIZATION1 = new String[]{"111", "222"};
    
    private static final String ADMIN_UID = "000";
    private static final String NOT_EXISTS_USERS_UID = "exterminator";
    private static final String ADMIN_PASSWORD = "000";
    private static final String ADMIN_COMMON_NAME = "Roman Osipov";
    private static final String ADMIN_SHORT_NAME = "Roman";
    private static final String VALID_ORGANIZATION_NAME = "TestOrganization2";
    private static final String SEARCH_ORGANIZATION_NAME = "TestOrganization1";
    private static final boolean USER_ACTIVE_STATUS = true;
    
    private static final String VALID_USER_UID = "user";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String VALID_USER_COMMON_NAME = "common";
    private static final String VALID_USER_SHORT_NAME = "short";
    private static final String VALID_USER_ORGANIZATION = "organization";
    private static final boolean VALID_USER_ACTIVE_STATUS = true;
    
    private static final String LDIF_FILE_NAME = "ivc.ldif";

    //-------------------Fields---------------------------------------------------
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LdapContextSource contextSource;

    //-------------------Before test---------------------------------------------
    @Before
    public void setup() throws Exception {
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
        LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName(LdapConfig.LDAP_BASE),
                new ClassPathResource(LDIF_FILE_NAME));
    }

    //-------------------Tests-------------------------------------------------
    //===================findAll===============================================
    @Test
    public void happyPathFindAll() {
        // act
        List<User> users = userRepository.findAll();
        // assert
        assertThat(extractProperty(User.UID_PROPERTY).from(users))
                .hasSize(VALID_USER_IDS.length)
                .containsOnly((Object[]) VALID_USER_IDS);
    }

    //===================create===============================================
    @Test
    public void happyPathCreate() {
        // arrange
        User user = new User(VALID_USER_UID, VALID_USER_PASSWORD, VALID_USER_COMMON_NAME,
                VALID_USER_SHORT_NAME, VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        ShaPasswordEncoder encoder = new PrefixShaPasswordEncoder(256);
        encoder.setEncodeHashAsBase64(true);
        String hash = encoder.encodePassword(VALID_USER_PASSWORD, null);
        // act
        userRepository.create(user);
        // assert
        List<User> users = userRepository.findAll();
        assertThat(extractProperty(User.UID_PROPERTY).from(users))
                .hasSize(VALID_USER_IDS.length + 1)
                .containsOnly((Object[]) ArrayUtils.add(VALID_USER_IDS, VALID_USER_UID));
        Optional<User> oUser = userRepository.findByUid(VALID_USER_UID);
        assertTrue(oUser.isPresent());
        assertEquals(hash, oUser.get().getPassword());
        oUser.get().setRole(user.getRole());
        assertReflectionEquals(user, oUser.get());
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithNullArgument() {
        // act
        userRepository.create(null);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithInvalidArguments() {
        // arrange
        User user = new User("", VALID_USER_PASSWORD, VALID_USER_COMMON_NAME,
                VALID_USER_SHORT_NAME, VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        // act
        userRepository.create(user);
    }

    //===================findByUid===============================================
    @Test
    public void happyPathFindByUid() {
        // arrange
        User user = new User(ADMIN_UID, ADMIN_PASSWORD, ADMIN_COMMON_NAME, ADMIN_SHORT_NAME,
                VALID_ORGANIZATION_NAME, USER_ACTIVE_STATUS);
        //act
        Optional<User> oUser = userRepository.findByUid(user.getId());
        // assert
        assertTrue(oUser.isPresent());
        oUser.get().setRole(user.getRole());
        user.setDn(oUser.get().getDn());
        assertReflectionEquals(user, oUser.get());
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByUidWhenCallWithNullArgument() {
        // act
        userRepository.findByUid(null);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByUidWhenCallWithEmptyArgument() {
        // act
        userRepository.findByUid("");
    }

    //===================update================================================
    @Test
    public void happyPathUpdate() {
        //arrange
        User user = userRepository.findByUid(ADMIN_UID).get();
        user.setActive(!user.isActive());
        user.setShortName(VALID_USER_SHORT_NAME);
        user.setCommonName(VALID_USER_COMMON_NAME);
        user.setOrganizationName(VALID_USER_ORGANIZATION);
        user.setPassword(VALID_USER_PASSWORD);
        //act
        userRepository.update(user);
        //assert
        Optional<User> oFoundUser = userRepository.findByUid(ADMIN_UID);
        assertReflectionEquals(user, oFoundUser.get());
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWithNullArgument() {
        // act
        userRepository.update(null);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWithInvalidArguments() {
        // arrange
        User user = new User("", VALID_USER_PASSWORD, VALID_USER_COMMON_NAME,
                VALID_USER_SHORT_NAME, VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        // act
        userRepository.update(user);
    }
    
    @Test(expected = NameNotFoundException.class)
    public void shouldThrowCVEForUpdateWithNotExistsUser() {
        //arrange
        User user = userRepository.findByUid(ADMIN_UID).get();
        user.setDn(null);
        user.setId(NOT_EXISTS_USERS_UID);
        user.setActive(!user.isActive());
        user.setShortName(VALID_USER_SHORT_NAME);
        user.setCommonName(VALID_USER_COMMON_NAME);
        user.setOrganizationName(VALID_USER_ORGANIZATION);
        user.setPassword(VALID_USER_PASSWORD);
        //act
        userRepository.update(user);
    }

    //===================delete===============================================
    @Test
    public void happyPathDelete() {
        // arrange
        User user = userRepository.findByUid(ADMIN_UID).get();
        // act
        userRepository.delete(user);
        // assert
        List<User> users = userRepository.findAll();
        assertThat(extractProperty(User.UID_PROPERTY).from(users))
                .hasSize(VALID_USER_IDS.length - 1)
                .containsOnly((Object[]) ArrayUtils.remove(VALID_USER_IDS, 0));
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForDeleteWithNullArgument() {
        // act
        userRepository.delete(null);
    }

    //===================findByOrganization================================
    @Test
    public void happyPathFindByOrganiztion() {
        //act
        List<User> users = userRepository.findByOrganization(SEARCH_ORGANIZATION_NAME);
        // assert
        assertThat(extractProperty(User.UID_PROPERTY).from(users))
                .hasSameSizeAs(USER_IDS_WHO_MEMBER_OF_ORGANIZATION1)
                .containsOnly((Object[]) USER_IDS_WHO_MEMBER_OF_ORGANIZATION1);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByOrganizationWhenCallWithNullArgument() {
        // act
        userRepository.findByOrganization(null);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByOrganizationWhenCallWithEmptyArgument() {
        // act
        userRepository.findByOrganization("");
    }
    
}
