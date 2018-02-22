package org.ivc.accountmanager.repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.naming.Name;
import javax.validation.ConstraintViolationException;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.Role;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.Group;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.GroupRepository;
import org.ivc.accountmanager.repository.GroupRepositoryImpl;
import org.junit.Assert;
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
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * GroupRepositoryImpl integration tests.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class GroupRepositoryImplTest {

    //-------------------Inner classes--------------------------------------------
    @Configuration
    @EnableAutoConfiguration
    @Import({LdapConfig.class, GroupRepositoryImpl.class, ValidatorConfig.class})
    public static class GroupRepositoryImplTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String[] GROUP_VALID_COMMON_NAMES = new String[]{
        Role.ADMIN, Role.ROCKET_ADMIN, Role.SENSOR_ADMIN, Role.USER,
        Role.ROCKET_USER, Role.SENSOR_USER};

    private static final String USER_UID = "000";
    private static final String NOT_EXISTS_USER_UID = "ABC";
    private static final String NOT_EXISTS_GROUP = "BEATLES";
    private static final String INVALID_ROLE = "EXTERMINATOR";
    private static final String NEW_USER_UID = "999";
    private static final String LDIF_FILE_NAME = "ivc.ldif";

    //-------------------Fields---------------------------------------------------
    @Autowired
    private GroupRepository groupRepository;

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
        List<Group> groups = groupRepository.findAll();
        // assert
        assertThat(extractProperty(Group.COMMON_NAME_PROPERTY).from(groups))
                .hasSize(GROUP_VALID_COMMON_NAMES.length)
                .containsOnly((Object[]) GROUP_VALID_COMMON_NAMES);
    }

    //======================FindByCn============================================
    /**
     * Test of findByCn method, of class GroupRepositoryImpl.
     */
    @Test
    public void happyPathFindByCn() {
        //act
        Optional<Group> result = groupRepository.findByCn(Role.ADMIN);
        //assert
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(Role.ADMIN, result.get().getCommonName());
    }

    @Test
    public void shouldReturnEmptyOptionalForFindByCnWithInvalidRole() {
        //act
        Optional<Group> result = groupRepository.findByCn(INVALID_ROLE);
        assertTrue(!result.isPresent());
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByCnWhenCallWithEmptyStringArgument() {
        // act 
        groupRepository.findByCn("");
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByCnWhenCallWithNullArgument() {
        // act 
        groupRepository.findByCn(null);
    }

    //======================FindByMember============================================ 
    @Test
    public void happyPathFindByMember() {
        //arrange
        Name absUserDn = LdapNameBuilder.newInstance(LdapConfig.LDAP_BASE).
                add(User.BASE_DN).
                add(User.UID_ATTRIBUTE, USER_UID).
                build();
        //act
        Optional<Group> optional = groupRepository.findByMember(USER_UID);
        // assert
        assertTrue(optional.isPresent());
        Group result = optional.get();
        assertThat(result.getMembers()).contains(absUserDn);
        assertEquals(Role.ADMIN, result.getCommonName());
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByMemberWhenCallWithNullArgument() {
        // act
        groupRepository.findByMember(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByMemberWhenCallWithEmptyArgument() {
        // act
        groupRepository.findByMember("");
    }

    @Test
    public void shouldReturnEmptyOptionForFindByMemberWhenCallWithNotExistsMember() {
        // act
        Optional<Group> optionalGroup = groupRepository.findByMember(NOT_EXISTS_USER_UID);
        assertTrue(!optionalGroup.isPresent());
    }

    //======================Update============================================
    @Test
    public void happyPathUpdate() {
        // arrange
        Group group = groupRepository.findByCn(Role.ADMIN).get();
        Name memberName = (Name) group.getMembers().toArray()[0];
        Name newMemberName = LdapNameBuilder.newInstance(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, NEW_USER_UID).build();
        group.removeMember(memberName);
        group.addMember(newMemberName);
        Set<Name> expectedMembers = group.getMembers();
        // act
        groupRepository.update(group);
        // assert
        Optional<Group> expOptionalGroup = groupRepository.findByCn(Role.ADMIN);
        Set<Name> actualMembers = expOptionalGroup.get().getMembers();
        assertThat(actualMembers)
                .hasSameSizeAs(expectedMembers)
                .containsAll(expectedMembers);
    }

    @Test(expected = NameNotFoundException.class)
    public void shouldThrowNNFEForUpdateForNotExistsGroup() {
        // arrange
        Group group = new Group(NOT_EXISTS_GROUP);
        Name newMemberName = LdapNameBuilder.newInstance(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, NEW_USER_UID).build();
        group.addMember(newMemberName);
        // act
        groupRepository.update(group);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWhenCallWithInvalidArgument() {
        // act
        groupRepository.update(new Group(""));
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWhenCallWithNullArgument() {
        // act
        groupRepository.update(null);
    }

    //======================findUsersByGroup============================================
//    @Test
//    public void happyPathFindUsersByGroup() {
//        // act
//        List<User> users = groupRepository.findUsersByGroup(Role.ROCKET_ADMIN);
//        // assert
//        assertThat(extractProperty(User.UID_PROPERTY).from(users))
//                .hasSize(2)
//                .containsOnly("222", "111");
//    }
//
//    @Test(expected = ConstraintViolationException.class)
//    public void shouldThrowCVEForFindUsersByGroupWhenCallWithEmptyArgument() {
//        // act
//        groupRepository.findUsersByGroup("");
//    }
//
//    @Test(expected = ConstraintViolationException.class)
//    public void shouldReturnEmptyOptionForFindUsersByGroupWhenCallWithNotExistsMember() {
//        // act
//        groupRepository.findUsersByGroup(null);
//    }

}
